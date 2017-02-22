package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.*;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;

import java.time.LocalDateTime;
import java.util.List;

public class Pv1Segment extends Segment {
    public Pv1Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public String getPatientClass() { return this.getFieldAsString(2); }
    public Pl getAssignedPatientLocation() { return this.getFieldAsDatatype(3, Pl.class); }
    public String getAdmissionType() { return this.getFieldAsString(4); }
    public Cx getPreadmitNumber() { return this.getFieldAsDatatype(5, Cx.class); }
    public Pl getPriorPatientLocation() { return this.getFieldAsDatatype(6, Pl.class); }
    public List<Xcn> getAttendingDoctor() { return this.getFieldAsDatatypes(7, Xcn.class); }
    public List<Xcn> getReferringDoctor() { return this.getFieldAsDatatypes(8, Xcn.class); }
    public List<Xcn> getConsultingDoctor() { return this.getFieldAsDatatypes(9, Xcn.class); }
    public String getHospitalService() { return this.getFieldAsString(10); }
    public Pl getTemporaryLocation() { return this.getFieldAsDatatype(11, Pl.class); }
    public String getPreadmitTestIndicator() { return this.getFieldAsString(12); }
    public String getReadmissionIndicator() { return this.getFieldAsString(13); }
    public String getAdmitSource() { return this.getFieldAsString(14); }
    public List<String> getAmbulatoryStatus() { return this.getFieldAsStringList(15); }
    public String getVIPIndicator() { return this.getFieldAsString(16); }
    public List<Xcn> getAdmittingDoctor() { return this.getFieldAsDatatypes(17, Xcn.class); }
    public String getPatientType() { return this.getFieldAsString(18); }
    public Cx getVisitNumber() { return this.getFieldAsDatatype(19, Cx.class); }
    //public Fc getFinancialClass() { return this.getFieldAsDatatype(20, Fc.class); }  Financial Class???
    public String getChargePriceIndicator() { return this.getFieldAsString(21); }
    public String getCourtesyCode() { return this.getFieldAsString(22); }
    public String getCreditRating() { return this.getFieldAsString(23); }
    public List<String> getContractCode() { return this.getFieldAsStringList(24); }
    public LocalDateTime getContractEffectiveDate() throws ParseException { return this.getFieldAsDate(25); }
    public String getContractAmount() { return this.getFieldAsString(26); }
    public String getContractPeriod() { return this.getFieldAsString(27); }
    public String getInterestCode() { return this.getFieldAsString(28); }
    public String getTransferToBadDebtCode() { return this.getFieldAsString(29); }
    public LocalDateTime getTransferToBadDebtDate() throws ParseException { return this.getFieldAsDate(30); }
    public String getBadDebtAgencyCode() { return this.getFieldAsString(31); }
    public String getBadDebtTransferAmount() { return this.getFieldAsString(32); }
    public String getBadDebtRecoveryAmount() { return this.getFieldAsString(33); }
    public String getDeleteAccountIndicator() { return this.getFieldAsString(34); }
    public LocalDateTime getDeleteAccountDate() throws ParseException { return this.getFieldAsDate(35); }
    public String getDischargeDisposition() { return this.getFieldAsString(36); }
    public String getDischargedToLocation() { return this.getFieldAsString(37); }
    public Ce getDietType() { return this.getFieldAsDatatype(38, Ce.class); }
    public String getServicingFacility() { return this.getFieldAsString(39); }
    public String getBedStatus() { return this.getFieldAsString(40); }
    public String getAccountStatus() { return this.getFieldAsString(41); }
    public Pl getPendingLocation() { return this.getFieldAsDatatype(42, Pl.class); }
    public Pl getPriorTemporaryLocation() { return this.getFieldAsDatatype(43, Pl.class); }
    public LocalDateTime getAdmitDateTime() throws ParseException { return this.getFieldAsDate(44); }
    public LocalDateTime getDischargeDateTime() throws ParseException { return this.getFieldAsDate(45); }
    public String getCurrentPatientBalance() { return this.getFieldAsString(46); }
    public String getTotalCharges() { return this.getFieldAsString(47); }
    public String getTotalAdjustments() { return this.getFieldAsString(48); }
    public String getTotalPayments() { return this.getFieldAsString(49); }
    public Cx getAlternateVisitID() { return this.getFieldAsDatatype(50, Cx.class); }
    public String getVisitIndicator() { return this.getFieldAsString(51); }
    public List<Xcn> getOtherHealthcareProvider() { return this.getFieldAsDatatypes(52, Xcn.class); }

}
