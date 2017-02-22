package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Ce;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;

import java.time.LocalDateTime;
import java.util.List;

public class ObxSegment extends Segment {
    public ObxSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public String getValueType() { return this.getFieldAsString(2); }
    public Ce getObservationIdentifier() { return this.getFieldAsDatatype(3, Ce.class); }
    public String getObservationSubID() { return this.getFieldAsString(4); }
    public String getObservationValue() { return this.getFieldAsString(5); }
    public Ce getUnits() { return this.getFieldAsDatatype(6, Ce.class); }
    public String getReferencesRange() { return this.getFieldAsString(7); }
    public String getAbnormalFlags() { return this.getFieldAsString(8); }
    public String getProbability() { return this.getFieldAsString(9); }
    public String getNatureOfAbnormalTest() { return this.getFieldAsString(10); }
    public String getObservationResultStatus() { return this.getFieldAsString(11); }
    public LocalDateTime getDateLastObsNormalValues() throws ParseException { return this.getFieldAsDate(12); }
    public String getUserDefinedAccessChecks() { return this.getFieldAsString(13); }
    public LocalDateTime getDateTimeOfTheObservation() throws ParseException { return this.getFieldAsDate(14); }
    public Ce getProducersID() { return this.getFieldAsDatatype(15, Ce.class); }
    public List<Xcn> getResponsibleObserver() { return this.getFieldAsDatatypes(16, Xcn.class); }
    public Ce getObservationMethod() { return this.getFieldAsDatatype(17, Ce.class); }
}
