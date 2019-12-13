package org.endeavourhealth.hl7receiver.engine;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.PostgresDataLayer;
import org.endeavourhealth.hl7receiver.mapping.Mapper;
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
    private PostgresDataLayer dataLayer;
    private Mapper mapper;
    private volatile boolean stopRequested = false;
    private boolean firstLockAttempt = true;

    public HL7ChannelProcessor(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new PostgresDataLayer();
        this.mapper = new Mapper(dbChannel.getSendingFacility(), dataLayer);
    }

    public void start() {
        LOG.info("Starting channel processor " + dbChannel.getChannelName());

        if (thread == null) {
            thread = new Thread(this);
            thread.setName(dbChannel.getChannelName() + "-HL7ChannelProcessor");
        }

        thread.start();
    }

    public void stop() {
        stopRequested = true;
        try {
            LOG.info("Stopping channel processor " + dbChannel.getChannelName());
            thread.join(THREAD_STOP_WAIT_TIMEOUT_MILLIS);
        } catch (Exception e) {
            LOG.error("Error stopping channel processor for channel", e);
        }
    }

    @Override
    public void run() {
        boolean gotLock = false;
        boolean isFirstRun = true;
        boolean isPaused = false;
        LocalDateTime lastLockTriedTime = null;

        LOG.trace("Starting processor runnable");

        try {

            while (!stopRequested) {
                LOG.trace("In main processor loop");

                gotLock = getLock(gotLock);
                lastLockTriedTime = LocalDateTime.now();
                LOG.trace("gotLock = " + gotLock);

                if (isFirstRun && gotLock) {
                    resetNextAttemptDateOnFailedMessages();
                }
                isFirstRun = false;

                while (!stopRequested
                        && (lastLockTriedTime.plusSeconds(LOCK_RECLAIM_INTERVAL_SECONDS).isAfter(LocalDateTime.now()))) {

                    if (gotLock) {

                        isPaused = getIsPaused(isPaused);

                        if (!isPaused) {

                            if (stopRequested) {
                                return;
                            }

                            DbMessage message = getNextMessage();
                            if (message == null) {
                                //LOG.trace("No next message");
                                Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                                continue;
                            }

                            long msStart = System.currentTimeMillis();
                            LOG.trace("Going to process message " + message.getMessageId());
                            if (!processMessage(message)) {
                                LOG.trace("Failed to process message " + message.getMessageId());

                                if (stopRequested) {
                                    return;
                                }

                                Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                            } else {
                                long msEnd = System.currentTimeMillis();
                                long msTaken = msEnd - msStart;
                                LOG.trace("Successfully processed message " + message.getMessageId() + " in " + msTaken + " ms");
                            }

                        } else {  // isPaused
                            LOG.trace("Is paused, so not processing messages");
                            Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                        }
                    } else {  // not gotLock
                        LOG.error("Not got processor lock");
                        Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                    }
                }
            }

        } catch (Throwable t) {
            LOG.error("Exception in main processor loop", t);

            //worried that this won't be spotted, so sending to Slack
            SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7Receiver, "Exception processing", t);
        }

        releaseLock(gotLock);
    }

    private boolean processMessage(DbMessage message) {
        Integer attemptId = setMessageProcessingStarted(message.getMessageId(), configuration.getInstanceId());

        if (attemptId == null)
            return false;

        try {
            HL7MessageProcessor messageProcessor = new HL7MessageProcessor(configuration,
                    dbChannel,
                    (contentType, content) -> dataLayer.addMessageProcessingContent(message.getMessageId(), attemptId, contentType, content),
                    this.mapper);

            if (messageProcessor.processMessage(message))
                return setMessageProcessingSuccess(message.getMessageId(), attemptId);

        } catch (HL7MessageProcessorException e) {
            setMessageProcessingFailure(message.getMessageId(), attemptId, e.getMessageStatus(), e);

            if (attemptId.intValue() == 1)
                sendSlackNotification(message, e);
        }

        return false;
    }

    private Integer setMessageProcessingStarted(int messageId, int instanceId) {
        try {
            return dataLayer.setMessageProcessingStarted(messageId, instanceId);

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    messageId,
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Error setting message processing started for message id {} in channel processor {} for instance {}", logArgs);
            return null;
        }
    }

    private boolean setMessageProcessingSuccess(int messageId, int attemptId) {
        try {
            dataLayer.setMessageProcessingSuccess(messageId, attemptId, configuration.getInstanceId());
            return true;

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    messageId,
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Error setting message processing success for message id {} in channel processor {} for instance {}", logArgs);
            return false;
        }
    }

    private void setMessageProcessingFailure(int messageId, int attemptId, DbMessageStatus dbMessageStatus, Exception exception) {
        try {
            String exceptionMessage = HL7ExceptionHandler.constructFormattedException(exception);

            if (StringUtils.isBlank(exceptionMessage))
                exceptionMessage = null;

            Object[] logArgs = new Object[] {
                    dbMessageStatus.name(),
                    messageId,
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    exception };

            LOG.error("Error {} occurred while processing message {} in channel processor {} on instance {}", logArgs);

            dataLayer.setMessageProcessingFailure(messageId, attemptId, dbMessageStatus, exceptionMessage, configuration.getInstanceId());

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    dbMessageStatus.name(),
                    messageId,
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Error adding message status {} for message id {} in channel processor {} for instance {}", logArgs);
        }
    }

    private void sendSlackNotification(DbMessage dbMessage, Exception exception) {
        String messageType = dbMessage.getInboundMessageType();
        String messageControlId = dbMessage.getMessageControlId();
        int messageId = dbMessage.getMessageId();
        String channelName = dbChannel.getChannelName();
        String instanceName = configuration.getMachineName();

        String message = "Error processing " + messageType + " message " + messageControlId + " (DBID " + Integer.toString(messageId) + ") on channel " + channelName + " on instance " + instanceName;

        //changing to use the standard Slack utility, so all Slack config is stored in the same place
        /*String exceptionMessage = HL7ExceptionHandler.constructFormattedException(exception);
        SlackNotifier slackNotifier = new SlackNotifier(configuration, dbChannel.getChannelId());
        slackNotifier.postMessage(message, exceptionMessage);*/

        SlackHelper.Channel channel = null;
        if (dbChannel.getChannelId() == 1) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsHomerton;

        } else if (dbChannel.getChannelId() == 2) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsBarts;

        } else {
            LOG.error("Unknown channel ID " + dbChannel.getChannelId());
        }

        SlackHelper.sendSlackMessage(channel, message, exception);
    }

    private DbMessage getNextMessage() {
        try {
            return dataLayer.getNextUnprocessedMessage(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {

            Object[] logArgs = new Object[] {
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Error getting next unprocessed message in channel processor {} for instance {} ", logArgs);
        }

        return null;
    }

    private boolean getIsPaused(boolean isCurrentlyPaused) {
        try {
            String isPausedString = dataLayer.getChannelOption(dbChannel.getChannelId(), DbChannelOptionType.PAUSE_PROCESSOR);

            boolean isPaused = DbChannelOptionType.isChannelOptionValueTrue(isPausedString);

            final String message = "Channel processor {} on channel {} on instance {}";

            if ((!isCurrentlyPaused) && (isPaused))
                LOG.info(message, "PAUSED", dbChannel.getChannelName(), configuration.getMachineName());
            else if ((isCurrentlyPaused) && (!isPaused))
                LOG.info(message, "RESUMED", dbChannel.getChannelName(), configuration.getMachineName());

            return isPaused;

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Exception getting paused status in channel processor for channel {} for instance {}", logArgs);
        }

        return false;
    }

    private void resetNextAttemptDateOnFailedMessages() {
        try {
            long messagesResetCount = dataLayer.reprocessFailedMessages(dbChannel.getChannelId(), configuration.getInstanceId());

            if (messagesResetCount > 0) {
                LOG.info("Reset next attempt date of " + messagesResetCount + " message(s) in error for " + dbChannel.getChannelName());
            }

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Exception resetting next attempt date on failed messages on channel {} on instance {}", logArgs);
        }
    }

    private boolean getLock(boolean currentlyHaveLock) {
        try {
            boolean gotLock = dataLayer.getChannelProcessorLock(dbChannel.getChannelId(), configuration.getInstanceId(), LOCK_BREAK_OTHERS_SECONDS);

            if (firstLockAttempt || (currentlyHaveLock != gotLock))
                LOG.info((gotLock ? "G" : "Not g") + "ot lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            firstLockAttempt = false;

            return gotLock;

        } catch (Exception e) {
            Object[] logArgs = new Object[] {
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Exception getting lock in channel processor on channel {} for instance {}", logArgs);
        }

        return false;
    }

    private void releaseLock(boolean currentlyHaveLock) {
        try {
            if (currentlyHaveLock)
                LOG.info("Releasing lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            dataLayer.releaseChannelProcessorLock(dbChannel.getChannelId(), configuration.getInstanceId());

        } catch (Exception e) {
            Object logArgs = new Object[] {
                    dbChannel.getChannelName(),
                    configuration.getMachineName(),
                    e };

            LOG.error("Exception releasing lock in channel processor for channel {} for instance {}", logArgs);
        }
    }
}
