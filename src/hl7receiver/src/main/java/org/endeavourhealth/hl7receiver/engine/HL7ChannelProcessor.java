package org.endeavourhealth.hl7receiver.engine;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.mapping.Mapper;
import org.endeavourhealth.hl7receiver.messageprocessor.HL7MessageProcessor;
import org.endeavourhealth.hl7receiver.model.db.*;
import org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class HL7ChannelProcessor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelProcessor.class);
    private static final int LOCK_RECLAIM_INTERVAL_SECONDS = 60;
    private static final int LOCK_BREAK_OTHERS_SECONDS = 360;
    private static final int THREAD_SLEEP_TIME_MILLIS = 1000;
    private static final int THREAD_STOP_WAIT_TIMEOUT_MILLIS = 10000;

    private Thread thread;
    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private volatile boolean stopRequested = false;
    private boolean firstLockAttempt = true;

    public HL7ChannelProcessor(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public void start() {
        LOG.info("Starting channel processor {}", dbChannel.getChannelName());

        if (thread == null) {
            thread = new Thread(this);
            thread.setName(dbChannel.getChannelName() + "-HL7ChannelProcessor");
        }

        thread.start();
    }

    public void stop() {
        stopRequested = true;
        try {
            LOG.info("Stopping channel processor {}", dbChannel.getChannelName());
            thread.join(THREAD_STOP_WAIT_TIMEOUT_MILLIS);
        } catch (Exception e) {
            LOG.error("Error stopping channel processor for channel", e);
        }
    }

    @Override
    public void run() {
        boolean gotLock = false;
        LocalDateTime lastLockTriedTime = null;

        try {
            while (!stopRequested) {

                gotLock = getLock(gotLock);
                lastLockTriedTime = LocalDateTime.now();

                while ((!stopRequested) && (lastLockTriedTime.plusSeconds(LOCK_RECLAIM_INTERVAL_SECONDS).isAfter(LocalDateTime.now()))) {

                    if (gotLock) {

                        DbMessage message = getNextMessage();

                        if (stopRequested)
                            return;

                        if (message == null) {
                            Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                            continue;
                        }

                        if (!processMessage(message)) {

                            if (stopRequested)
                                return;

                            Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                        }

                    } else {  // not gotLock
                        Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                    }
                }
            }
        }
        catch (Exception e) {
            LOG.error("Fatal exception in channel processor {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }

        releaseLock(gotLock);
    }

    private boolean processMessage(DbMessage message) {
        Integer attemptId = setMessageProcessingStarted(message.getMessageId(), configuration.getInstanceId());

        if (attemptId == null)
            return false;

        try {
            HL7MessageProcessor messageProcessor = new HL7MessageProcessor(configuration, dbChannel,
                    (contentType, content) -> dataLayer.addMessageProcessingContent(message.getMessageId(), attemptId, contentType, content),
                    new Mapper(this.dbChannel.getChannelId(), this.dbChannel.getSendingApplication(), this.dataLayer)
            );

            if (messageProcessor.processMessage(message))
                return setMessageProcessingSuccess(message.getMessageId(), attemptId);

        } catch (HL7MessageProcessorException e) {
            setMessageProcessingFailure(message.getMessageId(), attemptId, e.getMessageStatus(), e);
        }

        return false;
    }

    private Integer setMessageProcessingStarted(int messageId, int instanceId) {
        try {
            return dataLayer.setMessageProcessingStarted(messageId, instanceId);
        } catch (Exception e) {
            LOG.error("Error setting message processing started for message id {} in channel processor {} for instance {}", new Object[] { messageId, dbChannel.getChannelName(), configuration.getInstanceId(), e });
            return null;
        }
    }

    private boolean setMessageProcessingSuccess(int messageId, int attemptId) {
        try {
            dataLayer.setMessageProcessingSuccess(messageId, attemptId, configuration.getInstanceId());
            return true;
        } catch (Exception e) {
            LOG.error("Error setting message processing success for message id {} in channel processor {} for instance {}", new Object[] { messageId, dbChannel.getChannelName(), configuration.getInstanceId(), e });
            return false;
        }
    }

    private void setMessageProcessingFailure(int messageId, int attemptId, DbMessageStatus dbMessageStatus, Exception exception) {
        try {
            String exceptionMessage = HL7ExceptionHandler.constructFormattedException(exception);

            if (StringUtils.isBlank(exceptionMessage))
                exceptionMessage = null;

            LOG.error("Error {} occurred while processing message {} in channel processor {} on instance {}", new Object[] { dbMessageStatus.name(), messageId, dbChannel.getChannelName(), configuration.getInstanceId(), exception });
            dataLayer.setMessageProcessingFailure(messageId, attemptId, dbMessageStatus, exceptionMessage, configuration.getInstanceId());

        } catch (Exception e) {
            LOG.error("Error adding message status {} for message id {} in channel processor {} for instance {}", new Object[] { dbMessageStatus.name(), messageId, dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }
    }

    private DbMessage getNextMessage() {
        try {
            return dataLayer.getNextUnprocessedMessage(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {
            LOG.error("Error getting next unprocessed message in channel processor {} for instance {} ", new Object[] { dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }

        return null;
    }

    private boolean getLock(boolean currentlyHaveLock) {
        try {
            boolean gotLock = dataLayer.getChannelProcessorLock(dbChannel.getChannelId(), configuration.getInstanceId(), LOCK_BREAK_OTHERS_SECONDS);

            if (firstLockAttempt || (currentlyHaveLock != gotLock))
                LOG.info((gotLock ? "G" : "Not g") + "ot lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            firstLockAttempt = false;

            return gotLock;
        } catch (Exception e) {
            LOG.error("Exception getting lock in channel processor for channel {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getMachineName(), e });
        }

        return false;
    }

    private void releaseLock(boolean currentlyHaveLock) {
        try {
            if (currentlyHaveLock)
                LOG.info("Releasing lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            dataLayer.releaseChannelProcessorLock(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {
            LOG.error("Exception releasing lock in channel processor for channel {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getMachineName(), e });
        }
    }
}
