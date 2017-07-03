package org.endeavourhealth.hl7transform.mapper.resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.transforms.BartsPatientTransform;
import org.hl7.fhir.instance.model.ResourceType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ResourceMapper {

    private static final String PatientIdentifierTypeCodeKey = "PatientIdentifierTypeCode";
    private static final String PatientIdentifierAssigningAuthorityKey = "PatientIdentifierAssigningAuthority";
    private static final String PatientIdentifierValueKey = "PatientIdentifierValue";
    private static final String EpisodeIdentifierTypeCodeKey = "EpisodeIdentifierTypeCode";
    private static final String EpisodeIdentifierAssigningAuthorityKey = "EpisodeIdentifierAssigningAuthority";
    private static final String EpisodeIdentifierValueKey = "EpisodeIdentifierValue";
    private static final String EncounterDateTimeKey = "EncounterDateTime";
    private static final String OdsCodeKey = "OdsCode";

    private Mapper mapper;

    public ResourceMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Mapping - Global UUIDs
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
    // Mapping - Conditionally global/scoped UUIDs
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UUID mapOrganisationUuid(String odsCode, String name) throws MapperException {
        Validate.notBlank(name, "name");

        String identifier = null;

        if (StringUtils.isBlank(odsCode)) {
            identifier = ResourceMapParameters.create()
                    .put(OdsCodeKey, odsCode)
                    .put("Name", name)
                    .createIdentifyingString();

            return this.mapper.mapScopedResourceUuid(ResourceType.Organization, identifier);
        }

        identifier = ResourceMapParameters.create()
                .put(OdsCodeKey, odsCode)
                .createIdentifyingString();

        return this.mapper.mapGlobalResourceUuid(ResourceType.Organization, identifier);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Mapping - Scoped UUIDs
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UUID mapMessageHeaderUuid(String messageControlId) throws MapperException {
        Validate.notBlank(messageControlId);

        String identifier = ResourceMapParameters.create()
                .put("MessageControlId", messageControlId)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.MessageHeader, identifier);
    }

    public UUID mapParametersUuid(String messageControlId, String parametersType) throws MapperException {
        Validate.notBlank(messageControlId);
        Validate.notBlank(parametersType);

        String identifier = ResourceMapParameters.create()
                .put("MessageControlId", messageControlId)
                .put("ParametersType", parametersType)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Parameters, identifier);
    }

    public UUID mapPatientUuid(String patientIdentifierTypeCode, String patientIdentifierAssigningAuthority, String patientIdentifierValue) throws MapperException {

        String identifier =
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
                                 LocalDateTime encounterDateTime) throws MapperException {

        Validate.notNull(encounterDateTime, "encounterDateTime");

        String identifier = ResourceMapParameters.create()
                .putExisting(getEpisodeMap(
                        patientIdentifierTypeCode,
                        patientIdentifierAssigningAuthority,
                        patientIdentifierValue,
                        episodeIdentifierTypeCode,
                        episodeIdentifierAssigningAuthority,
                        episodeIdentifierValue))
                .put(EncounterDateTimeKey, encounterDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Encounter, identifier);
    }

    public UUID mapOrganisationUuidForHospitalService(String parentOdsCode, String serviceName) throws MapperException {
        Validate.notBlank(parentOdsCode, "parentOdsCode");
        Validate.notBlank(serviceName, "serviceName");

        String identifier = ResourceMapParameters.create()
                .put("ParentOdsCode", parentOdsCode)
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
                .put(OdsCodeKey, odsCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
    }

    public UUID mapPractitionerUuidWithConsultantCode(String surname, String forename, String consultantCode) throws MapperException {
        Validate.notBlank(surname);
        Validate.notBlank(consultantCode);

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("ConsultantCode", consultantCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
    }

    public UUID mapPractitionerUuidWithGmcCode(String surname, String forename, String gmcCode) throws MapperException {
        Validate.notBlank(surname);
        Validate.notBlank(gmcCode);

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("GmcCode", gmcCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
    }

    public UUID mapPractitionerUuidWithGdpCode(String surname, String forename, String gdpCode) throws MapperException {
        Validate.notBlank(surname);
        Validate.notBlank(gdpCode);

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("GmcCode", gdpCode)
                .createIdentifyingString();

        return this.mapper.mapScopedResourceUuid(ResourceType.Practitioner, identifier);
    }

    public UUID mapPractitionerUuidWithLocalHospitalIdentifiers(String surname, String forename, String localIdAssAuth1, String localIdValue1, String localIdAssAuth2, String localIdValue2) throws MapperException {
        Validate.notBlank(surname);
        Validate.isTrue((StringUtils.isNotBlank(localIdAssAuth1) && StringUtils.isNotBlank(localIdValue1))
                || (StringUtils.isNotBlank(localIdAssAuth2) && StringUtils.isNotBlank(localIdValue2)), "Not enough identifiers to proceed");

        String identifier = ResourceMapParameters.create()
                .put("Surname", surname)
                .put("Forename", forename)
                .put("LocalIdAssAuth1", localIdAssAuth1)
                .put("LocalIdValue1", localIdValue1)
                .put("LocalIdAssAuth2", localIdAssAuth2)
                .put("LocalIdValue2", localIdValue2)
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static ResourceMapParameters getEpisodeMap(String patientIdentifierTypeCode,
                                                       String patientIdentifierAssigningAuthority,
                                                       String patientIdentifierValue,
                                                       String episodeIdentifierTypeCode,
                                                       String episodeIdentifierAssigningAuthority,
                                                       String episodeIdentifierValue) {

        Validate.isTrue(StringUtils.isNotBlank(episodeIdentifierTypeCode) || StringUtils.isNotBlank(episodeIdentifierAssigningAuthority), "episodeIdentifierTypeCode and episodeIdentifierAssigningAuthority are both blank");
        Validate.notBlank(episodeIdentifierValue, "episodeIdentifierValue");

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.create()
                .putExisting(getPatientMap(patientIdentifierTypeCode, patientIdentifierAssigningAuthority, patientIdentifierValue));

        if (StringUtils.isNotEmpty(episodeIdentifierTypeCode))
            resourceMapParameters.put(EpisodeIdentifierTypeCodeKey, episodeIdentifierTypeCode);

        if (StringUtils.isNotEmpty(episodeIdentifierAssigningAuthority))
            resourceMapParameters.put(EpisodeIdentifierAssigningAuthorityKey, episodeIdentifierAssigningAuthority);

        resourceMapParameters.put(EpisodeIdentifierValueKey, episodeIdentifierValue);

        return resourceMapParameters;
    }

    private static ResourceMapParameters getPatientMap(String patientIdentifierTypeCode,
                                                       String patientIdentifierAssigningAuthority,
                                                       String patientIdentifierValue) {

        Validate.isTrue(StringUtils.isNotBlank(patientIdentifierTypeCode) || StringUtils.isNotBlank(patientIdentifierAssigningAuthority), "patientIdentifierTypeCode and patientIdentifierAssigningAuthority are both blank");
        Validate.notBlank(patientIdentifierValue, "patientIdentifierValue");

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.create();

        if (StringUtils.isNotEmpty(patientIdentifierTypeCode))
            resourceMapParameters.put(PatientIdentifierTypeCodeKey, patientIdentifierTypeCode);

        if (StringUtils.isNotEmpty(patientIdentifierAssigningAuthority))
            resourceMapParameters.put(PatientIdentifierAssigningAuthorityKey, patientIdentifierAssigningAuthority);

        resourceMapParameters.put(PatientIdentifierValueKey, patientIdentifierValue);

        return resourceMapParameters;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Remapping
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<MappedResourceUuid> getPatientResourceUuidMappings(String patientIdentifierTypeCode,
                                                                    String patientIdentifierAssigningAuthority,
                                                                    String patientIdentifierValue) throws MapperException {

        String identifier =
                getPatientMap(
                        patientIdentifierTypeCode,
                        patientIdentifierAssigningAuthority,
                        patientIdentifierValue)
                        .createIdentifyingString();

        return this.mapper.getScopedResourceUuidMappings(identifier);
    }

    public List<MappedResourceUuid> getEpisodeResourceUuidMappings(String patientIdentifierTypeCode,
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

        return this.mapper.getScopedResourceUuidMappings(identifier);
    }

    public HashMap<MappedResourceUuid, UUID> remapPatientResourceUuids(String patientIdentifierTypeCode,
                                                                       String patientIdentifierAssigningAuthority,
                                                                       String majorPatientIdentifierValue,
                                                                       String minorPatientIdentifierValue) throws MapperException, ParseException {

        List<MappedResourceUuid> mappedResourceUuids = getPatientResourceUuidMappings(
                patientIdentifierTypeCode,
                patientIdentifierAssigningAuthority,
                minorPatientIdentifierValue);

        HashMap<MappedResourceUuid, UUID> result = new HashMap<>();

        for (MappedResourceUuid mappedResourceUuid : mappedResourceUuids) {

            UUID newResourceUuid;

            switch (ResourceType.valueOf(mappedResourceUuid.getResourceType())) {
                case Patient: continue;
                case EpisodeOfCare: newResourceUuid = remapEpisodeOfCareUuid(mappedResourceUuid.getUniqueIdentifier(), majorPatientIdentifierValue); break;
                case Encounter: newResourceUuid = remapEncounterUuid(mappedResourceUuid.getUniqueIdentifier(), majorPatientIdentifierValue); break;
                default: throw new MapperException("ResourceType " + mappedResourceUuid.getResourceType() + " not expected when re-mapping resource UUIDs as part of merge");
            }

            if (newResourceUuid == null)
                throw new MapperException("Could not re-map resource UUID for resource of type " + mappedResourceUuid.getResourceType());

            result.put(mappedResourceUuid, newResourceUuid);
        }

        return result;
    }

    public HashMap<MappedResourceUuid, UUID> remapEpisodeResourceUuids(String patientIdentifierTypeCode,
                                                                       String patientIdentifierAssigningAuthority,
                                                                       String majorPatientIdentifierValue,
                                                                       String minorPatientIdentifierValue,
                                                                       String episodeIdentifierTypeCode,
                                                                       String episodeIdentifierAssigningAuthority,
                                                                       String episodeIdentifierValue) throws MapperException, ParseException {

        List<MappedResourceUuid> mappedResourceUuids = getEpisodeResourceUuidMappings(
                patientIdentifierTypeCode,
                patientIdentifierAssigningAuthority,
                minorPatientIdentifierValue,
                episodeIdentifierTypeCode,
                episodeIdentifierAssigningAuthority,
                episodeIdentifierValue);

        HashMap<MappedResourceUuid, UUID> result = new HashMap<>();

        for (MappedResourceUuid mappedResourceUuid : mappedResourceUuids) {

            UUID newResourceUuid;

            switch (ResourceType.valueOf(mappedResourceUuid.getResourceType())) {
                case EpisodeOfCare: newResourceUuid = remapEpisodeOfCareUuid(mappedResourceUuid.getUniqueIdentifier(), majorPatientIdentifierValue); break;
                case Encounter: newResourceUuid = remapEncounterUuid(mappedResourceUuid.getUniqueIdentifier(), majorPatientIdentifierValue); break;
                default: throw new MapperException("ResourceType " + mappedResourceUuid.getResourceType() + " not expected when re-mapping resource UUIDs as part of merge");
            }

            if (newResourceUuid == null)
                throw new MapperException("Could not re-map resource UUID for resource of type " + mappedResourceUuid.getResourceType());

            result.put(mappedResourceUuid, newResourceUuid);
        }

        return result;
    }

    private UUID remapEpisodeOfCareUuid(String existingUniqueIdentifier, String newPatientIdentifierValue) throws MapperException {

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.parse(existingUniqueIdentifier);

        String patientIdentifierTypeCode = resourceMapParameters.get(PatientIdentifierTypeCodeKey);
        String patientIdentifierAssigningAuthority = resourceMapParameters.get(PatientIdentifierAssigningAuthorityKey);
        String episodeIdentifierTypeCode = resourceMapParameters.get(EpisodeIdentifierTypeCodeKey);
        String episodeIdentifierAssigningAuthority = resourceMapParameters.get(EpisodeIdentifierAssigningAuthorityKey);
        String episodeIdentifierValue = resourceMapParameters.get(EpisodeIdentifierValueKey);

        return mapEpisodeUuid(
                patientIdentifierTypeCode,
                patientIdentifierAssigningAuthority,
                newPatientIdentifierValue,
                episodeIdentifierTypeCode,
                episodeIdentifierAssigningAuthority,
                episodeIdentifierValue);
    }

    private UUID remapEncounterUuid(String existingUniqueIdentifier, String newPatientIdentifierValue) throws MapperException {

        ResourceMapParameters resourceMapParameters = ResourceMapParameters.parse(existingUniqueIdentifier);

        String patientIdentifierTypeCode = resourceMapParameters.get(PatientIdentifierTypeCodeKey);
        String patientIdentifierAssigningAuthority = resourceMapParameters.get(PatientIdentifierAssigningAuthorityKey);
        String episodeIdentifierTypeCode = resourceMapParameters.get(EpisodeIdentifierTypeCodeKey);
        String episodeIdentifierAssigningAuthority = resourceMapParameters.get(EpisodeIdentifierAssigningAuthorityKey);
        String episodeIdentifierValue = resourceMapParameters.get(EpisodeIdentifierValueKey);
        LocalDateTime encounterDateTime = LocalDateTime.parse(resourceMapParameters.get(EncounterDateTimeKey), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return mapEncounterUuid(
                patientIdentifierTypeCode,
                patientIdentifierAssigningAuthority,
                newPatientIdentifierValue,
                episodeIdentifierTypeCode,
                episodeIdentifierAssigningAuthority,
                episodeIdentifierValue,
                encounterDateTime);
    }
}
