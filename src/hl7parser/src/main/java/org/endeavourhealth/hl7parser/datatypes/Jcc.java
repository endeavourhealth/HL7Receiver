package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Jcc extends Datatype {
    public Jcc(GenericDatatype datatype) {
        super(datatype);
    }

    public String getJobCode() { return this.getComponentAsString(1); }
    public String getJobClass() { return this.getComponentAsString(2); }
}
