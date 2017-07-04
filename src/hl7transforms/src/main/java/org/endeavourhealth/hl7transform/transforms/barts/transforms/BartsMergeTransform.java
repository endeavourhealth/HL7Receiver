package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.messages.AdtMessageType;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.transform.MergeCommon;
import org.endeavourhealth.hl7transform.common.transform.PatientCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.resource.MappedResourceUuid;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.Parameters;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.*;

public class BartsMergeTransform extends ResourceTransformBase {

    public static final String AdtA34MergeTwoPatients = AdtMessageType.AdtA34;
    public static final String AdtA35MergeEncountersForSamePatient = AdtMessageType.AdtA35;
    public static final String AdtA44MoveEncounterBetweenPatients = AdtMessageType.AdtA44;

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
        UUID majorPatientUuid = getPatientUuid(sourceMessage, SegmentName.PID);
        UUID minorPatientUuid = getPatientUuid(sourceMessage, SegmentName.MRG);

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
        UUID patientUuid = getPatientUuid(sourceMessage, SegmentName.PID);
        UUID majorEpisodeOfCareUuid = getEpisodeOfCareUuid(sourceMessage, SegmentName.PID, SegmentName.PV1);
        UUID minorEpisodeOfCareUuid = getEpisodeOfCareUuid(sourceMessage, SegmentName.PID, SegmentName.MRG);

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
        UUID majorPatientUuid = getPatientUuid(sourceMessage, SegmentName.PID);
        UUID minorPatientUuid = getPatientUuid(sourceMessage, SegmentName.MRG);
        UUID minorPatientEpisodeOfCareUuid = getEpisodeOfCareUuid(sourceMessage, SegmentName.MRG, SegmentName.MRG);

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

    private HashMap<MappedResourceUuid, UUID> remapPatientResources(AdtMessage sourceMessage) throws MapperException, ParseException, TransformException {
        String majorPatientIdentifierValue = getPatientIdentifierValue(sourceMessage, SegmentName.PID);
        String minorPatientIdentifierValue = getPatientIdentifierValue(sourceMessage, SegmentName.MRG);

        return mapper.getResourceMapper().remapPatientResourceUuids(
                null,
                BartsConstants.primaryPatientIdentifierAssigningAuthority,
                majorPatientIdentifierValue,
                minorPatientIdentifierValue);
    }

    private HashMap<MappedResourceUuid, UUID> remapEpisodeResources(AdtMessage sourceMessage) throws MapperException, ParseException, TransformException {
        String majorPatientIdentifierValue = getPatientIdentifierValue(sourceMessage, SegmentName.PID);
        String minorPatientIdentifierValue = getPatientIdentifierValue(sourceMessage, SegmentName.MRG);
        String episodeOfCareIdentifierValue = getEpisodeIdentifierValue(sourceMessage, SegmentName.MRG);

        return mapper.getResourceMapper().remapEpisodeResourceUuids(
                null,
                BartsConstants.primaryPatientIdentifierAssigningAuthority,
                majorPatientIdentifierValue,
                minorPatientIdentifierValue,
                BartsConstants.primaryEpisodeIdentifierTypeCode,
                null,
                episodeOfCareIdentifierValue);
    }

    private static Parameters.ParametersParameterComponent createStringParameter(String name, String value) {
        return MergeCommon.createStringParameter(name, value);
    }

    private UUID getParametersUuid(String messageControlId, String parametersType) throws MapperException {
        return mapper.getResourceMapper().mapParametersUuid(messageControlId, parametersType);
    }

    private UUID getPatientUuid(AdtMessage sourceMessage, String sourceSegmentName) throws TransformException, MapperException, ParseException {
        Validate.notEmpty(sourceSegmentName);

        List<Cx> patientIdentifierList = getPatientIdentifierList(sourceMessage, sourceSegmentName);
        UUID patientUuid = BartsPatientTransform.getBartsMappedPatientUuid(patientIdentifierList, mapper);

        if (patientUuid == null)
            throw new TransformException("PatientUuid is null");

        return patientUuid;
    }

    private UUID getEpisodeOfCareUuid(AdtMessage sourceMessage, String patientIdentifierSourceSegmentName, String episodeIdentifierSourceSegmentName) throws MapperException, TransformException, ParseException {
        Validate.notNull(sourceMessage);
        Validate.notEmpty(patientIdentifierSourceSegmentName);
        Validate.notEmpty(episodeIdentifierSourceSegmentName);

        List<Cx> patientIdentifierList = getPatientIdentifierList(sourceMessage, patientIdentifierSourceSegmentName);
        List<Cx> episodeIdentifierList = getEpisodeIdentifierList(sourceMessage, episodeIdentifierSourceSegmentName);

        UUID episodeOfCareUuid = BartsEpisodeOfCareTransform.getBartsMappedEpisodeOfCareUuid(patientIdentifierList, episodeIdentifierList, mapper);

        if (episodeOfCareUuid == null)
            throw new TransformException("EpisodeOfCareUuid is null");

        return episodeOfCareUuid;
    }

    private static String getPatientIdentifierValue(AdtMessage sourceMessage, String sourceSegmentName) throws TransformException, ParseException {
        return BartsPatientTransform.getBartsPrimaryPatientIdentifierValue(getPatientIdentifierList(sourceMessage, sourceSegmentName));
    }

    private static String getEpisodeIdentifierValue(AdtMessage sourceMessage, String sourceSegmentName) throws TransformException, ParseException {
        return BartsEpisodeOfCareTransform.getBartsPrimaryEpisodeIdentifierValue(getEpisodeIdentifierList(sourceMessage, sourceSegmentName));
    }

    private static List<Cx> getPatientIdentifierList(AdtMessage sourceMessage, String sourceSegmentName) throws TransformException, ParseException {
        switch (sourceSegmentName) {
            case SegmentName.PID: return PatientCommon.getAllPatientIdentifiers(sourceMessage);
            case SegmentName.MRG: return sourceMessage.getMrgSegment().getPriorPatientIdentifierList();
            default: throw new TransformException("Patient identifier not found in segment " + sourceSegmentName);
        }
    }

    private static List<Cx> getEpisodeIdentifierList(AdtMessage sourceMessage, String sourceSegmentName) throws TransformException, ParseException {
        switch (sourceSegmentName) {
            case SegmentName.PV1: return Arrays.asList(sourceMessage.getPv1Segment().getVisitNumber());
            case SegmentName.MRG: return Arrays.asList(sourceMessage.getMrgSegment().getPriorVisitNumber());
            default: throw new TransformException("Patient identifier not found in segment " + sourceSegmentName);
        }
    }
}
