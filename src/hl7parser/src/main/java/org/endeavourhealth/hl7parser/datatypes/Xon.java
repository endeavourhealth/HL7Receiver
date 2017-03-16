package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Xon extends Datatype {

    public Xon(GenericDatatype datatype) {
        super(datatype);
    }

    public String getOrganizationName() { return this.getComponentAsString(1); }
    public String getOrganizationNameTypeCode() { return this.getComponentAsString(2); }
    public String getIdNumber() { return this.getComponentAsString(3); }
    public String getCheckDigit() { return this.getComponentAsString(4); }
    public String getCodeIdentifyingTheCheckDigitSchemeEmployed() { return this.getComponentAsString(5); }
    public String getAssigningAuthority() { return this.getComponentAsString(6); }
    public String getIdentifierTypeCode() { return this.getComponentAsString(7); }
    public String getAssigningFacilityId() { return this.getComponentAsString(8); }
    public String getNameRepresentationCode() { return this.getComponentAsString(9); }
}
