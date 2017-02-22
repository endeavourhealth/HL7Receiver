package org.endeavourhealth.transform.hl7v2.profiles.homerton.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Zqa extends Datatype {
    public Zqa(GenericDatatype datatype) {
        super(datatype);
    }

    public String getValueType() { return this.getComponentAsString(1); }
    public String getQuestionIdentifier() { return this.getComponentAsString(2); }
    public String getQuestionLabel() { return this.getComponentAsString(3); }
    public String getQuestionValue() { return this.getComponentAsString(4); }
}
