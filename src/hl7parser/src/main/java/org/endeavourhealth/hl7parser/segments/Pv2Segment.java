package org.endeavourhealth.hl7parser.segments;

import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Ce;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.datatypes.Xon;

import java.util.List;

public class Pv2Segment extends Segment {
    public Pv2Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public Pl getPriorPendingLocation() { return this.getFieldAsDatatype(1, Pl.class); }
    public Ce getAccommodationCode() { return this.getFieldAsDatatype(2, Ce.class); }
    public Ce getAdmitReason() { return this.getFieldAsDatatype(3, Ce.class); }
    public Ce getTransferReason() { return this.getFieldAsDatatype(4, Ce.class); }
    public String getPatientValuables() { return this.getFieldAsString(5); }
    public String getPatientValuablesLocation() { return this.getFieldAsString(6); }
    public String getVisitUserCode() { return this.getFieldAsString(7); }
    public Hl7DateTime getExpectedAdmitDateTime() throws ParseException { return this.getFieldAsHl7Date(8); }
    public Hl7DateTime getExpectedDischargeDateTime() throws ParseException { return this.getFieldAsHl7Date(9); }
    public String getEstimatedLengthOfInpatientStay() { return this.getFieldAsString(10); }
    public String getActualLengthOfInpatientStay() { return this.getFieldAsString(11); }
    public String getVisitDescription() { return this.getFieldAsString(12); }
    public List<Xcn> getReferralSourceCode() { return this.getFieldAsDatatypes(13, Xcn.class); }
    public Hl7DateTime getPreviousServiceDate() throws ParseException { return this.getFieldAsHl7Date(14); }
    public String getEmploymentIllnessRelatedIndicator() { return this.getFieldAsString(15); }
    public String getPurgeStatusCode() { return this.getFieldAsString(16); }
    public Hl7DateTime getPurgeStatusDate() throws ParseException { return this.getFieldAsHl7Date(17); }
    public String getSpecialProgramCode() { return this.getFieldAsString(18); }
    public String getRetentionIndicator() { return this.getFieldAsString(19); }
    public String getExpectedNumberofInsurancePlans() { return this.getFieldAsString(20); }
    public String getVisitPublicityCode() { return this.getFieldAsString(21); }
    public String getVisitProtectionIndicator() { return this.getFieldAsString(22); }
    public List<Xon> getClinicOrganizationName() { return this.getFieldAsDatatypes(23, Xon.class); }
    public String getPatientStatusCode() { return this.getFieldAsString(24); }
    public String getVisitPriorityCode() { return this.getFieldAsString(25); }
    public Hl7DateTime getPreviousTreatmentDate() throws ParseException { return this.getFieldAsHl7Date(26); }
    public String getExpectedDischargeDisposition() { return this.getFieldAsString(27); }
    public Hl7DateTime getSignatureOnFileDate() throws ParseException { return this.getFieldAsHl7Date(28); }
    public Hl7DateTime getFirstSimilarIllnessDate() throws ParseException { return this.getFieldAsHl7Date(29); }
    public Ce getPatientChargeAdjustmentCode() { return this.getFieldAsDatatype(30, Ce.class); }
    public String getRecurringServiceCode() { return this.getFieldAsString(31); }
    public String getBillingMediaCode() { return this.getFieldAsString(32); }
    public Hl7DateTime getExpectedSurgeryDateTime() throws ParseException { return this.getFieldAsHl7Date(32); }
    public String getMilitaryPartnershipCode() { return this.getFieldAsString(33); }
    public String getMilitaryNonAvailabilityCode() { return this.getFieldAsString(34); }
    public String getNewbornBabyIndicator() { return this.getFieldAsString(35); }
    public String getBabyDetainedIndicator() { return this.getFieldAsString(36); }
}
