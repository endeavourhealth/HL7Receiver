package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.endeavourhealth.common.eds.EdsSender;
import org.endeavourhealth.common.eds.EdsSenderException;
import org.endeavourhealth.common.eds.EdsSenderResponse;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbEds;
import org.endeavourhealth.hl7receiver.model.db.DbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HL7ChannelForwarder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelForwarder.class);
    private static final int LOCK_RECLAIM_INTERVAL_SECONDS = 60;
    private static final int LOCK_BREAK_OTHERS_SECONDS = 360;
    private static final int THREAD_SLEEP_TIME_MILLIS = 1000;
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

    public HL7ChannelForwarder(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public void start() {
        LOG.info("Starting channel forwarder {}", dbChannel.getChannelName());

        if (thread == null) {
            thread = new Thread(this);
            thread.setName(dbChannel.getChannelName() + "-HL7ChannelForward");
        }

        thread.start();
    }

    public void stop() {
        stopRequested = true;
        try {
            LOG.info("Stopping channel forwarder {}", dbChannel.getChannelName());
            thread.join(10000);
        } catch (Exception e) {
            LOG.error("Error stopping channel forwarder for channel", e);
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
            LOG.error("Fatal exception in channel forwarder {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getInstanceId(), e });
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
                    .getDbNotificationAttemptIntervalsSeconds();

            if (attemptScheduleSeconds.size() == 0)
                attemptScheduleSeconds = Arrays.asList(DEFAULT_RETRY_INTERVALS_SECONDS);
        }

        return attemptScheduleSeconds;
    }

    private boolean processMessage(DbMessage dbMessage) {
        String requestNotification = null;
        String responseNotification = null;
        UUID messageUuid = dbMessage.getRequestMessageUuid();

        try {
            initialiseKeycloak();

            if (stopRequested)
                return false;

            requestNotification = buildEnvelope(dbMessage, messageUuid);

            if (stopRequested)
                return false;

            responseNotification = sendMessage(requestNotification);

            addNotificationStatus(dbMessage, messageUuid, requestNotification, responseNotification, true, null);

            return true;

        } catch (Exception e) {
            LOG.error("Error processing message id {} in channel forwarder {} for instance {}", new Object[] { dbMessage.getMessageId(), dbChannel.getChannelName(), configuration.getInstanceId(), e });

            addNotificationStatus(dbMessage, messageUuid, requestNotification, responseNotification, false, e);
        }

        return false;
    }

    private void addNotificationStatus(DbMessage dbMessage, UUID requestMessageUuid, String requestMessage, String responseMessage, boolean wasSuccess, Exception exception) {
        try {
            String exceptionMessage = HL7ExceptionHandler.constructFormattedException(exception);

            if (StringUtils.isBlank(exceptionMessage))
                exceptionMessage = null;

            dataLayer.addNotificationStatus(dbMessage.getMessageId(), wasSuccess, configuration.getInstanceId(), requestMessageUuid, requestMessage, responseMessage, exceptionMessage);

        } catch (Exception e) {
            LOG.error("Error adding {} notification status for message id {} in channel forwarder {} for instance {}", new Object[] { (true ? "SUCCESS" : "FAIL"), dbMessage.getMessageId(), dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }
    }

    private DbMessage getNextMessage() {
        try {
            return dataLayer.getNextUnnotifiedMessage(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {
            LOG.error("Error getting next unnotified message in channel forwarder {} for instance {} ", new Object[] { dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }

        return null;
    }

    private String sendMessage(String envelope) throws IOException, EdsSenderException {

        String edsUrl = configuration.getDbConfiguration().getDbEds().getEdsUrl();
        boolean useKeycloak = configuration.getDbConfiguration().getDbEds().isUseKeycloak();

        EdsSenderResponse edsSenderResponse = EdsSender.notifyEds(edsUrl, useKeycloak, envelope);
        return edsSenderResponse.getStatusLine() + "\r\n" + edsSenderResponse.getResponseBody();
    }

    private String buildEnvelope(DbMessage dbMessage, UUID messageUuid) throws IOException {

        String organisationId = dbChannel.getEdsServiceIdentifier();
        String sourceSoftware = configuration.getDbConfiguration().getDbEds().getSoftwareContentType();
        String sourceSoftwareVersion = configuration.getDbConfiguration().getDbEds().getSoftwareVersion();
        String payload = dbMessage.getInboundPayload();

        return EdsSender.buildEnvelope(messageUuid, organisationId, sourceSoftware, sourceSoftwareVersion, payload);
    }

    private boolean getLock(boolean currentlyHaveLock) {
        try {
            boolean gotLock = dataLayer.getChannelForwarderLock(dbChannel.getChannelId(), configuration.getInstanceId(), LOCK_BREAK_OTHERS_SECONDS);

            if (firstLockAttempt || (currentlyHaveLock != gotLock))
                LOG.info((gotLock ? "G" : "Not g") + "ot lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            firstLockAttempt = false;

            return gotLock;
        } catch (Exception e) {
            LOG.error("Exception getting lock in channel forwarder for channel {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getMachineName(), e });
        }

        return false;
    }

    private void releaseLock(boolean currentlyHaveLock) {
        try {
            if (currentlyHaveLock)
                LOG.info("Releasing lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            dataLayer.releaseChannelForwarderLock(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {
            LOG.error("Exception releasing lock in channel forwarder for channel {} for instance {}", new Object[] { e, dbChannel.getChannelName(), configuration.getMachineName() });
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
