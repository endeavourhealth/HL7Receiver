package org.endeavourhealth.transform.hl7v2.parser.messages;

import org.endeavourhealth.transform.hl7v2.parser.Message;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.*;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZpiSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZqaSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZviSegment;

import java.util.HashMap;
import java.util.List;

public class AdtMessage extends Message {
    public AdtMessage(String message) throws ParseException {
        super(message);
    }

    public AdtMessage(String message, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        super(message, zSegmentDefinitions);
    }

    public boolean hasMshSegment() {
        return super.hasSegment(SegmentName.MSH);
    }
    public boolean hasEvnSegment() { return super.hasSegment(SegmentName.EVN); }
    public boolean hasPidSegment() { return super.hasSegment(SegmentName.PID); }
    public boolean hasPd1Segment() { return super.hasSegment(SegmentName.PD1); }
    public boolean hasNk1Segment() { return super.hasSegment(SegmentName.NK1); }
    public boolean hasPv1Segment() { return super.hasSegment(SegmentName.PV1); }
    public boolean hasPv2Segment() { return super.hasSegment(SegmentName.PV2); }
    public boolean hasObxSegment() { return super.hasSegment(SegmentName.OBX); }
    public boolean hasDg1Segment() { return super.hasSegment(SegmentName.DG1); }
    public boolean hasAccSegment() { return super.hasSegment(SegmentName.ACC); }

    //Homerton profile
    public boolean hasZpiSegment() { return super.hasSegment(SegmentName.ZPI); }
    public boolean hasZqaSegment() { return super.hasSegment(SegmentName.ZQA); }
    public boolean hasZviSegment() { return super.hasSegment(SegmentName.ZVI); }


    public MshSegment getMshSegment() {
        return (MshSegment) super.getSegment(SegmentName.MSH);
    }
    public EvnSegment getEvnSegment() { return (EvnSegment) super.getSegment(SegmentName.EVN); }
    public PidSegment getPidSegment() { return (PidSegment) super.getSegment(SegmentName.PID); }
    public Pd1Segment getPd1Segment() { return (Pd1Segment) super.getSegment(SegmentName.PD1); }
    public List<Nk1Segment> getNk1Segments() { return (List<Nk1Segment>)super.getSegments(SegmentName.NK1); }
    public Pv1Segment getPv1Segment() { return (Pv1Segment) super.getSegment(SegmentName.PV1); }
    public Pv2Segment getPv2Segment() { return (Pv2Segment) super.getSegment(SegmentName.PV2); }
    public List<ObxSegment> getObxSegments() { return (List<ObxSegment>)super.getSegments(SegmentName.OBX); }
    public List<Dg1Segment> getDg1Segments() { return (List<Dg1Segment>)super.getSegments(SegmentName.DG1); }
    public AccSegment getAccSegment() { return (AccSegment) super.getSegment(SegmentName.ACC); }

    public ZpiSegment getZpiSegment() { return (ZpiSegment) super.getSegment(SegmentName.ZPI); }
    public ZqaSegment getZqaSegment() { return (ZqaSegment) super.getSegment(SegmentName.ZQA); }
    public ZviSegment getZviSegment() { return (ZviSegment) super.getSegment(SegmentName.ZVI); }
}
