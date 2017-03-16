package org.endeavourhealth.transform.hl7v2.profiles.homerton.segments;

import org.endeavourhealth.transform.hl7v2.parser.*;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;
import org.endeavourhealth.transform.hl7v2.parser.Hl7DateTime;

import java.util.List;

public class ZviSegment extends Segment {
    public ZviSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    //Segment cannot repeat
    public String getTrauma() { return this.getFieldAsString(1); }
    public Hl7DateTime getTraumaDate() throws ParseException { return this.getFieldAsHl7Date(2); }
    public Hl7DateTime getLastTraumaDate() throws ParseException { return this.getFieldAsHl7Date(3); }
    public String getReferringComment() { return this.getFieldAsString(4); }
    public String getServiceCategory() { return this.getFieldAsString(6); }
    public String getAdmitMode() { return this.getFieldAsString(9); }
    public String getRequestAccomodation() { return this.getFieldAsString(10); }
    public String getInformationGivenBy() { return this.getFieldAsString(11); }
    public String getTriageCode() { return this.getFieldAsString(12); }
    public Hl7DateTime getTriageDate() throws ParseException { return this.getFieldAsHl7Date(13); }
    public String getAccomodationReasonCode() { return this.getFieldAsString(14); }
    public Hl7DateTime getAssignToLocationDate() throws ParseException { return this.getFieldAsHl7Date(15); }
    public String getPendingEncounterType() { return this.getFieldAsString(16); }
    public String getPendingServiceCategory() { return this.getFieldAsString(17); }
    public String getPendingMedicalService() { return this.getFieldAsString(18); }
    public String getPendingAccomodation() { return this.getFieldAsString(19); }
    public String getPendingAccomodationReason() { return this.getFieldAsString(20); }
    public String getPendingAlternateLevelOfCare() { return this.getFieldAsString(21); }
    public String getPendingIsolation() { return this.getFieldAsString(22); }
    public Hl7DateTime getEstimatedTransferDate() throws ParseException { return this.getFieldAsHl7Date(23); }
    public String getPendingDischargeToLocation() { return this.getFieldAsString(24); }
    public String getPendingStatus() { return this.getFieldAsString(25); }
    public String getPendingPriority() { return this.getFieldAsString(26); }
    public List<Xcn> getPendingAttendingPhysician() { return this.getFieldAsDatatypes(27, Xcn.class); }
    public String getPreAdmitTesting() { return this.getFieldAsString(28); }
}
