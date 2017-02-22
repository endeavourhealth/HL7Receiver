package org.endeavourhealth.transform.hl7v2.profiles;

import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;

import java.util.HashMap;

public interface TransformProfile {
    HashMap<String, Class<? extends Segment>> getZSegments();
    AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException;
    CodeMapper getCodeMapper(String originatingSystemId);
}
