package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.PostgresDataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

class HL7Channel {
    private static final Logger LOG = LoggerFactory.getLogger(HL7Channel.class);

    private HapiContext context;
    private HL7Service service;
    private DbChannel dbChannel;
    private Configuration configuration;
    private PostgresDataLayer dataLayer;
    private HL7ConnectionManager connectionManager;
    private HL7MessageReceiver messageReceiver;
    private HL7ExceptionHandler exceptionHandler;
    private HL7ChannelProcessor channelProcessor;

    private HL7Channel() {
    }

    public HL7Channel(DbChannel dbChannel, Configuration configuration) throws SQLException {
        Validate.notNull(dbChannel);
        Validate.notBlank(dbChannel.getChannelName());
        Validate.isTrue(dbChannel.getPortNumber() > 0);

        this.dbChannel = dbChannel;
        this.configuration = configuration;

        this.dataLayer = new PostgresDataLayer();

        context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        connectionManager = new HL7ConnectionManager(configuration, dbChannel);
        messageReceiver = new HL7MessageReceiver(configuration, dbChannel, connectionManager);
        exceptionHandler = new HL7ExceptionHandler(configuration, dbChannel, connectionManager);
        service = context.newServer(dbChannel.getPortNumber(), false);
        channelProcessor = new HL7ChannelProcessor(configuration, dbChannel);

        service.registerApplication("*", "*", messageReceiver);
        service.registerConnectionListener(connectionManager);
        service.setExceptionHandler(exceptionHandler);
    }

    public void start() throws InterruptedException {
        LOG.info("Starting channel {} on port {}", dbChannel.getChannelName(), dbChannel.getPortNumber());
        channelProcessor.start();
        service.startAndWait();
    }

    public void stop() {
        LOG.info("Stopping channel {} on port {}", dbChannel.getChannelName(), dbChannel.getPortNumber());
        channelProcessor.stop();
        connectionManager.closeConnections();
        service.stopAndWait();
    }
}
