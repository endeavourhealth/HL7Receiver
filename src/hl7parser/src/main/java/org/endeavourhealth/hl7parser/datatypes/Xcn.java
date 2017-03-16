package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

// extended composite id and name for persons
public class Xcn extends Datatype implements XpnInterface, CxInterface {
    public Xcn(GenericDatatype datatype) { super(datatype); }

    public String getId() { return this.getComponentAsString(1); }
    public String getFamilyName() { return this.getComponentAsString(2); }
    public String getGivenName() { return this.getComponentAsString(3); }
    public String getMiddleName() { return this.getComponentAsString(4); }
    public String getSuffix() { return this.getComponentAsString(5); }
    public String getPrefix() { return this.getComponentAsString(6);}
    public String getDegree() { return this.getComponentAsString(7); }
    public String getSourceTable() { return this.getComponentAsString(8); }
    public String getAssigningAuthority() { return this.getComponentAsString(9); }
    public String getNameTypeCode() { return this.getComponentAsString(10); }
    public String getCheckDigit() { return this.getComponentAsString(11); }
    public String getCheckDigitCodeScheme() { return this.getComponentAsString(12); }
    public String getIdentifierTypeCode() { return this.getComponentAsString(13); }
    public String getAssigningFacility() { return this.getComponentAsString(14); }
    public String getNameRepresentationCode() { return this.getComponentAsString(15); }
}
