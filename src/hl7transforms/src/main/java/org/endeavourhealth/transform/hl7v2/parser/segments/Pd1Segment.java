package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Ce;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Cx;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;

import java.util.List;

public class Pd1Segment extends Segment {
    public Pd1Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public String getLivingDependency() { return this.getFieldAsString(1); }
    public String getLivingArrangement() { return this.getFieldAsString(2); }
    public String getPatientPrimaryCareFacility() { return this.getFieldAsString(3); }
    public List<Xcn> getPatientPrimaryCareProvider() { return this.getFieldAsDatatypes(4, Xcn.class); }
    public String getStudentIndicator() { return this.getFieldAsString(5); }
    public String getHandicap() { return this.getFieldAsString(6); }
    public String getLivingWill() { return this.getFieldAsString(7); }
    public String getOrganDonor() { return this.getFieldAsString(8); }
    public String getSeperateBill() { return this.getFieldAsString(9); }
    public List<Cx> getDuplicatePatient() { return this.getFieldAsDatatypes(10, Cx.class); }
    public Ce getPublicityCode() { return this.getFieldAsDatatype(11, Ce.class); }
    public String getProtectionIndicator() { return this.getFieldAsString(12); }
}
