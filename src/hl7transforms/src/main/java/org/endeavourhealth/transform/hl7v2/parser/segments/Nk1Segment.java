package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;
import org.endeavourhealth.transform.hl7v2.parser.Hl7DateTime;

import java.util.List;

public class Nk1Segment extends Segment {
    public Nk1Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public List<Xpn> getNKName() { return this.getFieldAsDatatypes(2, Xpn.class); }
    public Ce getRelationship() { return this.getFieldAsDatatype(3, Ce.class); }
    public List<Xad> getAddresses() { return this.getFieldAsDatatypes(4, Xad.class); }
    public List<Xtn> getPhoneNumber() { return this.getFieldAsDatatypes(5, Xtn.class); }
    public List<Xtn> getBusinessPhoneNumber() { return this.getFieldAsDatatypes(6, Xtn.class); }
    public Ce getContactRole() { return this.getFieldAsDatatype(7, Ce.class); }
    public Hl7DateTime getStartDate() throws ParseException { return this.getFieldAsHl7Date(8); }
    public Hl7DateTime getEndDate() throws ParseException { return this.getFieldAsHl7Date(9); }
    public String getNextOfKinAssociatedPartiesJobTitle() { return this.getFieldAsString(10); }
    public Jcc getNextOKinAssociatedPartiesJobCodeClass() { return this.getFieldAsDatatype(11, Jcc.class); }
    public Cx getNextOfKinAssociatedPartiesEmployeeNumber() { return this.getFieldAsDatatype(12, Cx.class); }
    public List<Xon> getOrganizationNameNK1() { return this.getFieldAsDatatypes(13, Xon.class); }
    public Ce getMaritalStatus() { return this.getFieldAsDatatype(14, Ce.class); }
    public String getSex() { return this.getFieldAsString(15); }
    public Hl7DateTime getDateTimeOfBirth() throws ParseException { return this.getFieldAsHl7Date(16); }
    public String getLivingDependency() { return this.getFieldAsString(17); }
    public String getAmbulatoryStatus() { return this.getFieldAsString(18); }
    public List<Ce> getCitizenship() { return this.getFieldAsDatatypes(19, Ce.class); }
    public Ce getPrimaryLanguage() { return this.getFieldAsDatatype(20, Ce.class); }
    public String getLivingArrangement() { return this.getFieldAsString(21); }
    public Ce getPublicityCode() { return this.getFieldAsDatatype(22, Ce.class); }
    public String getProtectionIndicator() { return this.getFieldAsString(23); }
    public String getStudentIndicator() { return this.getFieldAsString(24); }
    public Ce getReligion() { return this.getFieldAsDatatype(25, Ce.class); }
    public List<Xpn> getMothersMaidenName() { return this.getFieldAsDatatypes(26, Xpn.class); }
    public Ce getNationality() { return this.getFieldAsDatatype(27, Ce.class); }
    public List<Ce> getEthnicGroup() { return this.getFieldAsDatatypes(28, Ce.class); }
    public List<Ce> getContactReason() { return this.getFieldAsDatatypes(29, Ce.class); }
    public List<Xpn> getContactPersonsName() { return this.getFieldAsDatatypes(30, Xpn.class); }
    public List<Xtn> getContactPersonsTelephoneNumber() { return this.getFieldAsDatatypes(31, Xtn.class); }
    public List<Xad> getContactPersonsAddress() { return this.getFieldAsDatatypes(32, Xad.class); }
    public List<Cx> getNextOfKinAssociatedPartysIdentifiers() { return this.getFieldAsDatatypes(33, Cx.class); }
    public String getJobStatus() { return this.getFieldAsString(34); }
    public List<Ce> getRace() { return this.getFieldAsDatatypes(35, Ce.class); }
    public String getHandicap() { return this.getFieldAsString(36); }
    public String getContactPersonSocialSecurityNumber() { return this.getFieldAsString(37); }
}
