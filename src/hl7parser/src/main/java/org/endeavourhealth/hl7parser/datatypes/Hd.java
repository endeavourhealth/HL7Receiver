package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Hd extends Datatype {
    public Hd(GenericDatatype datatype) {
        super(datatype);
    }

    public String getNamespaceId() { return this.getComponentAsString(1); }
    public String getUniversalId() { return this.getComponentAsString(2); }
    public String getUniversalIdType() { return this.getComponentAsString(3); }
}
