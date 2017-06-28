package org.endeavourhealth.hl7transform;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.Bundle;

import java.util.HashMap;
import java.util.List;

public abstract class Transform {
    public abstract List<String> getSupportedSendingFacilities();
    public abstract HashMap<String, Class<? extends Segment>> getZSegments();
    public abstract AdtMessage preTransform(AdtMessage sourceMessage) throws Exception;
    public abstract Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception;

    public boolean supportsSendingFacility(String sendingFacility) throws TransformException {
        Validate.notEmpty(sendingFacility);

        if (getSupportedSendingFacilities() == null)
            return false;

        return getSupportedSendingFacilities()
                .stream()
                .anyMatch(t -> sendingFacility.equalsIgnoreCase(StringUtils.trim(t)));
    }

    protected void validateNoSegments(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 0L, 0L);
    }

    protected void validateZeroOrOneSegments(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 0L, 1L);
    }

    protected void validateExactlyOneSegment(AdtMessage sourceMessage, String segmentName) throws TransformException {
        validateMinAndMaxSegmentCount(sourceMessage, segmentName, 1L, 1L);
    }

    protected void validateMinAndMaxSegmentCount(AdtMessage sourceMessage, String segmentName, long min, long max) throws TransformException {
        if (sourceMessage.getSegmentCount(segmentName) < min)
            throw new TransformException(segmentName + " segment exists less than " + Long.toString(min) + " time(s)");

        if (sourceMessage.getSegmentCount(segmentName) > max)
            throw new TransformException(segmentName + " exists more than " + Long.toString(max) + " time(s)");
    }
}
