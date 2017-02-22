package org.endeavourhealth.transform.hl7v2.profiles.homerton;

import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;
import org.endeavourhealth.transform.hl7v2.profiles.TransformProfile;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZalSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZpiSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZqaSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZviSegment;

import java.util.HashMap;

public class HomertonTransformProfile implements TransformProfile {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();

    public HomertonTransformProfile() {
        zSegments.put("ZAL", ZalSegment.class);
        zSegments.put("ZPI", ZpiSegment.class);
        zSegments.put("ZQA", ZqaSegment.class);
        zSegments.put("ZVI", ZviSegment.class);
    }

    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    public AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        return HomertonPreTransform.preTransform(sourceMessage);
    }

    @Override
    public CodeMapper getCodeMapper(String originatingSystemId) {
        return new HomertonCodeMapper(originatingSystemId);
    }
}
