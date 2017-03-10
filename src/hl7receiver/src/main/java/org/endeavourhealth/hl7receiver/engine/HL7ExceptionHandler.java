package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

class HL7ExceptionHandler implements ReceivingApplicationExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HL7ExceptionHandler.class);

    private Configuration configuration;
    private HL7ConnectionManager connectionManager;
    private DbChannel dbChannel;
    private DataLayer dataLayer;

    public HL7ExceptionHandler(Configuration configuration, DbChannel dbChannel, HL7ConnectionManager connectionManager) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.connectionManager = connectionManager;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
        try {
            UUID deadLetterUuid = UUID.randomUUID();
            LOG.error("Exception while processing message", constructLogbackDeadLetterArgs(deadLetterUuid, exception));

            dataLayer.logDeadLetter(
                    configuration.getDbConfiguration().getInstanceId(),
                    dbChannel.getChannelId(),
                    connectionManager.getConnectionId(incomingMetadata),
                    configuration.getMachineName(),
                    dbChannel.getPortNumber(),
                    HL7ConnectionManager.getRemoteHost(incomingMetadata),
                    HL7ConnectionManager.getRemotePort(incomingMetadata),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    incomingMessage,
                    null,
                    outgoingMessage,
                    constructFormattedException(exception),
                    deadLetterUuid);
        } catch (Exception e3) {
            LOG.error("Error logging dead letter", e3);
        }

        if (outgoingMessage == null)
            outgoingMessage = "";

        return outgoingMessage;
    }

    static Object[] constructLogbackDeadLetterArgs(UUID deadLetterUuid, Exception exception) {
        return new Object[] { "DEAD-LETTER-UUID", deadLetterUuid, exception };
    }

    static String constructFormattedException(Throwable exception) {
        if (exception == null)
            return "";

        String message = "[" + exception.getClass().getName() + "]  " + exception.getMessage();

        if (exception.getCause() != null)
            if (exception.getCause() != exception)
                message += "\r\n" + constructFormattedException(exception.getCause());

        return message;
    }
}
