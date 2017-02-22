package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

// extended person name
public class Xpn extends Datatype implements XpnInterface {
    public Xpn(GenericDatatype datatype) {
        super(datatype);
    }

    public String getFamilyName() { return this.getComponentAsString(1); }
    public String getGivenName() { return this.getComponentAsString(2); }
    public String getMiddleName() { return this.getComponentAsString(3); }
    public String getSuffix() { return this.getComponentAsString(4); }
    public String getPrefix() { return this.getComponentAsString(5); }
    public String getDegree() { return this.getComponentAsString(6); }
    public String getNameTypeCode() { return this.getComponentAsString(7); }
    public String getNameRepresentationCode() { return this.getComponentAsString(8); }
}
