package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.endeavourhealth.common.eds.EdsSender;
import org.endeavourhealth.common.eds.EdsSenderHttpErrorResponseException;
import org.endeavourhealth.common.eds.EdsSenderResponse;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbEds;
import org.endeavourhealth.hl7receiver.model.db.DbMessage;
import org.endeavourhealth.hl7receiver.model.db.DbMessageStatusType;
import org.endeavourhealth.transform.hl7v2.Hl7v2Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HL7ChannelProcessor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelProcessor.class);
    private static final int LOCK_RECLAIM_INTERVAL_SECONDS = 60;
    private static final int LOCK_BREAK_OTHERS_SECONDS = 360;
    private static final int THREAD_SLEEP_TIME_MILLIS = 1000;
    private static final int THREAD_STOP_WAIT_TIMEOUT_MILLIS = 10000;
    private static final int KEYCLOAK_REINITIALISATION_WINDOW_HOURS = 1;
    private static final Integer[] DEFAULT_RETRY_INTERVALS_SECONDS = new Integer[] { 0, 10, 60 };

    private Thread thread;
    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private volatile boolean stopRequested = false;
    private boolean firstLockAttempt = true;
    private LocalDateTime lastInitialisedKeycloak = LocalDateTime.MIN;
    private List<Integer> attemptScheduleSeconds = null;

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
        DbMessage message = null;
        Integer attemptIntervalSeconds = getFirstAttemptIntervalSeconds();
        LocalDateTime lastAttemptTime = null;

        try {
            while (!stopRequested) {

                gotLock = getLock(gotLock);
                lastLockTriedTime = LocalDateTime.now();

                while ((!stopRequested) && (lastLockTriedTime.plusSeconds(LOCK_RECLAIM_INTERVAL_SECONDS).isAfter(LocalDateTime.now()))) {

                    if (gotLock) {

                        if (message == null) {
                            message = getNextMessage();
                            lastAttemptTime = LocalDateTime.now();
                        }

                        if (stopRequested)
                            return;

                        if (message != null) {

                            if (lastAttemptTime.plusSeconds(attemptIntervalSeconds).isBefore(LocalDateTime.now())) {

                                lastAttemptTime = LocalDateTime.now();
                                attemptIntervalSeconds = getNextAttemptIntervalSeconds(attemptIntervalSeconds);

                                boolean success = processMessage(message);

                                if (success) {
                                    message = null;
                                    attemptIntervalSeconds = getFirstAttemptIntervalSeconds();
                                }

                            } else { // next attempt time not yet
                                Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                            }
                        } else {  // messageToProcess == null
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

    private Integer getFirstAttemptIntervalSeconds() {
        return getAttemptScheduleSeconds().get(0);
    }

    private Integer getNextAttemptIntervalSeconds(Integer currentAttemptIntervalSeconds) {

        List<Integer> attemptSchedule = getAttemptScheduleSeconds();

        int currentIndex = getAttemptScheduleSeconds().indexOf(currentAttemptIntervalSeconds);

        if ((currentIndex + 1) < (attemptSchedule.size()))
            return getAttemptScheduleSeconds().get(currentIndex + 1);

        return currentAttemptIntervalSeconds;  // if the last interval, keep using that
    }

    private List<Integer> getAttemptScheduleSeconds() {
        if (attemptScheduleSeconds == null) {
            attemptScheduleSeconds = configuration
                    .getDbConfiguration()
                    .getDbProcessingAttemptIntervalsSeconds();

            if (attemptScheduleSeconds.size() == 0)
                attemptScheduleSeconds = Arrays.asList(DEFAULT_RETRY_INTERVALS_SECONDS);
        }

        return attemptScheduleSeconds;
    }

    private boolean processMessage(DbMessage dbMessage) {
        try {
            // can this be split into generic mesage processing steps?

            String transformedMessage = null;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // transform
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            try {
                transformedMessage = transformMessage(dbMessage);
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.TRANSFORMED, transformedMessage);

            } catch (Exception e) {
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.TRANSFORM_ERROR, null, e);
                LOG.error("Error transforming message - {}", e.getMessage());
                return false;
            }

            if (stopRequested)
                return false;

            String messageEnvelope = null;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // build envelope
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            try {
                messageEnvelope = buildEnvelope(dbMessage, transformedMessage);
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.NOTIFICATION_CREATED, messageEnvelope);

            } catch (Exception e) {
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.NOTIFICATION_CREATION_ERROR, null, e);
                LOG.error("Error building message envelope - {}", e.getMessage());
                return false;
            }

            if (stopRequested)
                return false;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // send envelope
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            try {
                initialiseKeycloak();

                if (stopRequested)
                    return false;

                String responseMessage = sendMessage(messageEnvelope);
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.NOTIFICATION_SENT, responseMessage);

            } catch (Exception e) {
                addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.NOTIFICATION_SEND_ERROR, e);
                LOG.error("Error sending message - {}", e.getMessage());
                return false;
            }

            return true;

        } catch (Exception e) {
            addMessageStatus(dbMessage.getMessageId(), DbMessageStatusType.UNEXPECTED_PROCESSING_ERROR, e);
            LOG.error("Unexpected processing error - {}", e.getMessage());
            return false;
        }
    }

    private String transformMessage(DbMessage dbMessage) throws Exception {
        return Hl7v2Transform.transform(dbMessage.getInboundPayload());
    }

    private void addMessageStatus(int messageId, DbMessageStatusType dbMessageStatusType, String messageStatusContent) {
        addMessageStatus(messageId, dbMessageStatusType, messageStatusContent,null);
    }

    private void addMessageStatus(int messageId, DbMessageStatusType dbMessageStatusType, Exception e) {
        addMessageStatus(messageId, dbMessageStatusType, null, e);
    }

    private void addMessageStatus(int messageId, DbMessageStatusType dbMessageStatusType, String messageStatusContent, Exception exception) {
        try {
            boolean inError = (exception != null);

            String exceptionMessage = HL7ExceptionHandler.constructFormattedException(exception);

            if (StringUtils.isBlank(exceptionMessage))
                exceptionMessage = null;

            dataLayer.addMessageStatus(messageId, configuration.getInstanceId(), dbMessageStatusType, messageStatusContent, inError, exceptionMessage);

        } catch (Exception e) {
            LOG.error("Error adding {} message status for message id {} in channel processor {} for instance {}", new Object[] { (true ? "SUCCESS" : "FAIL"), messageId, dbChannel.getChannelName(), configuration.getInstanceId(), e });
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

    private String sendMessage(String envelope) throws IOException, EdsSenderHttpErrorResponseException {

        String edsUrl = configuration.getDbConfiguration().getDbEds().getEdsUrl();
        boolean useKeycloak = configuration.getDbConfiguration().getDbEds().isUseKeycloak();

        EdsSenderResponse edsSenderResponse = EdsSender.notifyEds(edsUrl, useKeycloak, envelope);
        return edsSenderResponse.getStatusLine() + "\r\n" + edsSenderResponse.getResponseBody();
    }

    private String buildEnvelope(DbMessage dbMessage, String transformedMessage) throws IOException {

        UUID messageUuid = dbMessage.getMessageUuid();
        String organisationId = dbChannel.getEdsServiceIdentifier();
        String sourceSoftware = configuration.getDbConfiguration().getDbEds().getSoftwareContentType();
        String sourceSoftwareVersion = configuration.getDbConfiguration().getDbEds().getSoftwareVersion();
        String payload = transformedMessage;

        return EdsSender.buildEnvelope(messageUuid, organisationId, sourceSoftware, sourceSoftwareVersion, payload);
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
            LOG.error("Exception releasing lock in channel processor for channel {} for instance {}", new Object[] { e, dbChannel.getChannelName(), configuration.getMachineName() });
        }
    }

    private void initialiseKeycloak() throws HL7Exception {

        if (!this.lastInitialisedKeycloak.plusHours(KEYCLOAK_REINITIALISATION_WINDOW_HOURS).isBefore(LocalDateTime.now()))
            return;

        final DbEds dbEds = configuration.getDbConfiguration().getDbEds();

        if (dbEds.isUseKeycloak()) {
            LOG.trace("Initialising keycloak at: {}", dbEds.getKeycloakTokenUri());

            try {
                KeycloakClient.init(dbEds.getKeycloakTokenUri(),
                        dbEds.getKeycloakRealm(),
                        dbEds.getKeycloakUsername(),
                        dbEds.getKeycloakPassword(),
                        dbEds.getKeycloakClientId());

                Header response = KeycloakClient.instance().getAuthorizationHeader();

                LOG.trace("Keycloak initialised");

            } catch (IOException e) {
                LOG.error("Error initialising keycloak", e);
                throw new HL7Exception("Error initialising keycloak", e);
            }

        } else {
            LOG.trace("Keycloak is not enabled");
        }

        this.lastInitialisedKeycloak = LocalDateTime.now();
    }
}
