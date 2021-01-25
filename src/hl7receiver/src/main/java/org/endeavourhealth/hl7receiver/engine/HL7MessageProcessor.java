package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.http.Header;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.mapping.Mapper;
import org.endeavourhealth.hl7receiver.model.db.*;
import org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException;
import org.endeavourhealth.hl7receiver.sender.EdsSender;
import org.endeavourhealth.hl7receiver.sender.EdsSenderHttpErrorResponseException;
import org.endeavourhealth.hl7receiver.sender.EdsSenderResponse;
import org.endeavourhealth.hl7transform.Hl7v2Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class HL7MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageProcessor.class);
    private static final int KEYCLOAK_REINITIALISATION_WINDOW_MINUTES = 10;
    private static LocalDateTime lastInitialisedKeycloak = LocalDateTime.MIN;

    private Configuration configuration;
    private DbChannel dbChannel;
    private HL7ContentSaver contentSaver;
    private Mapper mapper;

    public HL7MessageProcessor(Configuration configuration, DbChannel dbChannel, HL7ContentSaver contentSaver, Mapper mapper) {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.contentSaver = contentSaver;
        this.mapper = mapper;
    }

    public void processMessage(DbMessage dbMessage) throws HL7MessageProcessorException {
        // can this be split into generic mesage processing steps?

        try {
            String transformedMessage = null;

            try {
                transformedMessage = transformMessage(dbMessage);
                contentSaver.save(DbProcessingContentType.FHIR, transformedMessage);
            } catch (Exception e) {
                throw new HL7MessageProcessorException(DbMessageStatus.TRANSFORM_FAILURE, e);
            }

            String requestMessage = null;
            Date messageDateTime = null;

            try {
                requestMessage = buildEnvelope(dbMessage, transformedMessage);
                messageDateTime = java.sql.Timestamp.valueOf(dbMessage.getMessageDate()); //get the message date and convert to the object type we need
                contentSaver.save(DbProcessingContentType.ONWARD_REQUEST_MESSAGE, requestMessage);
            } catch (Exception e) {
                throw new HL7MessageProcessorException(DbMessageStatus.ENVELOPE_GENERATION_FAILURE, e);
            }

            String responseMessage = null;

            if (!skipMessageSending())
            {
                try {
                    initialiseKeycloak();

                    responseMessage = sendMessage(requestMessage, messageDateTime);
                    contentSaver.save(DbProcessingContentType.ONWARD_RESPONSE_MESSAGE, responseMessage);

                    MetricsHelper.recordEvent(dbChannel.getChannelName() + ".post-to-messaging-api-ok");

                } catch (Exception e) {

                    if (e instanceof EdsSenderHttpErrorResponseException) {
                        EdsSenderResponse edsSenderResponse = ((EdsSenderHttpErrorResponseException) e).getEdsSenderResponse();
                        responseMessage = getFormattedEdsSenderResponse(edsSenderResponse);
                        contentSaver.save(DbProcessingContentType.ONWARD_RESPONSE_MESSAGE, responseMessage);
                    }

                    MetricsHelper.recordEvent(dbChannel.getChannelName() + ".post-to-messaging-api-error");

                    throw new HL7MessageProcessorException(DbMessageStatus.SEND_FAILURE, e);
                }
            }

        } catch (HL7MessageProcessorException mpe) {
            throw mpe;

        } catch (Exception e) {
            throw new HL7MessageProcessorException(DbMessageStatus.UNEXPECTED_ERROR, e);
        }
    }

    private boolean skipMessageSending() {
        DbChannelOption channelOption = configuration.getChannelOption(dbChannel.getChannelId(), DbChannelOptionType.SKIP_ONWARD_MESSAGE_SENDING_IN_PROCESSOR);

        if (channelOption == null)
            return false;

        return ("TRUE".equals(channelOption.getChannelOptionValue()));
    }

    private String transformMessage(DbMessage dbMessage) throws Exception {
        return Hl7v2Transform.transform(dbMessage.getInboundPayload(), this.mapper);
    }

    private String sendMessage(String envelope, Date messageDateTime) throws IOException, EdsSenderHttpErrorResponseException {

        String edsUrl = configuration.getDbConfiguration().getDbEds().getEdsUrl();
        boolean useKeycloak = configuration.getDbConfiguration().getDbEds().isUseKeycloak();

        EdsSenderResponse edsSenderResponse = EdsSender.notifyEds(edsUrl, useKeycloak, envelope, messageDateTime);
        return getFormattedEdsSenderResponse(edsSenderResponse);
    }

    private static String getFormattedEdsSenderResponse(EdsSenderResponse edsSenderResponse) {
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

    private void initialiseKeycloak() throws HL7Exception {

        //tokens only last for 10 minutes in keycloak config
        if (!lastInitialisedKeycloak.plusMinutes(KEYCLOAK_REINITIALISATION_WINDOW_MINUTES).isBefore(LocalDateTime.now()))
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

            //} catch (IOException e) {
            } catch (Throwable e) { //had a class not found exception which is a throwable, so changed to catch throwable
                LOG.error("Error initialising keycloak", e);
                throw new HL7Exception("Error initialising keycloak", e);
            }

        } else {
            LOG.trace("Keycloak is not enabled");
        }

        lastInitialisedKeycloak = LocalDateTime.now();
    }
}
