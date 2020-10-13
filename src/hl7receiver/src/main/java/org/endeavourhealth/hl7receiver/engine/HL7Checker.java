package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v23.datatype.CX;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ADT_A44_PATIENT;
import ca.uhn.hl7v2.model.v23.segment.MRG;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.hl7receiver.model.db.DbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HL7Checker {
    private static final Logger LOG = LoggerFactory.getLogger(HL7Checker.class);

    private static Map<Integer, Integer> previousMessagesInError = new HashMap<>(); //channel ID and message ID of last known error

    /**
     * utility to check the HL7 Receiver database and move any blocking messages to the Dead Letter Queue
     * returns true if the error was handled OK and moved out of the way, false otherwise
     */
    public static boolean checkIfErrorCanBeHandled(int messageId, int channelId) {
        try {
            LOG.info("Starting HL7 Check for message " + messageId + " on channel " + channelId);
            boolean handledOk = checkNewErrorImpl(messageId, channelId);
            LOG.info("Completed HL7 Check for message " + messageId + " on channel " + channelId);
            return handledOk;

        } catch (Throwable t) {
            LOG.error("", t);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7Receiver, "Exception in HL7 Checking", t);
            return false;
        }
    }

    private static boolean checkNewErrorImpl(int messageId, int channelId) throws Exception {

        Wrapper err = findError(messageId, channelId);
        if (err == null) {
            throw new Exception("Expecting an error for message ID " + messageId + " and channel " + channelId);
        }

        //auto-closable
        try (HapiContext context = new DefaultHapiContext()) {

            context.setValidationContext(new NoValidation());
            Parser parser = context.getGenericParser();

            String ignoreReason = shouldIgnore(parser, channelId, err.getMessageType(), err.getInboundPayload(), err.getErrorMessage());
            if (!Strings.isNullOrEmpty(ignoreReason)) {

                //if we have a non-null reason, move to the DLQ
                moveToDlq(messageId, ignoreReason);

                //and notify Slack that we've done so
                sendDlqAuditSlackMessage(channelId, messageId, ignoreReason, err.getLocalPatientId());

                return true;

            } else {
                //can't automatically handle it
                return false;
            }
        }
    }


    private static void writeState(Map<Integer, Integer> currentMessages, String stateFile) throws Exception {

        //LOG.info("Writing state to " + stateFile);

        List<String> lines = new ArrayList<>();
        for (Integer channelId: currentMessages.keySet()) {
            Integer messageId = currentMessages.get(channelId);
            String line = "" + channelId + ":" + messageId;
            lines.add(line);
            //LOG.info("Writing line: " + line);
        }

        File f = new File(stateFile);
        FileUtils.writeLines(f, null, lines);

        //LOG.info("Written");
    }

    private static Map<Integer, Integer> readState(String stateFile) throws Exception {
        //LOG.info("Reading state file from " + stateFile);

        Map<Integer, Integer> ret = new HashMap<>();

        File f = new File(stateFile);
        if (f.exists()) {
            //LOG.info("File exists");
            List<String> lines = FileUtils.readLines(f, null);
            for (String line: lines) {
                //LOG.info("Line: " + line);
                String[] toks = line.split(":");
                String channelId = toks[0];
                String messageId = toks[1];
                ret.put(Integer.valueOf(channelId), Integer.valueOf(messageId));
            }
        }

        //LOG.info("Read from file into map size " + ret.size());

        return ret;
    }

    private static String getChannelName(int channelId) {

        if (channelId == 1) {
            return "Homerton";

        } else if (channelId == 2) {
            return "Barts";

        } else {
            throw new RuntimeException("Unknown channel " + channelId);
        }
    }

    private static SlackHelper.Channel getSlackAuditChannel(int channelId) {

        if (channelId == 1) {
            return SlackHelper.Channel.Hl7ReceiverAlertsHomerton;

        } else if (channelId == 2) {
            return SlackHelper.Channel.Hl7ReceiverAlertsBarts;

        } else {
            throw new RuntimeException("Unknown channel " + channelId);
        }
    }

    /**
     * sends a Slack message to say we've moved the message to the DLQ - goes to the hospital-specific channels
     */
    private static void sendDlqAuditSlackMessage(int channelId, int messageId, String ignoreReason, String localPatientId) throws Exception {
        String msg = "HL7 Checker moved message ID " + messageId + " (PatientId=" + localPatientId + "):\r\n" + ignoreReason;
        SlackHelper.sendSlackMessage(getSlackAuditChannel(channelId), msg);
    }

    /**
     * sends a Slack message to say we've had an error - goes to the hospital-specific channels
     */
    public static void sendErrorAuditMessage(int channelId, DbMessage dbMessage, Exception exception) {
        String messageType = dbMessage.getInboundMessageType();
        String messageControlId = dbMessage.getMessageControlId();
        int messageId = dbMessage.getMessageId();

        String msg = "Error processing " + messageType + " message " + messageControlId + " (DBID " + Integer.toString(messageId) + ")";
        SlackHelper.sendSlackMessage(getSlackAuditChannel(channelId), msg, exception);
    }


    private static String shouldIgnore(Parser parser, int channelId, String messageType, String inboundPayload, String errorMessage) throws HL7Exception {
        LOG.info("Checking auto-DLQ rules");
        LOG.info("channelId:" + channelId);
        LOG.info("messageType:" + messageType);
        LOG.info("errorMessage:" + errorMessage);
        LOG.info("inboundPayload:" + inboundPayload);

        // *************************************************************************************************************************************************
        // Rules for Homerton
        // *************************************************************************************************************************************************
        if (channelId == 1
                && messageType.equals("ADT^A44")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String mergeEpisodeId = terser.get("/PATIENT/MRG-5");
            LOG.info("mergeEpisodeId:" + mergeEpisodeId);

            // If merge episodeId is missing then move to DLQ
            if (Strings.isNullOrEmpty(mergeEpisodeId)) {
                return "Automatically moved A44 because of missing episode ID";
            }

            String mergeEpisodeIdSource = terser.get("/PATIENT/MRG-5-4");
            LOG.info("mergeEpisodeIdSource:" + mergeEpisodeIdSource);

            // If merge episodeId is from Newham then move to DLQ
            if (mergeEpisodeIdSource != null && mergeEpisodeIdSource.toUpperCase().startsWith("NEWHAM")) {
                return "Automatically moved A44 because of merging Newham episode";
            }
        }


        if (channelId == 1
                && messageType.equals("ADT^A44")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  patientIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String mergePatientId = terser.get("/PATIENT/MRG-1");
            LOG.info("mergePatientId:" + mergePatientId);

            // If merge patientId is missing then move to DLQ
            if (Strings.isNullOrEmpty(mergePatientId)) {
                return "Automatically moved A44 because of missing MRG patientId ID";
            }
        }

        // Added 2017-10-24
        if (channelId == 1
                && (messageType.startsWith("ADT^"))
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.IllegalArgumentException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String episodeId = terser.get("/PV1-19");
            LOG.info("episodeId:" + episodeId);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(episodeId)) {
                return "Automatically moved ADT because of missing PV1:19";
            }

            String finNo = terser.get("/PID-18-1");
            LOG.info("finNo:" + finNo);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(finNo)) {
                return "Automatically moved ADT because of missing PID18.1 (FIN No)";
            }
        }

        if (channelId == 1
                && messageType.startsWith("ADT^")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String finNoType = terser.get("/PID-18-4");
            LOG.info("finNoType:" + finNoType);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(finNoType)
                    || finNoType.compareToIgnoreCase("Newham FIN") == 0) {
                return "Automatically moved ADT because PID18.4 (FIN No Type) indicates Newham";
            }
        }

        // Added 2017-11-08
        if (channelId == 1
                && messageType.equals("ADT^A34")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A34 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2017-11-10
        if (channelId == 1
                && messageType.equals("ADT^A35")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A35 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2018-01-10
        if (channelId == 1
                && messageType.startsWith("ADT^")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  Hospital servicing facility of NEWHAM GENERAL not recognised")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);
            String servicingFacility = terser.get("/PV1-39");
            LOG.info("servicingFacility(PV1:39):" + servicingFacility);

            // If "NEWHAM GENERAL" then move to DLQ
            if (servicingFacility.compareToIgnoreCase("NEWHAM GENERAL") == 0) {
                return "Automatically moved ADT because servicing facility is NEWHAM GENERAL in Homerton channel";
            }
        }

        // Added 2018-01-11
        if (channelId == 1
                && messageType.startsWith("ADT^")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  patientIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            //Terser terser = new Terser(hapiMsg);
            //String cnn = terser.get("/PID-3");
            //LOG.info("PID:3(looking for CNN):" + cnn);

            boolean cnnFound = false;
            PID pid = (PID) hapiMsg.get("PID");
            if (pid != null) {
                CX[] pid3s = pid.getPid3_PatientIDInternalID();
                if (pid3s != null) {
                    for (int i = 0; i < pid3s.length; i++) {
                        LOG.info("PID:3(" + i + "):" + pid3s[i].toString());
                        if (pid3s[i].toString().indexOf("CNN") == -1) {
                            LOG.info("CNN NOT FOUND");
                        } else {
                            LOG.info("CNN FOUND");
                            cnnFound = true;
                        }
                    }
                }
            }

            // If "CNN" not found then move to DLQ
            if (cnnFound == false) {
                return "Automatically moved ADT because PID:3 does not contain CNN";
            }
        }

        // Added 2019-10-07
        if (channelId == 1
                && messageType.equals("ADT^A03")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.IllegalArgumentException]  patientIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);

            boolean cnnValueFound = false;
            PID pid = (PID) hapiMsg.get("PID");
            if (pid != null) {
                CX[] pid3s = pid.getPid3_PatientIDInternalID();
                if (pid3s != null) {
                    for (int i = 0; i < pid3s.length; i++) {
                        LOG.info("PID:3(" + i + "):" + pid3s[i].toString());
                        if (pid3s[i].toString().contains("CNN")) {

                            ST id = pid3s[i].getID();
                            if (id.isEmpty()) {
                                LOG.info("CNN Value Missing");
                                cnnValueFound = false;
                            } else {
                                cnnValueFound = true;
                            }
                        }
                    }
                }
            }

            // If "CNN" is blank then move to DLQ
            if (cnnValueFound == false) {
                return "Automatically moved ADT because PID:3 contains a blank CNN";
            }
        }

        // *************************************************************************************************************************************************
        // Rules for Barts
        // *************************************************************************************************************************************************
        // Added 2018-01-10
        if (channelId == 2
                && messageType.startsWith("ADT^")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  More than one patient primary care provider")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);
            String gpId = terser.get("/PD1-4(1)-1");
            LOG.info("GP(2):" + gpId);

            // If multiple GPs then move to DLQ
            if (!Strings.isNullOrEmpty(gpId)) {
                return "Automatically moved ADT because of multiple GPs in PD1:4";
            }
        }

        if (channelId == 2
                && messageType.equals("ADT^A31")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  Could not create organisation ")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);
            String gpPracticeId = terser.get("/PD1-3-3");
            LOG.info("Practice:" + gpPracticeId);

            // If practice id is missing or numeric then move to DLQ
            if (Strings.isNullOrEmpty(gpPracticeId)
                    || StringUtils.isNumeric(gpPracticeId)) {
                return "Automatically moved A31 because of invalid practice code";
            }
        }

        // Added 2017-11-07
        if (channelId == 2
                && messageType.startsWith("ADT^")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String episodeId = terser.get("/PV1-19");
            LOG.info("episodeId:" + episodeId);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(episodeId)) {
                return "Automatically moved ADT because of missing PV1:19";
            }

            String episodeIdType = terser.get("/PV1-19-5");
            LOG.info("episodeIdType:" + episodeIdType);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(episodeIdType)) {
                return "Automatically moved ADT because of missing PV1:19.5 - expecting VISITID";
            }
        }

        // Added 2017-11-08
        if (channelId == 2
                && messageType.equals("ADT^A34")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A34 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2017-11-08
        if (channelId == 2
                && messageType.equals("ADT^A35")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A35 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2018-03-30
        if (channelId == 2
                && messageType.startsWith("ADT^A44")
                && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  patientIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            //Terser terser = new Terser(hapiMsg);
            //String cnn = terser.get("/PID-3");
            //LOG.info("PID:3(looking for CNN):" + cnn);
            try {
                boolean MRNFound = false;
                ADT_A44_PATIENT group = (ADT_A44_PATIENT) hapiMsg.get("PATIENT");
                PID pid = (PID) group.get("PID");
                //PID pid = (PID) hapiMsg.get("/PATIENT/PID");
                if (pid != null) {
                    CX[] pid3s = pid.getPid3_PatientIDInternalID();
                    if (pid3s != null && pid3s.length > 0) {
                        MRNFound = true;
                /* This check might need to be more specific than just 'PID:3 present'
                for (int i = 0; i < pid3s.length; i++) {
                    LOG.info("PID:3(" + i + "):" + pid3s[i].toString());
                    if (pid3s[i].toString().indexOf("CNN") == -1) {
                        LOG.info("CNN NOT FOUND");
                    } else {
                        LOG.info("CNN FOUND");
                        MRNFound = true;
                    }
                }*/
                    }
                }

                // If "CNN" not found then move to DLQ
                if (MRNFound == false) {
                    return "Automatically moved ADT because PID:3 does not contain MRN";
                }
            }
            catch (Exception ex) {
                LOG.info("Error:" + ex.getMessage());
                LOG.info("Error:" + hapiMsg.printStructure());
            }

        }

        // Added 2019-04-30
        if (channelId == 2
                && messageType.equals("ADT^A34")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n" +
                "[org.endeavourhealth.hl7transform.common.TransformException]  EVN segment exists less than 1 time(s)")) {

            LOG.info("Looking for EVN segment");
            Message hapiMsg = parser.parse(inboundPayload);
            Structure evn = hapiMsg.get("EVN");

            // If MRG missing
            if (evn == null || evn.isEmpty()) {
                return "Automatically moved A34 because of missing EVN";
            } else {
                LOG.info("EVN segment found. isEmpty()=" + evn.isEmpty());
            }
        }


        //return null to indicate we don't ignore it
        return null;
    }

    /*
     * calls the fn to bump the message to the DLQ and pushes the retry timer forwards so we don't end
     * up waiting for ages to check again
     */
    private static void moveToDlq(int messageId, String reason) throws Exception {

        LOG.debug("Moving " + messageId + " to DLQ becaue: " + reason);

        Connection connection = ConnectionManager.getHl7ReceiverConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            //although it looks like a select, it's just invoking a function which performs an update
            String sql = "SELECT helper.move_message_to_dead_letter(" + messageId + ", '" + reason + "');";
            statement.execute(sql);

            //not sure what this was trying to be before, since the above fn deletes the
            //message from the log.message table
            /*sql = "UPDATE log.message"
                    + " SET next_attempt_date = now() - interval '1 hour'"
                    + " WHERE message_id = " + messageId + ";";
            statement.execute(sql);*/

            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (statement != null) {
                statement.close();
            }
            connection.close();
        }
    }

    private static Wrapper findError(int messageId, int channelId) throws Exception {

        Connection connection = ConnectionManager.getHl7ReceiverConnection();
        PreparedStatement ps = null;

        try {
            String sql = "SELECT inbound_message_type, inbound_payload, error_message, pid2"
                    + " FROM log.message"
                    + " WHERE error_message is not null"
                    + " AND channel_id = ?"
                    + " AND message_id = ?";
            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setInt(col++, channelId);
            ps.setInt(col++, messageId);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                col = 1;
                String messageType = resultSet.getString(col++);
                String inboundPayload = resultSet.getString(col++);
                String errorMessage = resultSet.getString(col++);
                String localPatientId = resultSet.getString(col++);

                Wrapper ret = new Wrapper();
                ret.setMessageType(messageType);
                ret.setInboundPayload(inboundPayload);
                ret.setErrorMessage(errorMessage);
                ret.setLocalPatientId(localPatientId);
                return ret;

            } else {
                return null;
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * when a message is successfully transformed, if we were previously stuck, then
     * send an "all clear" Slack message
     */
    public static void sendAllClearMessage(int channelId) {

        Integer previousStuckMessage = previousMessagesInError.get(new Integer(channelId));
        if (previousStuckMessage == null) {
            return;
        }

        String msg = getChannelName(channelId) + " HL7 feed is no longer stuck on message " + previousStuckMessage;
        SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7Receiver, msg);

        previousMessagesInError.remove(new Integer(channelId));
    }

    /**
     * if we've had a transform failure and can't automatically deal with it, then and a Slack alert to
     * say we're stuck and need manual intervention
     */
    public static void sendStuckMessage(int messageId, int channelId) {

        //if we're still stuck on the same message as last time we attempted, then don't send the alert again
        Integer lastMessageIdInError = previousMessagesInError.get(new Integer(channelId));
        if (lastMessageIdInError != null
                && lastMessageIdInError.intValue() == messageId) {
            return;
        }

        //just get the error message from the DB, since it's formatted in a specific way
        String errorMessage = null;
        try {
            Wrapper err = findError(messageId, channelId);
            errorMessage = err.getErrorMessage();

        } catch (Exception ex) {
            LOG.error("Expecting an error for message ID " + messageId + " and channel " + channelId, ex);
            errorMessage = "Unknown_Error";
        }

        //only send the Slack alert the first time we detect we're stuck
        String msg = getChannelName(channelId) + " HL7 feed is now stuck on message " + messageId + "\n```" + errorMessage + "```";
        SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7Receiver, msg);

        //stick in the hash map so we don't send the same message again when we automatically have another attempt (and fail again)
        previousMessagesInError.put(new Integer(channelId), new Integer(messageId));
    }

    static class Wrapper {
        private String messageType = null;
        private String inboundPayload = null;
        private String errorMessage = null;
        private String localPatientId = null;

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getInboundPayload() {
            return inboundPayload;
        }

        public void setInboundPayload(String inboundPayload) {
            this.inboundPayload = inboundPayload;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getLocalPatientId() {
            return localPatientId;
        }

        public void setLocalPatientId(String localPatientId) {
            this.localPatientId = localPatientId;
        }
    }
}
