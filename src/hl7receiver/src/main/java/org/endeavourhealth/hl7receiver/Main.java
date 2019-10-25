package org.endeavourhealth.hl7receiver;

import ch.qos.logback.classic.LoggerContext;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.hl7receiver.engine.HL7Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private static final String PROGRAM_DISPLAY_NAME = "HL7 Receiver";

	private static HL7Service hl7Service;

	public static void main(String[] args) {
		try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));

		    Configuration configuration = Configuration.getInstance();

		    LOG.info("--------------------------------------------------");
            LOG.info(PROGRAM_DISPLAY_NAME);
            LOG.info("--------------------------------------------------");

			hl7Service = new HL7Service(configuration);
            hl7Service.start();

            LOG.info("Started succesfully...");

        } catch (ConfigManagerException cme) {
            printToErrorConsole("Fatal exception occurred initializing ConfigManager", cme);
		    LOG.error("Fatal exception occurred initializing ConfigManager", cme);
            System.exit(-2);
        }
        catch (Exception e) {
			LOG.error("Fatal exception occurred", e);
			System.exit(-1);
		}
	}

	private static void shutdown() {
	    try {
            LOG.info("Shutting down...");

            if (hl7Service != null)
                hl7Service.stop();

	    } catch (Exception e) {
            printToErrorConsole("Exception occurred during shutdown", e);
	        LOG.error("Exception occurred during shutdown", e);
        }

        try {
	        LOG.info("Flushing logging system...");

	        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        } catch (Exception e) {
            printToErrorConsole("Exception occurred while flushing logging", e);
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            printToErrorConsole("Exception occurred during shutdown", e);
        }
    }

    private static void printToErrorConsole(String message, Exception e) {
        System.err.println(message + " [" + e.getClass().getName() + "] " + e.getMessage());
    }
}