package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.*;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;
import org.endeavourhealth.transform.hl7v2.parser.Hl7DateTime;

import java.util.List;

public class PidSegment extends Segment {
    public PidSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public Cx getExternalPatientId() { return this.getFieldAsDatatype(2, Cx.class); }
    public List<Cx> getInternalPatientId() { return this.getFieldAsDatatypes(3, Cx.class); }
    public List<Cx> getAlternatePatientId() { return this.getFieldAsDatatypes(4, Cx.class); }
    public List<Xpn> getPatientNames() { return this.getFieldAsDatatypes(5, Xpn.class); }
    public List<Xpn> getMothersMaidenNames() { return this.getFieldAsDatatypes(6, Xpn.class); }
    public Hl7DateTime getDateOfBirth() throws ParseException { return this.getFieldAsHl7Date(7); }
    public String getSex() { return this.getFieldAsString(8); }
    public List<Xpn> getPatientAlias() { return this.getFieldAsDatatypes(9, Xpn.class); }
    public Ce getRace() { return this.getFieldAsDatatype(10, Ce.class); }
    public List<Xad> getAddresses() { return this.getFieldAsDatatypes(11, Xad.class); }
    public String getCountryCode() { return this.getFieldAsString(12); }
    public List<Xtn> getHomeTelephones() { return this.getFieldAsDatatypes(13, Xtn.class); }
    public List<Xtn> getBusinessTelephones() { return this.getFieldAsDatatypes(14, Xtn.class); }
    public Ce getPrimaryLanguage() { return this.getFieldAsDatatype(15, Ce.class); }
    public Ce getMaritalStatus() { return this.getFieldAsDatatype(16, Ce.class); }
    public Ce getReligion() { return this.getFieldAsDatatype(17, Ce.class); }
    public Cx getPatientAccountNumber() { return this.getFieldAsDatatype(18, Cx.class); }
    public String getSsnNumber() { return this.getFieldAsString(19); }
    public String getDriversLicenseNumber() { return this.getFieldAsString(20); }
    public List<Cx> getMothersIdentifiers() { return this.getFieldAsDatatypes(21, Cx.class); }
    public List<Ce> getEthnicGroups() { return this.getFieldAsDatatypes(22, Ce.class); }
    public String getBirthPlace() { return this.getFieldAsString(23); }
    public String getMultipleBirthIndicator() { return this.getFieldAsString(24); }
    public String getBirthOrder() { return this.getFieldAsString(25); }
    public List<Ce> getCitizenship() { return this.getFieldAsDatatypes(26, Ce.class); }
    public List<Ce> getVeteransMilitaryStatus() { return this.getFieldAsDatatypes(27, Ce.class); }
    public Ce getNationality() { return this.getFieldAsDatatype(28, Ce.class); }
    public Hl7DateTime getDateOfDeath() throws ParseException { return this.getFieldAsHl7Date(29); }
    public String getDeathIndicator() { return this.getFieldAsString(30); }
    public Ce getTraceStatus() { return this.getFieldAsDatatype(32, Ce.class); }
}
