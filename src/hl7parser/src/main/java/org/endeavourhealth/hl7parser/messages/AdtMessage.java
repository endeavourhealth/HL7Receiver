package org.endeavourhealth.hl7parser.messages;

import org.endeavourhealth.hl7parser.Message;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.segments.*;

import java.util.HashMap;
import java.util.List;

public class AdtMessage extends Message {
    public AdtMessage(String message) throws ParseException {
        super(message);
    }

    public AdtMessage(String message, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        super(message, zSegmentDefinitions);
    }

    public boolean hasMshSegment() { return super.hasSegment(SegmentName.MSH); }
    public boolean hasEvnSegment() { return super.hasSegment(SegmentName.EVN); }
    public boolean hasPidSegment() { return super.hasSegment(SegmentName.PID); }
    public boolean hasPd1Segment() { return super.hasSegment(SegmentName.PD1); }
    public boolean hasNk1Segment() { return super.hasSegment(SegmentName.NK1); }
    public boolean hasPv1Segment() { return super.hasSegment(SegmentName.PV1); }
    public boolean hasPv2Segment() { return super.hasSegment(SegmentName.PV2); }
    public boolean hasObxSegment() { return super.hasSegment(SegmentName.OBX); }
    public boolean hasDg1Segment() { return super.hasSegment(SegmentName.DG1); }
    public boolean hasAccSegment() { return super.hasSegment(SegmentName.ACC); }
    public boolean hasMrgSegment() { return super.hasSegment(SegmentName.MRG); }

    public MshSegment getMshSegment() {
        return super.getSegment(SegmentName.MSH, MshSegment.class);
    }
    public EvnSegment getEvnSegment() { return super.getSegment(SegmentName.EVN, EvnSegment.class); }
    public PidSegment getPidSegment() { return super.getSegment(SegmentName.PID, PidSegment.class); }
    public Pd1Segment getPd1Segment() { return super.getSegment(SegmentName.PD1, Pd1Segment.class); }
    public List<Nk1Segment> getNk1Segments() { return super.getSegments(SegmentName.NK1, Nk1Segment.class); }
    public Pv1Segment getPv1Segment() { return super.getSegment(SegmentName.PV1, Pv1Segment.class); }
    public Pv2Segment getPv2Segment() { return super.getSegment(SegmentName.PV2, Pv2Segment.class); }
    public List<ObxSegment> getObxSegments() { return super.getSegments(SegmentName.OBX, ObxSegment.class); }
    public List<Dg1Segment> getDg1Segments() { return super.getSegments(SegmentName.DG1, Dg1Segment.class); }
    public AccSegment getAccSegment() { return super.getSegment(SegmentName.ACC, AccSegment.class); }
    public MrgSegment getMrgSegment() { return super.getSegment(SegmentName.MRG, MrgSegment.class); }
}
