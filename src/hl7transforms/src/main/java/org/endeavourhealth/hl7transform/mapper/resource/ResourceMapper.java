package org.endeavourhealth.hl7transform.mapper.resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ResourceMapper {

    private Mapper mapper;

    public ResourceMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Global UUIDs
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UUID mapClassOfLocationUuid(String classOfLocationName) throws MapperException {
        Validate.notBlank(classOfLocationName, "classOfLocationName");

        String identifier = ResourceMapParameters.create()
                .put("ClassOfLocationName", classOfLocationName)
                .createIdentifyingString();

        return this.mapper.mapGlobalResourceUuid(ResourceType.Location, identifier);
    }

    public UUID mapLocationUuid(String odsSiteCode) throws MapperException {
        Validate.notBlank(odsSiteCode, "odsSiteCode");

        String identifier = ResourceMapParameters.create()
                .put("OdsSiteCode", odsSiteCode)
                .createIdentifyingString();

        return this.mapper.mapGlobalResourceUuid(ResourceType.Location, identifier);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Conditionally global/scoped UUIDs
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UUID mapOrganisationUuid(String odsCode, String name) throws MapperException {
        Validate.notBlank(name, "name");

        String identifier = null;

        if (StringUtils.isBlank(odsCode)) {
            identifier = ResourceMapParameters.create()
                    .put("OdsCode", odsCode)
                    .put("Name", name)
                    .createIdentifyingString();

            return this.mapper.mapScopedResourceUuid(ResourceType.Organization, identifier);
        }

        identifier = ResourceMapParameters.create()
                .put("OdsCode", odsCode)
                .createIdentifyingString();

        return this.mapper.mapGlobalResourceUuid(ResourceType.Organization, identifier);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scoped UUIDs
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UUID mapMessageHeaderUuid(String messageControlId) throws MapperException {
        Validate.notBlank(messageControlId);

        String identifier = ResourceMapParameters.create()
                .put("MessageControlId", messageControlId)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.MessageHeader, identifier);
    }

    public UUID mapPatientUuid(String patientIdentifierTypeCode, String patientIdentifierAssigningAuthority, String patientIdentifierValue) throws MapperException {
        String identifier;

            identifier =
                    getPatientMap(
                            patientIdentifierTypeCode,
                            patientIdentifierAssigningAuthority,
                            patientIdentifierValue)
                            .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Patient, identifier);
    }

    public UUID mapEpisodeUuid(String patientIdentifierTypeCode,
                               String patientIdentifierAssigningAuthority,
                               String patientIdentifierValue,
                               String episodeIdentifierTypeCode,
                               String episodeIdentifierAssigningAuthority,
                               String episodeIdentifierValue) throws MapperException {

        String identifier =
                getEpisodeMap(
                        patientIdentifierTypeCode,
                        patientIdentifierAssigningAuthority,
                        patientIdentifierValue,
                        episodeIdentifierTypeCode,
                        episodeIdentifierAssigningAuthority,
                        episodeIdentifierValue)
                        .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.EpisodeOfCare, identifier);
    }

    public UUID mapEncounterUuid(String patientIdentifierTypeCode,
                                 String patientIdentifierAssigningAuthority,
                                 String patientIdentifierValue,
                                 String episodeIdentifierTypeCode,
                                 String episodeIdentifierAssigningAuthority,
                                 String episodeIdentifierValue,
                                 Hl7DateTime encounterDateTime) throws MapperException {

        Validate.notNull(encounterDateTime, "encounterDateTime");
        Validate.notNull(encounterDateTime.getLocalDateTime(), "encounterDateTime.getLocalDateTime()");

        String identifier = ResourceMapParameters.create()
                .putExisting(getEpisodeMap(
                        patientIdentifierTypeCode,
                        patientIdentifierAssigningAuthority,
                        patientIdentifierValue,
                        episodeIdentifierTypeCode,
                        episodeIdentifierAssigningAuthority,
                        episodeIdentifierValue))
                .put("EncounterDateTime", encounterDateTime.getLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Encounter, identifier);
    }

    public UUID mapOrganisationUuid(String parentOdsCode, String parentName, String serviceName) throws MapperException {
        Validate.notBlank(parentOdsCode, "parentOdsCode");
        Validate.notBlank(parentName, "parentName");
        Validate.notBlank(serviceName, "serviceName");

        String identifier = ResourceMapParameters.create()
                .put("ParentOdsCode", parentOdsCode)
                .put("ParentName", parentName)
                .put("ServiceName", serviceName)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Organization, identifier);
    }

    public UUID mapLocationUuid(String parentOdsSiteCode, List<String> locationNames) throws MapperException {
        Validate.notBlank(parentOdsSiteCode, "parentOdsSiteCode");
        Validate.notBlank(StringUtils.join(locationNames, ""), "locationNames");

        String identifier = ResourceMapParameters.create()
                .put("ParentOdsSiteCode", parentOdsSiteCode)
                .put("LocationNameHierarchy", locationNames)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Location, identifier);
    }

    public UUID mapPractitionerUuid(String surname,
                                    String forename,
                                    String odsCode) throws MapperException {
        Validate.notBlank(surname);

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("OdsCode", odsCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
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

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("LocalPrimaryIdentifierType", localPrimaryIdentifierType)
                .put("LocalPrimaryIdentifierValue", localPrimaryIdentifierValue)
                .put("ConsultantCode", consultantCode)
                .put("GmcCode", gmcCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
    }

    private static ResourceMapParameters getEpisodeMap(String patientIdentifierTypeCode,
                                                       String patientIdentifierAssigningAuthority,
                                                       String patientIdentifierValue,
                                                       String episodeIdentifierTypeCode,
                                                       String episodeIdentifierAssigningAuthority,
                                                       String episodeIdentifierValue) {

        Validate.isTrue(StringUtils.isNotBlank(episodeIdentifierTypeCode) || StringUtils.isNotBlank(episodeIdentifierAssigningAuthority), "episodeIdentifierTypeCode and episodeIdentifierAssigningAuthority are both blank");
        Validate.notEmpty(episodeIdentifierValue, "episodeIdentifierValue");

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.create()
                .putExisting(getPatientMap(patientIdentifierTypeCode, patientIdentifierAssigningAuthority, patientIdentifierValue));

        if (StringUtils.isNotEmpty(episodeIdentifierTypeCode))
            resourceMapParameters.put("EpisodeIdentifierTypeCode", episodeIdentifierTypeCode);

        if (StringUtils.isNotEmpty(episodeIdentifierAssigningAuthority))
            resourceMapParameters.put("EpisodeIdentifierAssigningAuthority", episodeIdentifierAssigningAuthority);

        resourceMapParameters.put("EpisodeIdentifierValue", episodeIdentifierValue);

        return resourceMapParameters;
    }

    private static ResourceMapParameters getPatientMap(String patientIdentifierTypeCode,
                                                       String patientIdentifierAssigningAuthority,
                                                       String patientIdentifierValue) {

        Validate.isTrue(StringUtils.isNotBlank(patientIdentifierTypeCode) || StringUtils.isNotBlank(patientIdentifierAssigningAuthority), "patientIdentifierTypeCode and patientIdentifierAssigningAuthority are both blank");
        Validate.notEmpty(patientIdentifierValue, "patientIdentifierValue");

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.create();

        if (StringUtils.isNotEmpty(patientIdentifierTypeCode))
            resourceMapParameters.put("PatientIdentifierTypeCode", patientIdentifierTypeCode);

        if (StringUtils.isNotEmpty(patientIdentifierAssigningAuthority))
            resourceMapParameters.put("PatientIdentifierAssigningAuthority", patientIdentifierTypeCode);

        resourceMapParameters.put("PatientIdentifierValue", patientIdentifierValue);

        return resourceMapParameters;
    }
}
