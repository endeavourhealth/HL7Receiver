package org.endeavourhealth.hl7receiver.management;

import org.eclipse.jetty.server.Server;
import org.endeavourhealth.hl7receiver.Configuration;

public class ManagementService {

    private Configuration configuration;
    private Server server;

    public ManagementService(Configuration configuration) {
        this.configuration = configuration;
    }

    public void start() throws Exception {
        this.server = new Server(8100);
        this.server.setHandler(new ManagementHandler());
        this.server.start();
    }

    public void stop() throws Exception {
        this.server.stop();
    }
}
