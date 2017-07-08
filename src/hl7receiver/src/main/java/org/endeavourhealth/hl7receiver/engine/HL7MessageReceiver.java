package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.exceptions.MessageProcessingException;
import org.endeavourhealth.hl7receiver.model.exceptions.TransientMessageProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

class HL7MessageReceiver implements ReceivingApplication {

    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageReceiver.class);
    private static final AcknowledgmentCode ACK_ERROR_IGNORE = AcknowledgmentCode.AE;
    private static final AcknowledgmentCode ACK_ERROR_RETRY = AcknowledgmentCode.AR;

    private Configuration configuration;
    private DbChannel dbChannel;
    private HL7ConnectionManager connectionManager;
    private DataLayer dataLayer;

    private HL7MessageReceiver() {
    }

    public HL7MessageReceiver(Configuration configuration, DbChannel dbChannel, HL7ConnectionManager connectionManager) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.connectionManager = connectionManager;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public Message processMessage(Message message, Map<String, Object> map) throws ReceivingApplicationException, HL7Exception {
        String messageText = null;
        Integer connectionId = null;
        HL7KeyFields hl7KeyFields = null;
        Message response = null;
        HL7KeyFields hl7KeyFieldsResponse = null;

        try {
            messageText = getMessageText(message);

            connectionId = connectionManager.getConnectionId(map);

            hl7KeyFields = HL7KeyFields.parse(messageText, dbChannel);

            if (connectionId == null)
                throw new TransientMessageProcessingException("Could not determine connection");

            if (!isMessageControlIdPresent(hl7KeyFields))
                throw new MessageProcessingException("Message control ID is empty");

            if (!areSenderAndRecipientIdentifiersMatching(hl7KeyFields))
                throw new MessageProcessingException("Sender and/or recipient identifiers do not match");

            if (!didMessageDateParseCorrectly(hl7KeyFields))
                throw new MessageProcessingException("Could not parse message date", hl7KeyFields.getMessageDateTimeParseException());

            if (!isMessageTypeAllowed(hl7KeyFields))
                throw new MessageProcessingException("Message type is not allowed");

            response = message.generateACK();
            String responseText = getMessageText(response);
            hl7KeyFieldsResponse = HL7KeyFields.parse(responseText, dbChannel);

            try {
                dataLayer.logMessage(
                        dbChannel.getChannelId(),
                        connectionId,
                        hl7KeyFields.getMessageControlId(),
                        hl7KeyFields.getSequenceNumber(),
                        hl7KeyFields.getMessageDateTime(),
                        hl7KeyFields.getPid1(),
                        hl7KeyFields.getPid2(),
                        hl7KeyFields.getMessageType(),
                        hl7KeyFields.getEncodedMessage(),
                        hl7KeyFieldsResponse.getMessageType(),
                        hl7KeyFieldsResponse.getEncodedMessage());
            } catch (Exception e) {
                throw new TransientMessageProcessingException("Error occurred writing to message log", e);
            }

            return response;

        } catch (Exception e1) {

            try {
                UUID deadLetterUuid = UUID.randomUUID();
                LOG.error("Exception while receiving message", HL7ExceptionHandler.constructLogbackDeadLetterArgs(deadLetterUuid, e1));

                Message negativeResponse = null;
                String negativeResponseText = null;
                HL7KeyFields negativeResponseKeyFields = null;
                AcknowledgmentCode acknowledgmentCode = ACK_ERROR_IGNORE;

                try {
                    if (e1.getClass().isAssignableFrom(TransientMessageProcessingException.class))
                        acknowledgmentCode = ACK_ERROR_RETRY;

                    LOG.error("Generating negative acknowledgement with error code " + acknowledgmentCode.getMessage());

                    negativeResponse = message.generateACK(acknowledgmentCode, new HL7Exception(e1.getMessage(), e1));
                    negativeResponseText = getMessageText(negativeResponse);
                    negativeResponseKeyFields = HL7KeyFields.parse(negativeResponseText, dbChannel);

                } catch (Exception e2) {
                    LOG.error("Error generating negative acknowledgement", e2);
                    acknowledgmentCode = null;
                }

                try {
                    dataLayer.logDeadLetter(
                            configuration.getDbConfiguration().getInstanceId(),
                            dbChannel.getChannelId(),
                            connectionId,
                            configuration.getMachineName(),
                            dbChannel.getPortNumber(),
                            HL7ConnectionManager.getRemoteHost(map),
                            HL7ConnectionManager.getRemotePort(map),
                            (hl7KeyFields == null ? null : hl7KeyFields.getSendingApplication()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getSendingFacility()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getReceivingApplication()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getReceivingFacility()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getMessageControlId()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getSequenceNumber()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getMessageDateTime()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getPid1()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getPid2()),
                            (hl7KeyFields == null ? null : hl7KeyFields.getMessageType()),
                            (hl7KeyFields == null ? messageText : hl7KeyFields.getEncodedMessage()),
                            (negativeResponseKeyFields == null ? null : negativeResponseKeyFields.getMessageType()),
                            (negativeResponseKeyFields == null ? null : negativeResponseKeyFields.getEncodedMessage()),
                            HL7ExceptionHandler.constructFormattedException(e1),
                            deadLetterUuid,
                            (acknowledgmentCode == null ? null : acknowledgmentCode.name()));
                } catch (Exception e3) {
                    LOG.error("Error logging dead letter", e3);
                    LOG.error("Inbound payload was: " + messageText + System.lineSeparator() + "Outbound payload was: " + negativeResponseText);

                    if (acknowledgmentCode.equals(ACK_ERROR_IGNORE)) {
                        acknowledgmentCode = ACK_ERROR_RETRY;

                        LOG.error("Because there was an error logging the dead letter, changing the negative acknowledgement response to error code " + acknowledgmentCode.getMessage());

                        try {
                            negativeResponse = message.generateACK(acknowledgmentCode, new HL7Exception(e1.getMessage(), e3));
                        } catch (Exception e4) {
                            LOG.error("Error generating negative acknowledgement", e4);
                        }
                    }
                }

                return negativeResponse;

            } catch (Exception e4) {
                LOG.error("Error handling exception", e4);
            }
        }

        return null;
    }

    private boolean isMessageControlIdPresent(HL7KeyFields hl7KeyFields) {
        return StringUtils.isNotEmpty(hl7KeyFields.getMessageControlId());
    }

    private boolean areSenderAndRecipientIdentifiersMatching(HL7KeyFields hl7KeyFields) {
        return ((dbChannel.getSendingApplication().equals(hl7KeyFields.getSendingApplication()))
            && (dbChannel.getSendingFacility().equals(hl7KeyFields.getSendingFacility()))
            && (dbChannel.getReceivingApplication().equals(hl7KeyFields.getReceivingApplication()))
            && (dbChannel.getReceivingFacility().equals(hl7KeyFields.getReceivingFacility())));
    }

    private boolean didMessageDateParseCorrectly(HL7KeyFields hl7KeyFields) {
        if (hl7KeyFields.getMessageDateTimeParseException() != null)
            return false;

        return (hl7KeyFields.getMessageDateTime() != null);
    }

    private boolean isMessageTypeAllowed(HL7KeyFields message) {
        return dbChannel
                .getDbChannelMessageTypes()
                .stream()
                .filter(t -> t.isAllowed())
                .anyMatch(t -> t.getMessageType().equals(message.getMessageType()));
    }

    private static String getMessageText(Message message) throws HL7Exception {
        return new DefaultHapiContext().getPipeParser().encode(message);
    }

    public boolean canProcess(Message message) {
        return true;
    }
}
