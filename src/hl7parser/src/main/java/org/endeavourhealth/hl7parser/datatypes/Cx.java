package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Cx extends Datatype implements CxInterface {

    public Cx(GenericDatatype datatype) {
        super(datatype);
    }

    public String getId() { return this.getComponentAsString(1); }
    public String getCheckDigit() { return this.getComponentAsString(2); }
    public String getCheckDigitCodeScheme() { return this.getComponentAsString(3); }
    public String getAssigningAuthority() { return this.getComponentAsString(4); }
    public String getIdentifierTypeCode() { return this.getComponentAsString(5); }
    public String getAssigningFacility() { return this.getComponentAsString(6); }
}
