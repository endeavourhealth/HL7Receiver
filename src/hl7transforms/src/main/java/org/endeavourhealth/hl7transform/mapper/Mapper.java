package org.endeavourhealth.hl7transform.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.ResourceType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public abstract class Mapper {

    public abstract Code mapCode(String mapContext, Code code) throws MapperException;
    public abstract UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException;

    public UUID mapMessageHeaderUuid(String messageControlId) throws MapperException {
        Validate.notBlank(messageControlId);

        String identifier = MapParameters.create()
                .put("MessageControlId", messageControlId)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.MessageHeader, identifier);
    }

    public UUID mapPatientUuid(String patientIdentifierTypeCode, String patientIdentifierValue) throws MapperException {

        String identifier =
                getPatientMap(
                        patientIdentifierTypeCode,
                        patientIdentifierValue)
                        .createIdentifyingString();

        return mapResourceUuid(ResourceType.Patient, identifier);
    }

    public UUID mapEpisodeUuid(String patientIdentifierTypeCode,
                               String patientIdentifierValue,
                               String episodeIdentifierAssigningAuthority,
                               String episodeIdentifierValue) throws MapperException {

        String identifier =
                getEpisodeMap(
                        patientIdentifierTypeCode,
                        patientIdentifierValue,
                        episodeIdentifierAssigningAuthority,
                        episodeIdentifierValue)
                        .createIdentifyingString();

        return mapResourceUuid(ResourceType.EpisodeOfCare, identifier);
    }

    public UUID mapEncounterUuid(String patientIdentifierTypeCode,
                                 String patientIdentifierValue,
                                 String episodeIdentifierAssigningAuthority,
                                 String episodeIdentifierValue,
                                 Hl7DateTime encounterDateTime) throws MapperException {

        Validate.notNull(encounterDateTime, "encounterDateTime");
        Validate.notNull(encounterDateTime.getLocalDateTime(), "encounterDateTime.getLocalDateTime()");

        String identifier = MapParameters.create()
                .putExisting(getEpisodeMap(
                        patientIdentifierTypeCode,
                        patientIdentifierValue,
                        episodeIdentifierAssigningAuthority,
                        episodeIdentifierValue))
                .put("EncounterDateTime", encounterDateTime.getLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Encounter, identifier);
    }

    public UUID mapOrganisationUuid(String odsCode, String name) throws MapperException {
        Validate.notBlank(odsCode, "odsCode");
        Validate.notBlank(name, "name");

        String identifier = MapParameters.create()
                .put("OdsCode", odsCode)
                .put("Name", name)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Organization, identifier);
    }

    public UUID mapOrganisationUuid(String parentOdsCode, String parentName, String serviceName) throws MapperException {
        Validate.notBlank(parentOdsCode, "parentOdsCode");
        Validate.notBlank(parentName, "parentName");
        Validate.notBlank(serviceName, "serviceName");

        String identifier = MapParameters.create()
                .put("ParentOdsCode", parentOdsCode)
                .put("ParentName", parentName)
                .put("ServiceName", serviceName)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Organization, identifier);
    }

    public UUID mapLocationUuid(String classOfLocationName) throws MapperException {
        Validate.notBlank(classOfLocationName, "classOfLocationName");

        String identifier = MapParameters.create()
                .put("ClassOfLocationName", classOfLocationName)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Location, identifier);
    }

    public UUID mapLocationUuid(String odsSiteCode, String locationName) throws MapperException {
        Validate.notBlank(odsSiteCode, "odsSiteCode");
        Validate.notBlank(locationName, "locationName");

        String identifier = MapParameters.create()
                .put("OdsSiteCode", odsSiteCode)
                .put("LocationName", locationName.replace(".", ""))
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Location, identifier);
    }

    public UUID mapLocationUuid(String parentOdsSiteCode, String parentLocationName, List<String> locationNames) throws MapperException {
        Validate.notBlank(parentOdsSiteCode, "parentOdsSiteCode");
        Validate.notBlank(parentLocationName, "parentLocationName");
        Validate.notBlank(StringUtils.join(locationNames, ""), "locationNames");

        String identifier = MapParameters.create()
                .put("ParentOdsSiteCode", parentOdsSiteCode)
                .put("ParentLocationName", parentLocationName.replace(".", ""))
                .put("LocationNameHierarchy", locationNames)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Location, identifier);
    }

    public UUID mapPractitionerUuid(String surname,
                                     String forename,
                                     String localPrimaryIdentifierType,
                                     String localPrimaryIdentifierValue,
                                     String consultantCode,
                                     String gmcCode) throws MapperException, TransformException {

        Validate.notBlank(surname);
        Validate.isTrue((StringUtils.isNotBlank(localPrimaryIdentifierType) && StringUtils.isNotBlank(localPrimaryIdentifierValue))
                        || StringUtils.isNotBlank(consultantCode)
                        || StringUtils.isNotBlank(gmcCode), "Not enough identifiers to proceed");

        String identifier = MapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("LocalPrimaryIdentifierType", localPrimaryIdentifierType)
                .put("LocalPrimaryIdentifierValue", localPrimaryIdentifierValue)
                .put("ConsultantCode", consultantCode)
                .put("GmcCode", gmcCode)
                .createIdentifyingString();

        return mapResourceUuid(ResourceType.Practitioner, identifier);
    }

    private static MapParameters getEpisodeMap(String patientIdentifierTypeCode,
                                              String patientIdentifierValue,
                                              String episodeIdentifierAssigningAuthority,
                                              String episodeIdentifierValue) {

        Validate.notEmpty(episodeIdentifierAssigningAuthority, "episodeIdentifierAssigningAuthority");
        Validate.notEmpty(episodeIdentifierValue, "episodeIdentifierValue");

        return MapParameters.create()
                .putExisting(getPatientMap(patientIdentifierTypeCode, patientIdentifierValue))
                .put("EpisodeIdentifierAssigningAuthority", episodeIdentifierAssigningAuthority)
                .put("EpisodeIdentifierValue", episodeIdentifierValue);
    }

    private static MapParameters getPatientMap(String patientIdentifierTypeCode,
                                              String patientIdentifierValue) {

        Validate.notEmpty(patientIdentifierTypeCode, "patientIdentifierTypeCode");
        Validate.notEmpty(patientIdentifierValue, "patientIdentifierValue");

        return MapParameters.create()
                .put("PatientIdentifierTypeCode", patientIdentifierTypeCode)
                .put("PatientIdentifierValue", patientIdentifierValue);
    }
}