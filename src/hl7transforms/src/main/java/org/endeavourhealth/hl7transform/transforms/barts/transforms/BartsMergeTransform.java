package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.transform.EpisodeOfCareCommon;
import org.endeavourhealth.hl7transform.common.transform.MergeCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.resource.MappedResourceUuid;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.Parameters;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.*;

public class BartsMergeTransform extends ResourceTransformBase {

    public static final String AdtA34MergeTwoPatients = "ADT^A34";
    public static final String AdtA35MergeEncountersForSamePatient = "ADT^A35";
    public static final String AdtA44MoveEncounterBetweenPatients = "ADT^A44";

    public static final List<String> MergeMessageTypes = Arrays.asList(AdtA34MergeTwoPatients, AdtA35MergeEncountersForSamePatient, AdtA44MoveEncounterBetweenPatients);

    public BartsMergeTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Parameters;
    }

    public Parameters transform(AdtMessage sourceMessage) throws MapperException, ParseException, TransformException {

        String messageType = sourceMessage.getMshSegment().getMessageType();

        if (AdtA34MergeTwoPatients.equalsIgnoreCase(messageType))
            return handleMergeTwoPatients(sourceMessage);
        else if (AdtA35MergeEncountersForSamePatient.equalsIgnoreCase(messageType))
            return handleMergeEncountersForSamePatient(sourceMessage);
        else if (AdtA44MoveEncounterBetweenPatients.equalsIgnoreCase(messageType))
            return handleMoveEncounterBetweenPatients(sourceMessage);

        return null;
    }

    private Parameters handleMergeTwoPatients(AdtMessage sourceMessage) throws MapperException, ParseException, TransformException {

        final String parametersType = "MergeTwoPatients";

        String messageControlId = sourceMessage.getMshSegment().getMessageControlId();
        UUID majorPatientUuid = getMajorPatientUuid(sourceMessage);
        UUID minorPatientUuid = getMinorPatientUuid(sourceMessage);

        HashMap<MappedResourceUuid, UUID> resourceMap = remapPatientResources(sourceMessage);

        Parameters parameters = new Parameters()
                .addParameter(createStringParameter("ParametersType", parametersType))
                .addParameter(createStringParameter("MajorPatientUuid", majorPatientUuid.toString()))
                .addParameter(createStringParameter("MinorPatientUuid", minorPatientUuid.toString()))
                .addParameter(MergeCommon.createOldToNewResourceMap(resourceMap));

        parameters.setId(getParametersUuid(messageControlId, parametersType).toString());

        return parameters;
    }

    private Parameters handleMergeEncountersForSamePatient(AdtMessage sourceMessage) throws TransformException, MapperException, ParseException {

        final String parametersType = "MergeEpisodesForTheSamePatient";

        String messageControlId = sourceMessage.getMshSegment().getMessageControlId();
        UUID patientUuid = getMajorPatientUuid(sourceMessage);
        UUID majorEpisodeOfCareUuid = getMajorEpisodeOfCareUuid(sourceMessage);
        UUID minorEpisodeOfCareUuid = getMinorEpisodeOfCareUuid(sourceMessage);

        Parameters parameters = new Parameters()
                .addParameter(createStringParameter("ParametersType", parametersType))
                .addParameter(createStringParameter("PatientUuid", patientUuid.toString()))
                .addParameter(createStringParameter("MajorEpisodeOfCareUuid", majorEpisodeOfCareUuid.toString()))
                .addParameter(createStringParameter("MinorEpisodeOfCareUuid", minorEpisodeOfCareUuid.toString()));

        parameters.setId(getParametersUuid(messageControlId, parametersType).toString());

        return parameters;
    }

    private Parameters handleMoveEncounterBetweenPatients(AdtMessage sourceMessage) throws TransformException, MapperException, ParseException {

        final String parametersType = "MoveEpisodeBetweenPatients";

        String messageControlId = sourceMessage.getMshSegment().getMessageControlId();
        UUID majorPatientUuid = getMajorPatientUuid(sourceMessage);
        UUID minorPatientUuid = getMinorPatientUuid(sourceMessage);
        UUID minorPatientEpisodeOfCareUuid = getMinorEpisodeOfCareUuid(sourceMessage);

        HashMap<MappedResourceUuid, UUID> resourceMap = remapEpisodeResources(sourceMessage);

        Parameters parameters = new Parameters()
                .addParameter(createStringParameter("ParametersType", parametersType))
                .addParameter(createStringParameter("MajorPatientUuid", majorPatientUuid.toString()))
                .addParameter(createStringParameter("MinorPatientUuid", minorPatientUuid.toString()))
                .addParameter(createStringParameter("MinorPatientEpisodeOfCareUuid", minorPatientEpisodeOfCareUuid.toString()))
                .addParameter(MergeCommon.createOldToNewResourceMap(resourceMap));

        parameters.setId(getParametersUuid(messageControlId, parametersType).toString());

        return parameters;
    }

    private HashMap<MappedResourceUuid, UUID> remapPatientResources(AdtMessage sourceMessage) throws MapperException, ParseException {
        List<MappedResourceUuid> mappedResourceUuids = BartsPatientTransform.getBartsPatientResourceUuidMappings(sourceMessage.getMrgSegment(), mapper);

        HashMap<MappedResourceUuid, UUID> map = new HashMap<>();


        return map;
    }

    private HashMap<MappedResourceUuid, UUID> remapEpisodeResources(AdtMessage sourceMessage) throws MapperException, ParseException {
        List<MappedResourceUuid> mappedResourceUuids = BartsEpisodeOfCareTransform.getBartsPatientResourceUuidMappings(sourceMessage.getMrgSegment(), mapper);

        HashMap<MappedResourceUuid, UUID> map = new HashMap<>();


        return map;
    }

    private static Parameters.ParametersParameterComponent createStringParameter(String name, String value) {
        return MergeCommon.createStringParameter(name, value);
    }

    private UUID getParametersUuid(String messageControlId, String parametersType) throws MapperException {
        return mapper.getResourceMapper().mapParametersUuid(messageControlId, parametersType);
    }

    private UUID getMajorPatientUuid(AdtMessage sourceMessage) throws TransformException, MapperException {
        UUID majorPatientUuid = BartsPatientTransform.getBartsMappedPatientUuid(sourceMessage, mapper);

        if (majorPatientUuid == null)
            throw new TransformException("MajorPatientUuid is null");

        return majorPatientUuid;
    }

    private UUID getMinorPatientUuid(AdtMessage sourceMessage) throws TransformException, ParseException, MapperException {
        if (!sourceMessage.hasMrgSegment())
            throw new TransformException("MRG segment is empty");

        UUID minorPatientUuid = BartsPatientTransform.getBartsMappedPatientUuid(sourceMessage.getMrgSegment(), mapper);

        if (minorPatientUuid == null)
            throw new TransformException("MinorPatientUuid is null");

        return minorPatientUuid;
    }

    private UUID getMajorEpisodeOfCareUuid(AdtMessage sourceMessage) throws MapperException, TransformException {
        UUID majorEpisodeOfCareUuid = BartsEpisodeOfCareTransform.getBartsMappedEpisodeOfCareUuid(sourceMessage, mapper);

        if (majorEpisodeOfCareUuid == null)
            throw new TransformException("MajorEpisodeOfCareUuid is null");

        return majorEpisodeOfCareUuid;
    }

    private UUID getMinorEpisodeOfCareUuid(AdtMessage sourceMessage) throws MapperException, TransformException, ParseException {
        if (!sourceMessage.hasMrgSegment())
            throw new TransformException("MRG segment is empty");

        UUID minorEpisodeOfCareUuid = BartsEpisodeOfCareTransform.getBartsMappedEpisodeOfCareUuid(sourceMessage.getMrgSegment(), mapper);

        if (minorEpisodeOfCareUuid == null)
            throw new TransformException("MinorEpisodeOfCareUuid is null");

        return minorEpisodeOfCareUuid;
    }
}
