package org.endeavourhealth.hl7transform.transforms.barts;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7transform.Transform;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.transforms.barts.pretransform.BartsPreTransform;
import org.endeavourhealth.hl7transform.transforms.barts.transforms.MessageHeaderTransform;
import org.endeavourhealth.hl7transform.transforms.barts.transforms.OrganizationTransform;
import org.endeavourhealth.hl7transform.transforms.barts.transforms.PatientTransform;
import org.hl7.fhir.instance.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BartsAdtTransform extends Transform {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();

    public BartsAdtTransform() {
    }

    public List<String> getSupportedSendingFacilities() {
        return Arrays.asList(new String[] { BartsConstants.sendingFacility });
    }

    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    public AdtMessage preTransform(AdtMessage sourceMessage) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);

        return BartsPreTransform.preTransform(sourceMessage);
    }

    public Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);
        validateSegmentCounts(sourceMessage);

        ResourceContainer targetResources = new ResourceContainer();

        ///////////////////////////////////////////////////////////////////////////
        // create main hospital organisation
        //
        OrganizationTransform organizationTransform = new OrganizationTransform(mapper, targetResources);
        Organization mainHospitalOrganisation = organizationTransform.createBartsManagingOrganisation(sourceMessage);
        targetResources.addResource(mainHospitalOrganisation, ResourceTag.MainHospitalOrganisation);

        ///////////////////////////////////////////////////////////////////////////
        // create message header
        //
        MessageHeaderTransform messageHeaderTransform = new MessageHeaderTransform(mapper, targetResources);
        MessageHeader messageHeader = messageHeaderTransform.transform(sourceMessage);
        targetResources.addResource(messageHeader);

        ///////////////////////////////////////////////////////////////////////////
        // create patient
        //
        PatientTransform patientTransform = new PatientTransform(mapper, targetResources);
        Patient patient = patientTransform.transform(sourceMessage);
        targetResources.addResource(patient, ResourceTag.PatientSubject);

        ///////////////////////////////////////////////////////////////////////////
        // create bundle
        //
        return targetResources
                .orderByResourceType()
                .createBundle();
    }

    private void validateSendingFacility(AdtMessage sourceMessage) throws TransformException {
        Validate.notNull(sourceMessage.getMshSegment());

        if (!supportsSendingFacility(sourceMessage.getMshSegment().getSendingFacility()))
            throw new TransformException("Sending facility of " + sourceMessage.getMshSegment().getSendingFacility() + " not recognised");
    }

    private void validateSegmentCounts(AdtMessage sourceMessage) throws TransformException {
        // improve segment count definitions

        String messageType = StringUtils.trim(sourceMessage.getMshSegment().getMessageType());
        List<String> swapMessageTypes = Arrays.asList("ADT^A17");
        List<String> mergeMessageTypes = Arrays.asList("ADT^A17", "ADT^A34", "ADT^A35", "ADT^A44");

        validateExactlyOneSegment(sourceMessage, SegmentName.MSH);
        validateExactlyOneSegment(sourceMessage, SegmentName.EVN);

        if (!swapMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.PID);
        else
            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PID, 2, 2);

        if (!mergeMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.PD1);
        else
            validateNoSegments(sourceMessage, SegmentName.PD1);

        if (!swapMessageTypes.contains(messageType)) {
            validateZeroOrOneSegments(sourceMessage, SegmentName.PV1);

            long segmentCount = sourceMessage.getSegmentCount(SegmentName.PV1);

            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PV2, 0, segmentCount);
        }
    }

    private void validateNoSegments(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 0L, 0L);
    }

    private void validateZeroOrOneSegments(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 0L, 1L);
    }

    private void validateExactlyOneSegment(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 1L, 1L);
    }

    private void validateMinAndMaxSegmentCount(AdtMessage sourceMessage, String segmentName, long min, long max) throws TransformException {
        if (sourceMessage.getSegmentCount(segmentName) < min)
            throw new TransformException(segmentName + " segment exists less than " + Long.toString(min) + " time(s)");

        if (sourceMessage.getSegmentCount(segmentName) > max)
            throw new TransformException(segmentName + " exists more than " + Long.toString(max) + " time(s)");
    }
}
