package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Ce;

import java.time.LocalDateTime;
import java.util.List;

public class Al1Segment extends Segment {
    public Al1Segment(String segmentText, Seperators seperators) throws ParseException {
        super(segmentText, seperators);
    }

    public String getSetId() { return this.getFieldAsString(1); }
    public String getAlleryType() { return this.getFieldAsString(2); }
    public Ce getAllergyCode() { return this.getFieldAsDatatype(3, Ce.class); }
    public List<String> getAllergyReaction() { return this.getFieldAsStringList(4); }
    public LocalDateTime getIdentificationDate() throws ParseException { return this.getFieldAsDate(5); }
}