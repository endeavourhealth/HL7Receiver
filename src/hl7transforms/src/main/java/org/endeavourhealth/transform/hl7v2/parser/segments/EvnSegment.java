package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;

import java.time.LocalDateTime;
import java.util.List;

public class EvnSegment extends Segment {
    public EvnSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public String getEventTypeCode() { return this.getFieldAsString(1); }
    public LocalDateTime getRecordedDateTime() throws ParseException { return this.getFieldAsDate(2); }
    public LocalDateTime getPlannedDateTime() throws ParseException { return this.getFieldAsDate(3); }
    public String getEventReasonCode() { return this.getFieldAsString(4); }
    public List<Xcn> getOperators() { return this.getFieldAsDatatypes(5, Xcn.class); }
    public LocalDateTime getOccurredDateTime() throws ParseException { return this.getFieldAsDate(6); }
}
