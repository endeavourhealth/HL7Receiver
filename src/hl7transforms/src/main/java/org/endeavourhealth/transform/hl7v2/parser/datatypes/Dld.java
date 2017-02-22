package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;

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