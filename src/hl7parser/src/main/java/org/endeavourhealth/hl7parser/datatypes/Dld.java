package org.endeavourhealth.hl7parser.datatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;
import org.endeavourhealth.hl7parser.ParseException;

import java.time.LocalDateTime;

public class Dld extends Datatype {

    public Dld(GenericDatatype datatype) {
        super(datatype);
    }

    public String getDischargeLocation() {
        return this.getComponentAsString(1);
    }
    public LocalDateTime getEffectiveDate() throws ParseException { return this.getComponentAsDate(2); }
}