package org.endeavourhealth.utilitymergedb;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.CX;
import ca.uhn.hl7v2.model.v23.segment.MRG;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.hl7receiver.models.ResourceId;
import org.endeavourhealth.core.database.dal.publisherTransform.PatientMergeDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static PatientMergeDalI dal;
    private static HikariDataSource connectionPool = null;
    private static HapiContext context;
    private static Parser parser;
    private static PreparedStatement resourceIdSelectStatement;

    /**
     * utility to check the HL7 Receiver database and save any merge inforamtion to new merge table
     *
     * Parameters:
     * <db_connection_url> <driver_class> <db_username> <db_password>
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("UtilityMergedbUpdater");

        PatientMergeDalI dal = DalProvider.factoryPatientMergeDal();

        if (args.length < 7) {
            LOG.error("Expecting four parameters:");
            LOG.error("<db_connection_url> <driver_class> <db_username> <db_password> <channel_id> <service_id> <last_message_id>");
            System.exit(0);
            return;
        }

        int msgCount = 0;
        int msgCountA34 = 0;
        int msgCountA35 = 0;
        int msgCountA44 = 0;
        int msgCountA34Saved = 0;
        int msgCountA35Saved = 0;
        int msgCountA44Saved = 0;
        String url = args[0];
        String driverClass = args[1];
        String user = args[2];
        String pass = args[3];
        String channelId = args[4];
        String serviceId = args[5];
        String startMessageId = args[6];

        //optional seventh parameter puts it in read only mode
        boolean readOnly = false;
        if (args.length > 7) {
            readOnly = Boolean.parseBoolean(args[7]);
        }
        LOG.info("url:" + url);
        LOG.info("driverClass:" + driverClass);
        LOG.info("user:" + user);
        LOG.info("pass(only length shown):" + pass.length());
        LOG.info("channelId:" + channelId);
        LOG.info("serviceId:" + serviceId);
        LOG.info("startMessageId:" + startMessageId);
        LOG.info("Read-only mode:" + readOnly);

        LOG.info("Starting HL7 Merge Check on " + url);

        try {
            openConnectionPool(url, driverClass, user, pass);

            context = new DefaultHapiContext();
            context.setValidationContext(new NoValidation());

            parser = context.getGenericParser();

            String primarysql = "SELECT message_id, inbound_message_type, inbound_payload, pid2 FROM log.message WHERE message_id > " + startMessageId + " and channel_id = '" + channelId + "' and is_complete = true and (inbound_message_type = 'ADT^A34' or inbound_message_type = 'ADT^A35' or inbound_message_type = 'ADT^A44') ORDER BY message_id asc;";
            LOG.info("SQL=" + primarysql);
            Connection connection = getConnection();
            ResultSet resultSet = executeQuery(connection, primarysql);

            resourceIdSelectStatement = connection.prepareStatement("SELECT resource_uuid FROM mapping.resource_uuid where scope_id=? and resource_type=? and unique_identifier=?");

            try {
                while (resultSet.next()) {
                    msgCount++;
                    String encounterDateTime = null;
                    String encounterDateTimeForUniqueKey = null;

                    String fromPatient = null;
                    String fromPatientUniquePatientKey = null;
                    String fromPatientResourceId = null;

                    String toPatientUniquePatientKey = null;
                    String toPatientResourceId = null;

                    String fromVisitId = null;
                    String fromEpisodeIdUniquekey = null;
                    String fromEpisodeIdResourceId = null;
                    String fromEncounterIdUniquekey = null;
                    String fromEncounterIdResourceId = null;

                    String toVisitId = null;
                    String toEpisodeIdUniquekey = null;
                    String toEpisodeIdResourceId = null;
                    String toEncounterIdUniqueKey = null;
                    String toEncounterIdResourceId = null;

                    int messageId = resultSet.getInt(1);
                    String messageType = resultSet.getString(2);
                    String inboundPayload = resultSet.getString(3);
                    String localPatientId = resultSet.getString(4);

                    Message hapiMsg = parser.parse(inboundPayload);
                    Terser terser = new Terser(hapiMsg);

                    //Extract encounterDateTime - ENV:2
                    encounterDateTime = terser.get("/EVN-2");
                    encounterDateTimeForUniqueKey = encounterDateTime.substring(0,4) + "/$H/" + encounterDateTime.substring(4, 6) + "/$H/" + encounterDateTime.substring(6, 8) + "T" + encounterDateTime.substring(8, 10) + ":" + encounterDateTime.substring(10, 12) + ":" + encounterDateTime.substring(12);
                    //LOG.info("encounterDateTimeForUniqueKey=" + encounterDateTimeForUniqueKey);

                    LOG.info("Found message " + messageType + " for patient " + localPatientId + " done on " + encounterDateTime + " - Message_id=" + messageId);

                    //Find resource_uuid for from patient
                    if (messageType.compareTo("ADT^A44") == 0) {
                        fromPatient = terser.get("/PATIENT/MRG-1-1");
                    } else {
                        fromPatient = terser.get("/MRG-1-1");
                    }
                    if (fromPatient != null && fromPatient.length() > 0) {
                        if (channelId.compareTo("1") == 0) {
                            fromPatientUniquePatientKey = "PatIdTypeCode=CNN-PatIdValue=" + fromPatient;
                            fromPatientResourceId = getResourceId("H", "Patient", fromPatientUniquePatientKey);
                        } else {
                            fromPatientUniquePatientKey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + fromPatient;
                            fromPatientResourceId = getResourceId("B", "Patient", fromPatientUniquePatientKey);
                        }
                    }

                    //Find resource_uuid for to patient
                    if (channelId.compareTo("1") == 0) {
                        toPatientUniquePatientKey = "PatIdTypeCode=CNN-PatIdValue=" + localPatientId;
                        toPatientResourceId = getResourceId("H", "Patient", toPatientUniquePatientKey);
                    } else {
                        toPatientUniquePatientKey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId;
                        toPatientResourceId = getResourceId("B", "Patient", toPatientUniquePatientKey);
                    }

                    //Find resource_uuid for from visit
                    if (messageType.compareTo("ADT^A44") == 0) {
                        fromVisitId = terser.get("/PATIENT/MRG-5");
                    } else {
                        fromVisitId = terser.get("/MRG-5");
                    }
                    if (fromVisitId != null && fromVisitId.length() > 0) {
                        if (channelId.compareTo("1") == 0) {
                            fromEpisodeIdUniquekey = "PatIdTypeCode=CNN-PatIdValue=" + fromPatient + "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + fromVisitId;
                            fromEpisodeIdResourceId = getResourceId("H", "EpisodeOfCare", fromEpisodeIdUniquekey);
                            fromEncounterIdUniquekey = "PatIdTypeCode=CNN-PatIdValue=" + fromPatient + "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + fromVisitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
                            fromEncounterIdResourceId = getResourceId("H", "Encounter", fromEncounterIdUniquekey);
                        } else {
                            fromEpisodeIdUniquekey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + fromPatient + "-EpIdTypeCode=VISITID-EpIdValue=" + fromVisitId;
                            fromEpisodeIdResourceId = getResourceId("B", "EpisodeOfCare", fromEpisodeIdUniquekey);
                            fromEncounterIdUniquekey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + fromPatient + "-EpIdTypeCode=VISITID-EpIdValue=" + fromVisitId;
                            fromEncounterIdResourceId = getResourceId("B", "Encounter", fromEncounterIdUniquekey);
                            if (fromEncounterIdResourceId == null) {
                                fromEncounterIdUniqueKey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + fromPatient + "-EpIdTypeCode=VISITID-EpIdValue=" + fromVisitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
                                fromEncounterIdResourceId = getResourceId("B", "Encounter", fromEncounterIdUniqueKey);
                            }
                        }
                    }

                    //Find resource_uuid for to visit - only used for ADT^A35
                    if (messageType.compareTo("ADT^A35") == 0) {
                        toVisitId = terser.get("/PV1-19");
                        if (toVisitId != null && toVisitId.length() > 0) {
                            if (channelId.compareTo("1") == 0) {
                                toEpisodeIdUniquekey = "PatIdTypeCode=CNN-PatIdValue=" + localPatientId+ "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + toVisitId;
                                toEpisodeIdResourceId = getResourceId("H", "EpisodeOfCare", toEpisodeIdUniquekey);
                                toEncounterIdUniqueKey = "PatIdTypeCode=CNN-PatIdValue=" + localPatientId + "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + toVisitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
                                toEncounterIdResourceId = getResourceId("H", "Encounter", toEncounterIdUniqueKey);
                            } else {
                                toEpisodeIdUniquekey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + toVisitId;
                                toEpisodeIdResourceId = getResourceId("B", "EpisodeOfCare", toEpisodeIdUniquekey);
                                toEncounterIdUniqueKey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + toVisitId;
                                toEncounterIdResourceId = getResourceId("B", "Encounter", toEncounterIdUniqueKey);
                                if (toEncounterIdResourceId == null) {
                                    toEncounterIdUniqueKey = "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + toVisitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
                                    toEncounterIdResourceId = getResourceId("B", "Encounter", toEncounterIdUniqueKey);
                                }
                            }
                        }
                    }

                    //Save merge/move record
                    if (messageType.compareTo("ADT^A34") == 0) {
                        msgCountA34++;
                        if (fromPatientResourceId == null) {
                            LOG.info("From-Patient ResourceId not found for key " + fromPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (toPatientResourceId == null) {
                            LOG.info("To-Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else {
                            msgCountA34Saved++;
                            LOG.info("Patient merge from " + fromPatient + " (" + fromPatientResourceId + ") to " + localPatientId + "(" + toPatientResourceId + ") on date " + encounterDateTime);
                            if (!readOnly) {
                                //dal.recordMerge(UUID.fromString(serviceId), UUID.fromString(fromPatientResourceId), UUID.fromString(toPatientResourceId));
                                // Save db entry
                            }
                        }
                    }
                    else if (messageType.compareTo("ADT^A35") == 0) {
                        msgCountA35++;
                        if (toPatientResourceId == null) {
                            LOG.info("Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (fromEncounterIdResourceId == null) {
                            LOG.info("From-Encounter ResourceId not found for key " + fromEncounterIdUniquekey + " in message_id " + messageId);
                        } else if (toEncounterIdResourceId == null) {
                            LOG.info("To-Encounter ResourceId not found for key " + toEncounterIdUniqueKey + " in message_id " + messageId);
                        } else {
                            msgCountA35Saved++;
                            LOG.info("Encounter merge for patient " + localPatientId + "(" + fromPatientResourceId + ") From visit " + fromVisitId + "(" + fromEpisodeIdResourceId + ") to visit " + toVisitId + "(" + toEpisodeIdResourceId + ") on date " + encounterDateTime);
                            if (!readOnly) {
                                // Save db entry
                            }
                        }
                    }
                    else if (messageType.compareTo("ADT^A44") == 0) {
                        msgCountA44++;
                        if (fromPatientResourceId == null) {
                            LOG.info("From-Patient ResourceId not found for key " + fromPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (toPatientResourceId == null) {
                            LOG.info("To-Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (fromEncounterIdResourceId == null) {
                            LOG.info("Encounter ResourceId not found for key " + fromEncounterIdUniquekey + " in message_id " + messageId);
                        } else {
                            msgCountA44Saved++;
                            LOG.info("Encounter move. From patient " + fromPatient + "(" + fromPatientResourceId + ") visitId " + fromVisitId + " To patient " + localPatientId + "(" + toPatientResourceId + ") on date " + encounterDateTime);
                            if (!readOnly) {
                                // Save db entry
                            }
                        }
                    }
                }

            } finally {
                resultSet.close();
                connection.close();

                LOG.info("MsgCount=" + msgCount);
                LOG.info("MsgCountA34=" + msgCountA34);
                LOG.info("MsgCountA35=" + msgCountA35);
                LOG.info("MsgCountA44=" + msgCountA44);
                LOG.info("MsgCountA34Saved=" + msgCountA34Saved);
                LOG.info("MsgCountA35Saved=" + msgCountA35Saved);
                LOG.info("MsgCountA44Saved=" + msgCountA44Saved);
            }

        } catch (Exception ex) {
            LOG.error("", ex);
            //although the error may be related to Homerton, just send to one channel for simplicity
            //SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7ReceiverAlertsBarts, "Exception in HL7 Checker", ex);
            System.exit(0);
        }

        LOG.info("Completed HL7 Merge Check on " + url);
        System.exit(0);
    }

    /*
     *
     */
    private static void openConnectionPool(String url, String driverClass, String username, String password) throws Exception {

        //force the driver to be loaded
        Class.forName(driverClass);

        HikariDataSource pool = new HikariDataSource();
        pool.setJdbcUrl(url);
        pool.setUsername(username);
        pool.setPassword(password);
        pool.setMaximumPoolSize(4);
        pool.setMinimumIdle(1);
        pool.setIdleTimeout(60000);
        pool.setPoolName("Hl7MergeCheckerPool" + url);
        pool.setAutoCommit(false);

        connectionPool = pool;

        //test getting a connection
        Connection conn = pool.getConnection();
        conn.close();
    }

    private static Connection getConnection() throws Exception {
        return connectionPool.getConnection();
    }

    private static void executeUpdate(String sql) throws Exception {

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
        } finally {
            connection.close();
        }
    }

    private static ResultSet executeQuery(Connection connection, String sql) throws Exception {

        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public static String getResourceId(String scope, String resourceType, String uniqueId) throws SQLException, ClassNotFoundException, IOException {
        String ret = null;

        resourceIdSelectStatement.setString(1, scope);
        resourceIdSelectStatement.setString(2, resourceType);
        resourceIdSelectStatement.setString(3, uniqueId);

        ResultSet rs = resourceIdSelectStatement.executeQuery();
        if (rs.next()) {
            ret = ((UUID) rs.getObject(1)).toString();
        }
        rs.close();

        return ret;
    }

}
