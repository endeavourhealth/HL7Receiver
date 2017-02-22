package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Jcc extends Datatype {
    public Jcc(GenericDatatype datatype) {
        super(datatype);
    }

    public String getJobCode() { return this.getComponentAsString(1); }
    public String getJobClass() { return this.getComponentAsString(2); }
}
