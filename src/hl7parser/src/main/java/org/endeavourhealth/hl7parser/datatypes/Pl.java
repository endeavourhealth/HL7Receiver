package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Pl extends Datatype {

    public Pl(GenericDatatype datatype) {
        super(datatype);
    }

    public String getPointOfCare() { return this.getComponentAsString(1); }
    public String getRoom() { return this.getComponentAsString(2); }
    public String getBed() { return this.getComponentAsString(3); }
    public String getFacility() { return this.getComponentAsString(4); }
    public String getLocationStatus() { return this.getComponentAsString(5); }
    public String getPersonLocationType() { return this.getComponentAsString(6); }
    public String getBuilding() { return this.getComponentAsString(7); }
    public String getFloor() { return this.getComponentAsString(8); }
    public String getLocationDescription() { return this.getComponentAsString(9); }
}
