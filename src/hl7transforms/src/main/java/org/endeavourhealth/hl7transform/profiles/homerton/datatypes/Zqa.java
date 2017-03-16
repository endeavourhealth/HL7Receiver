package org.endeavourhealth.hl7transform.profiles.homerton.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Zqa extends Datatype {
    public Zqa(GenericDatatype datatype) {
        super(datatype);
    }

    public String getValueType() { return this.getComponentAsString(1); }
    public String getQuestionIdentifier() { return this.getComponentAsString(2); }
    public String getQuestionLabel() { return this.getComponentAsString(3); }
    public String getQuestionValue() { return this.getComponentAsString(4); }
}
