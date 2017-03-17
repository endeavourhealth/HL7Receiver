package org.endeavourhealth.hl7transform;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.Bundle;

import java.util.HashMap;
import java.util.List;

public interface Transform {
    List<String> getSupportedSendingFacilities();
    HashMap<String, Class<? extends Segment>> getZSegments();
    AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException;
    Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception;
}
