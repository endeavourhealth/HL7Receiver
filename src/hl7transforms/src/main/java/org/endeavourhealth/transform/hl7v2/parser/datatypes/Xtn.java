package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Xtn extends Datatype {
    public Xtn(GenericDatatype datatype) {
        super(datatype);
    }

    public String getTelephoneNumber() { return this.getComponentAsString(1); }
    public String getUseCode() { return this.getComponentAsString(2); }
    public String getEquipmentType() { return this.getComponentAsString(3); }
    public String getEmailAddress() { return this.getComponentAsString(4); }
    public String getCountryCode() { return this.getComponentAsString(5); }
    public String getAreaCode() { return this.getComponentAsString(6); }
    public String getPhoneNumber() { return this.getComponentAsString(7); }
    public String getExtension() { return this.getComponentAsString(8); }
    public String getText() { return this.getComponentAsString(9); }
}
