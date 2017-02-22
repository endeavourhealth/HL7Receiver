package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Ce;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xad;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;

import java.time.LocalDateTime;

public class AccSegment extends Segment {
    public AccSegment(String segmentText, Seperators seperators) throws ParseException {
        super(segmentText, seperators);
    }

    public LocalDateTime getAccidentDateTime() throws ParseException { return this.getFieldAsDate(1); }
    public Ce getAccidentCode() { return this.getFieldAsDatatype(2, Ce.class); }
    public String getAccidentLocation() { return this.getFieldAsString(3); }
    public Ce getAutoAccidentState() { return this.getFieldAsDatatype(4, Ce.class); }
    public String getAccidentJobRelatedIndicator() { return this.getFieldAsString(5); }
    public String getAccidentDeathIndicator() { return this.getFieldAsString(6); }
    public Xcn getEnteredBy() { return this.getFieldAsDatatype(7, Xcn.class); }
    public String getAccidentDescription() { return this.getFieldAsString(8); }
    public String getBroughtInBy() { return this.getFieldAsString(9); }
    public String getPoliceNotifiedIndicator() { return this.getFieldAsString(10); }
    public Xad getAccidentAddress() { return this.getFieldAsDatatype(11, Xad.class); }
}