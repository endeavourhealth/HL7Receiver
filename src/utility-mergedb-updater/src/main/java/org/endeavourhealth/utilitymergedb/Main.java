package org.endeavourhealth.utilitymergedb;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import com.zaxxer.hikari.HikariDataSource;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.hl7receiver.Hl7ResourceIdDalI;
import org.endeavourhealth.core.database.dal.hl7receiver.models.ResourceId;
import org.endeavourhealth.core.database.dal.publisherTransform.ResourceMergeDalI;
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
    private static String RESOURCETYPE_ENCOUNTER = "Encounter";
    private static String RESOURCETYPE_PATIENT = "Patient";
    private static ResourceMergeDalI dalResourceMerge;
    private static Hl7ResourceIdDalI dalHL7ResourceId;
    private static HikariDataSource connectionPool = null;
    private static HapiContext context;
    private static Parser parser;
    private static PreparedStatement resourceIdSelectStatement;
    private static PreparedStatement resourceIdInsertStatement;

    /**
     * utility to check the HL7 Receiver database and save any merge inforamtion to new merge table
     *
     * Parameters:
     * <db_connection_url> <driver_class> <db_username> <db_password>
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("UtilityMergedbUpdater");

        dalResourceMerge = DalProvider.factoryResourceMergeDal();

        dalHL7ResourceId = DalProvider.factoryHL7ResourceDal();

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
        boolean verbose = Boolean.parseBoolean(System.getProperty("verbose", "false"));

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

        UUID globalserviceId = UUID.fromString(serviceId);

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
            resourceIdInsertStatement = connection.prepareStatement("insert into mapping.resource_uuid (scope_id, resource_type, unique_identifier, resource_uuid) values (?, ?, ?, ?)");

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
                    String fromEpisodeIdUniqueKey = null;
                    String fromEpisodeIdResourceId = null;
                    String fromEncounterIdUniqueKey = null;
                    String fromEncounterIdResourceId = null;

                    String toVisitId = null;
                    String toEpisodeIdUniqueKey = null;
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

                    LOG.info("********************************************************************************************************************************************************************");
                    LOG.info("Found message " + messageType + " for patient " + localPatientId + " performed on " + encounterDateTime + " - Message_id=" + messageId);

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
                        fromVisitId = terser.get("/PATIENT/MRG-5-1");
                    } else {
                        fromVisitId = terser.get("/MRG-5-1");
                    }
                    if (fromVisitId != null && fromVisitId.length() > 0) {
                        String useThisPatient = null;
                        if (messageType.compareTo("ADT^A35") == 0) {
                            useThisPatient = localPatientId;
                        } else {
                            useThisPatient = fromPatient;
                        }

                        if (channelId.compareTo("1") == 0) {
                            fromEpisodeIdUniqueKey = createEpisodeIdUniqueKey(channelId, useThisPatient, fromVisitId, encounterDateTimeForUniqueKey);
                            fromEpisodeIdResourceId = getResourceId("H", "EpisodeOfCare", fromEpisodeIdUniqueKey);
                            fromEncounterIdUniqueKey = createEncounterIdUniqueKey(channelId, useThisPatient, fromVisitId, encounterDateTimeForUniqueKey);
                            fromEncounterIdResourceId = getResourceId("H", "Encounter", fromEncounterIdUniqueKey);
                        } else {
                            fromEpisodeIdUniqueKey = createEpisodeIdUniqueKey(channelId, useThisPatient, fromVisitId, encounterDateTimeForUniqueKey);
                            fromEpisodeIdResourceId = getResourceId("B", "EpisodeOfCare", fromEpisodeIdUniqueKey);
                            fromEncounterIdUniqueKey = createEncounterIdUniqueKey(channelId, useThisPatient, fromVisitId, encounterDateTimeForUniqueKey);
                            fromEncounterIdResourceId = getResourceId("B", "Encounter", fromEncounterIdUniqueKey);
                            if (fromEncounterIdResourceId == null) {
                                LOG.info("From-Encounter ResourceId not found for key " + fromEncounterIdUniqueKey);
                                fromEncounterIdUniqueKey = createEncounterIdUniqueKeyOld(useThisPatient, fromVisitId, encounterDateTimeForUniqueKey);
                                fromEncounterIdResourceId = getResourceId("B", "Encounter", fromEncounterIdUniqueKey);
                            }
                        }
                    }

                    //Find resource_uuid for to visit - only used for ADT^A35
                    if (messageType.compareTo("ADT^A35") == 0) {
                        toVisitId = terser.get("/PV1-19");
                    } else if (messageType.compareTo("ADT^A44") == 0) {
                        toVisitId = fromVisitId;
                    }
                    if (toVisitId != null && toVisitId.length() > 0) {
                        if (channelId.compareTo("1") == 0) {
                            toEpisodeIdUniqueKey = createEpisodeIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey);
                            toEpisodeIdResourceId = getResourceId("H", "EpisodeOfCare", toEpisodeIdUniqueKey);
                            toEncounterIdUniqueKey = createEncounterIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey);
                            toEncounterIdResourceId = getResourceId("H", "Encounter", toEncounterIdUniqueKey);
                        } else {
                            toEpisodeIdUniqueKey = createEpisodeIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey);
                            toEpisodeIdResourceId = getResourceId("B", "EpisodeOfCare", toEpisodeIdUniqueKey);
                            toEncounterIdUniqueKey = createEncounterIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey);
                            toEncounterIdResourceId = getResourceId("B", "Encounter", toEncounterIdUniqueKey);
                            if (toEncounterIdResourceId == null) {
                                LOG.info("To-Encounter ResourceId not found for key " + toEncounterIdUniqueKey);
                                toEncounterIdUniqueKey = createEncounterIdUniqueKeyOld(localPatientId, toVisitId, encounterDateTimeForUniqueKey);
                                toEncounterIdResourceId = getResourceId("B", "Encounter", toEncounterIdUniqueKey);
                            }
                        }
                    }

                    if (verbose) {
                        LOG.info("fromPatient =" + fromPatient);
                        LOG.info("fromPatientUniquePatientKey =" + fromPatientUniquePatientKey );
                        LOG.info("fromPatientResourceId =" + fromPatientResourceId);

                        LOG.info("toPatientUniquePatientKey =" + toPatientUniquePatientKey);
                        LOG.info("toPatientResourceId =" + toPatientResourceId);

                        LOG.info("fromVisitId =" + fromVisitId);
                        LOG.info("fromEpisodeIdUniquekey =" + fromEpisodeIdUniqueKey);
                        LOG.info("fromEpisodeIdResourceId = " + fromEpisodeIdResourceId);
                        LOG.info("fromEncounterIdUniquekey =" + fromEncounterIdUniqueKey);
                        LOG.info("fromEncounterIdResourceId =" + fromEncounterIdResourceId);

                        LOG.info("toVisitId =" + toVisitId);
                        LOG.info("toEpisodeIdUniquekey =" + toEpisodeIdUniqueKey);
                        LOG.info("toEpisodeIdResourceId =" + toEpisodeIdResourceId);
                        LOG.info("toEncounterIdUniqueKey =" + toEncounterIdUniqueKey);
                        LOG.info("toEncounterIdResourceId =" + toEncounterIdResourceId);
                    }

                    // *********************
                    //Save merge/move record
                    // *********************
                    boolean saved = false;
                    if (messageType.compareTo("ADT^A34") == 0) {
                        msgCountA34++;
                        if (fromPatientResourceId == null) {
                            LOG.info("From-Patient ResourceId not found for key " + fromPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (toPatientResourceId == null) {
                            LOG.info("To-Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else {
                            msgCountA34Saved++;
                            saved = true;
                            LOG.info("Patient merge from " + fromPatient + " (" + fromPatientResourceId + ") to " + localPatientId + "(" + toPatientResourceId + ") on date " + encounterDateTime + " for service " + globalserviceId);
                            if (!readOnly) {
                                // Save merge db entry
                                recordMerge(globalserviceId, RESOURCETYPE_PATIENT, UUID.fromString(fromPatientResourceId), UUID.fromString(toPatientResourceId));
                            }
                        }
                    }
                    else if (messageType.compareTo("ADT^A35") == 0) {
                        msgCountA35++;
                        if (toPatientResourceId == null) {
                            LOG.info("Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else {
                            msgCountA35Saved++;
                            saved = true;

                            if (fromEpisodeIdResourceId == null) {
                                ResourceId episodeResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    episodeResourceId.setScopeId("H");
                                } else {
                                    episodeResourceId.setScopeId("B");
                                }
                                episodeResourceId.setResourceType("EpisodeOfCare");
                                episodeResourceId.setUniqueId(createEpisodeIdUniqueKey(channelId, localPatientId, fromVisitId, encounterDateTimeForUniqueKey));
                                episodeResourceId.setResourceId(UUID.randomUUID());
                                fromEpisodeIdResourceId = episodeResourceId.getResourceId().toString();
                                LOG.info("Create " + episodeResourceId.getResourceType() + " resourceId " + episodeResourceId.getResourceId() + " in scope " + episodeResourceId.getScopeId() + " for key:" + episodeResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(episodeResourceId);
                                }
                            }

                            if (fromEncounterIdResourceId == null) {
                                ResourceId encounterResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    encounterResourceId.setScopeId("H");
                                } else {
                                    encounterResourceId.setScopeId("B");
                                }
                                encounterResourceId.setResourceType("Encounter");
                                encounterResourceId.setUniqueId(createEncounterIdUniqueKey(channelId, localPatientId, fromVisitId, encounterDateTimeForUniqueKey));
                                encounterResourceId.setResourceId(UUID.randomUUID());
                                fromEncounterIdResourceId = encounterResourceId.getResourceId().toString();
                                LOG.info("Create " + encounterResourceId.getResourceType() + " resourceId " + encounterResourceId.getResourceId() + " in scope " + encounterResourceId.getScopeId() + " for key:" + encounterResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(encounterResourceId);
                                }
                            }

                            if (toEpisodeIdResourceId == null) {
                                ResourceId episodeResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    episodeResourceId.setScopeId("H");
                                } else {
                                    episodeResourceId.setScopeId("B");
                                }
                                episodeResourceId.setResourceType("EpisodeOfCare");
                                episodeResourceId.setUniqueId(createEpisodeIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey));
                                episodeResourceId.setResourceId(UUID.randomUUID());
                                toEpisodeIdResourceId = episodeResourceId.getResourceId().toString();
                                LOG.info("Create " + episodeResourceId.getResourceType() + " resourceId " + episodeResourceId.getResourceId() + " in scope " + episodeResourceId.getScopeId() + " for key:" + episodeResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(episodeResourceId);
                                }
                            }

                            if (toEncounterIdResourceId == null) {
                                ResourceId encounterResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    encounterResourceId.setScopeId("H");
                                } else {
                                    encounterResourceId.setScopeId("B");
                                }
                                encounterResourceId.setResourceType("Encounter");
                                encounterResourceId.setUniqueId(createEncounterIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey));
                                encounterResourceId.setResourceId(UUID.randomUUID());
                                toEncounterIdResourceId = encounterResourceId.getResourceId().toString();
                                LOG.info("Create " + encounterResourceId.getResourceType() + " resourceId " + encounterResourceId.getResourceId() + " in scope " + encounterResourceId.getScopeId() + " for key:" + encounterResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(encounterResourceId);
                                }
                            }

                            LOG.info("Encounter merge for patient " + localPatientId + "(" + fromPatientResourceId + ") From visit " + fromVisitId + "(" + fromEncounterIdResourceId + ") to visit " + toVisitId + "(" + toEncounterIdResourceId + ") on date " + encounterDateTime);
                            if (!readOnly) {
                                // Save merge db entry
                                recordMerge(globalserviceId, RESOURCETYPE_ENCOUNTER, UUID.fromString(fromEncounterIdResourceId), UUID.fromString(toEncounterIdResourceId));
                            }
                        }
                    }
                    else if (messageType.compareTo("ADT^A44") == 0) {
                        msgCountA44++;
                        if (fromPatientResourceId == null) {
                            LOG.info("From-Patient ResourceId not found for key " + fromPatientUniquePatientKey + " in message_id " + messageId);
                        } else if (toPatientResourceId == null) {
                            LOG.info("To-Patient ResourceId not found for key " + toPatientUniquePatientKey + " in message_id " + messageId);
                        } else {
                            msgCountA44Saved++;
                            saved = true;

                            if (fromEpisodeIdResourceId == null) {
                                ResourceId episodeResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    episodeResourceId.setScopeId("H");
                                } else {
                                    episodeResourceId.setScopeId("B");
                                }
                                episodeResourceId.setResourceType("EpisodeOfCare");
                                episodeResourceId.setUniqueId(createEpisodeIdUniqueKey(channelId, fromPatient, fromVisitId, encounterDateTimeForUniqueKey));
                                episodeResourceId.setResourceId(UUID.randomUUID());
                                fromEpisodeIdResourceId = episodeResourceId.getResourceId().toString();
                                LOG.info("Create " + episodeResourceId.getResourceType() + " resourceId " + episodeResourceId.getResourceId() + " in scope " + episodeResourceId.getScopeId() + " for key:" + episodeResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(episodeResourceId);
                                }
                            }

                            if (fromEncounterIdResourceId == null) {
                                ResourceId encounterResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    encounterResourceId.setScopeId("H");
                                } else {
                                    encounterResourceId.setScopeId("B");
                                }
                                encounterResourceId.setResourceType("Encounter");
                                encounterResourceId.setUniqueId(createEncounterIdUniqueKey(channelId, fromPatient, fromVisitId, encounterDateTimeForUniqueKey));
                                encounterResourceId.setResourceId(UUID.randomUUID());
                                fromEncounterIdResourceId = encounterResourceId.getResourceId().toString();
                                LOG.info("Create " + encounterResourceId.getResourceType() + " resourceId " + encounterResourceId.getResourceId() + " in scope " + encounterResourceId.getScopeId() + " for key:" + encounterResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(encounterResourceId);
                                }
                            }

                            if (toEpisodeIdResourceId == null) {
                                ResourceId episodeResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    episodeResourceId.setScopeId("H");
                                } else {
                                    episodeResourceId.setScopeId("B");
                                }
                                episodeResourceId.setResourceType("EpisodeOfCare");
                                episodeResourceId.setUniqueId(createEpisodeIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey));
                                episodeResourceId.setResourceId(UUID.randomUUID());
                                toEpisodeIdResourceId = episodeResourceId.getResourceId().toString();
                                LOG.info("Create " + episodeResourceId.getResourceType() + " resourceId " + episodeResourceId.getResourceId() + " in scope " + episodeResourceId.getScopeId() + " for key:" + episodeResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(episodeResourceId);
                                }
                            }

                            if (toEncounterIdResourceId == null) {
                                ResourceId encounterResourceId = new ResourceId();
                                if (channelId.compareTo("1") == 0) {
                                    encounterResourceId.setScopeId("H");
                                } else {
                                    encounterResourceId.setScopeId("B");
                                }
                                encounterResourceId.setResourceType("Encounter");
                                encounterResourceId.setUniqueId(createEncounterIdUniqueKey(channelId, localPatientId, toVisitId, encounterDateTimeForUniqueKey));
                                encounterResourceId.setResourceId(UUID.randomUUID());
                                toEncounterIdResourceId = encounterResourceId.getResourceId().toString();
                                LOG.info("Create " + encounterResourceId.getResourceType() + " resourceId " + encounterResourceId.getResourceId() + " in scope " + encounterResourceId.getScopeId() + " for key:" + encounterResourceId.getUniqueId());
                                if (!readOnly) {
                                    // Create new resource_uuid entry - the from-visit-id under the to-patient-id doesnt exist so to ensure it gets the right resoruce id its created here
                                    saveResourceId(encounterResourceId);
                                }
                            }

                            LOG.info("Patient move from " + fromPatient + " (" + fromPatientResourceId + ") to " + localPatientId + "(" + toPatientResourceId + ") on date " + encounterDateTime);
                            LOG.info("Encounter move. From visitId " + fromVisitId + "(" + fromEncounterIdResourceId + ") To visitId " + toVisitId + "(" + toEncounterIdResourceId + ") on date " + encounterDateTime);

                            if (!readOnly) {
                                // Save merge db entry
                                // recordMerge for patient resources may cause a duplicate if the A44 is part of a patient merge (A34) so duplicate errors should be ignored
                                recordMerge(globalserviceId, RESOURCETYPE_PATIENT, UUID.fromString(fromPatientResourceId), UUID.fromString(toPatientResourceId));
                                recordMerge(globalserviceId, RESOURCETYPE_ENCOUNTER, UUID.fromString(fromEncounterIdResourceId), UUID.fromString(toEncounterIdResourceId));
                            }
                        }
                    }

                    if (verbose && saved == false) {
                        LOG.info(hapiMsg.printStructure());
                    }

                }

            } finally {
                LOG.info("MsgCount=" + msgCount);
                LOG.info("MsgCountA34=" + msgCountA34);
                LOG.info("MsgCountA35=" + msgCountA35);
                LOG.info("MsgCountA44=" + msgCountA44);
                LOG.info("MsgCountA34Saved=" + msgCountA34Saved);
                LOG.info("MsgCountA35Saved=" + msgCountA35Saved);
                LOG.info("MsgCountA44Saved=" + msgCountA44Saved);
                //******************************************************************************
                // TEST
                //******************************************************************************
                //UUID fakeUUID = UUID.fromString("3ce7aace-5682-43f4-9036-999999999999");
                //UUID newUUID = dalResourceMerge.resolveMergeUUID(globalserviceId, "Patient", fakeUUID);
                //LOG.info("Test1 reply=" + newUUID + " result=" + (newUUID.toString().compareTo(fakeUUID.toString()) == 0));
                //
                //UUID newUUID = dalResourceMerge.resolveMergeUUID(globalserviceId, "Patient", UUID.fromString("8de0319c-d8a8-450c-b5aa-91c117f8d9e3"));
                //LOG.info("Test2 reply=" + newUUID + " result=" + (newUUID.toString().compareTo("3ce7aace-5682-43f4-9036-5e4c9f1d8803") == 0));
                //
                //Create two-step test
                //dalResourceMerge.insertMergeRecord(globalserviceId, "Patient", UUID.fromString("3ce7aace-5682-43f4-9036-5e4c9f1d8803"), fakeUUID);
                //newUUID = dalResourceMerge.resolveMergeUUID(globalserviceId, "Patient", UUID.fromString("8de0319c-d8a8-450c-b5aa-91c117f8d9e3"));
                //LOG.info("Test3 reply=" + newUUID + " result=" + (newUUID.toString().compareTo(fakeUUID.toString()) == 0));
                // Remove fakeUUID manually
                //******************************************************************************
                resultSet.close();
                connection.close();

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

    /*
    private static void executeUpdate(String sql) throws Exception {

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
        } finally {
            connection.close();
        }
    }*/

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

    public static void saveResourceId(ResourceId r) throws Exception {
        dalHL7ResourceId.saveResourceId(r);
        /*
        try {
            resourceIdInsertStatement.setString(1, r.getScopeId());
            resourceIdInsertStatement.setString(2, r.getResourceType());
            resourceIdInsertStatement.setString(3, r.getUniqueId());
            resourceIdInsertStatement.setObject(4, r.getResourceId());
        }
        catch (SQLException sq) {
            throw sq;
        }
        try {
            if (resourceIdInsertStatement.executeUpdate() != 1) {
                throw new SQLException("Could not create ResourceId:" + r.getScopeId() + ":" + r.getResourceType() + ":" + r.getUniqueId() + ":" + r.getResourceId().toString());
            }
        }
        catch (SQLException sq) {
            if (sq.getMessage().indexOf("duplicate key") >= 0) {
                LOG.info("Duplicate key");
                throw new SQLException("Could not create ResourceId:" + r.getScopeId() + ":" + r.getResourceType() + ":" + r.getUniqueId() + ":" + r.getResourceId().toString(), sq);
            } else {
                throw sq;
            }
        }
        */

    }

    public static void recordMerge(UUID globalserviceId, String resourceType, UUID fromEncounterIdResourceId, UUID toEncounterIdResourceId) throws Exception {
        try {
            dalResourceMerge.upsertMergeRecord(globalserviceId, resourceType, fromEncounterIdResourceId, toEncounterIdResourceId);
        }
        catch (Exception sq) {
            LOG.info("SQL DAL error:" + sq.getMessage());
            if (sq.getMessage().indexOf("duplicate key") >= 0) {
                LOG.info("Duplicate key");
                throw new SQLException("Could not create " + resourceType + " in service " + globalserviceId + " from " + fromEncounterIdResourceId + " to " + toEncounterIdResourceId, sq);
            } else {
                throw sq;
            }
        }

    }

    public static String createEpisodeIdUniqueKey(String channelId, String localPatientId, String visitId, String encounterDateTimeForUniqueKey) {
        if (channelId.compareTo("1") == 0) {
            return "PatIdTypeCode=CNN-PatIdValue=" + localPatientId+ "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + visitId;
        } else {
            return "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + visitId;
        }
    }

    public static String createEncounterIdUniqueKey(String channelId, String localPatientId, String visitId, String encounterDateTimeForUniqueKey) {
        if (channelId.compareTo("1") == 0) {
            return "PatIdTypeCode=CNN-PatIdValue=" + localPatientId + "-EpIdAssAuth=HOMERTONFIN-EpIdValue=" + visitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
        } else {
            return "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + visitId;
        }
    }

    public static String createEncounterIdUniqueKeyOld(String localPatientId, String visitId, String encounterDateTimeForUniqueKey) {
        return "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=" + localPatientId + "-EpIdTypeCode=VISITID-EpIdValue=" + visitId + "-EncounterDateTime=" + encounterDateTimeForUniqueKey;
    }
}
