package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Xad extends Datatype {
    public Xad(GenericDatatype datatype) {
        super(datatype);
    }

    public String getStreetAddress() { return this.getComponentAsString(1); }
    public String getOtherDesignation() { return this.getComponentAsString(2); }
    public String getCity() { return this.getComponentAsString(3); }
    public String getProvince() { return this.getComponentAsString(4); }
    public String getPostCode() { return this.getComponentAsString(5); }
    public String getCountry() { return this.getComponentAsString(6); }
    public String getAddressType() { return this.getComponentAsString(7); }
    public String getOtherGeographicDesignation() { return this.getComponentAsString(8); }
    public String getCountyCode() { return this.getComponentAsString(9); }
    public String getCensusTract() { return this.getComponentAsString(10); }
    public String getAddressRepresentationCode() { return this.getComponentAsString(11); }
}
