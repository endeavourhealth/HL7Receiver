package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Ce extends Datatype {
    public Ce(GenericDatatype datatype) {
        super(datatype);
    }

    public String getIdentifier() { return this.getComponentAsString(1); }
    public String getText() { return this.getComponentAsString(2); }
    public String getCodingSystem() { return this.getComponentAsString(3); }
    public String getAlternateIdentifier() { return this.getComponentAsString(4); }
    public String getAlternateText() { return this.getComponentAsString(5); }
    public String getAlternativeCodingSystem() { return this.getComponentAsString(6); }
}
