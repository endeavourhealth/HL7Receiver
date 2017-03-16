package org.endeavourhealth.hl7parser.segments;

import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Ce;

import java.util.List;

public class Al1Segment extends Segment {
    public Al1Segment(String segmentText, Seperators seperators) throws ParseException {
        super(segmentText, seperators);
    }

    public String getSetId() { return this.getFieldAsString(1); }
    public String getAlleryType() { return this.getFieldAsString(2); }
    public Ce getAllergyCode() { return this.getFieldAsDatatype(3, Ce.class); }
    public List<String> getAllergyReaction() { return this.getFieldAsStringList(4); }
    public Hl7DateTime getIdentificationDate() throws ParseException { return this.getFieldAsHl7Date(5); }
}