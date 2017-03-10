package org.endeavourhealth.hl7receiver.engine;

import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HL7Service {

    private static final Logger LOG = LoggerFactory.getLogger(HL7Service.class);
    private Configuration configuration;
    private List<HL7Channel> channels;

    public HL7Service(Configuration configuration) throws SQLException {
        this.configuration = configuration;
        this.channels = new ArrayList<>();

        createChannels();
    }

    private void createChannels() throws SQLException {
        List<DbChannel> activeDbChannels = configuration
                .getDbConfiguration()
                .getDbChannels()
                .stream()
                .filter(t -> t.isActive())
                .collect(Collectors.toList());

        for (DbChannel dbChannel : activeDbChannels)
            channels.add(new HL7Channel(dbChannel, configuration));
    }

    public void start() throws InterruptedException {
        if (channels.size() == 0)
            LOG.info("No active channels to start");

        for (HL7Channel channel : channels)
            channel.start();
    }

    public void stop() {
        for (HL7Channel channel : channels)
            channel.stop();
    }
}
