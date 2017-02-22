package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Hd extends Datatype {
    public Hd(GenericDatatype datatype) {
        super(datatype);
    }

    public String getNamespaceId() { return this.getComponentAsString(1); }
    public String getUniversalId() { return this.getComponentAsString(2); }
    public String getUniversalIdType() { return this.getComponentAsString(3); }
}
