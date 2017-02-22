package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;

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
