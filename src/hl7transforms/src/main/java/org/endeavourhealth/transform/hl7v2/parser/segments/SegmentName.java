package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZalSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZpiSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZqaSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZviSegment;

public abstract class SegmentName {
    public static final String AL1 = "AL1";
    public static final String EVN = "EVN";
    public static final String MSH = "MSH";
    public static final String NK1 = "NK1";
    public static final String NTE = "NTE";
    public static final String OBX = "OBX";
    public static final String PD1 = "PD1";
    public static final String PID = "PID";
    public static final String PV1 = "PV1";
    public static final String PV2 = "PV2";
    public static final String DG1 = "DG1";
    public static final String ZPI = "ZPI";
    public static final String ZQA = "ZQA";
    public static final String ZVI = "ZVI";
    public static final String ACC = "ACC";

    public static Class<? extends Segment> getSegmentClass(String segmentName) {
        if (segmentName == null)
            return null;

        switch (segmentName) {
            case AL1: return Al1Segment.class;
            case EVN: return EvnSegment.class;
            case MSH: return MshSegment.class;
            case NK1: return Nk1Segment.class;
            case NTE: return NteSegment.class;
            case OBX: return ObxSegment.class;
            case PD1: return Pd1Segment.class;
            case PID: return PidSegment.class;
            case PV1: return Pv1Segment.class;
            case PV2: return Pv2Segment.class;
            case DG1: return Dg1Segment.class;
            case ZPI: return ZpiSegment.class;
            case ZQA: return ZqaSegment.class;
            case ZVI: return ZviSegment.class;
            case ACC: return AccSegment.class;
            default: return null;
        }
    }
}
