package org.endeavourhealth.hl7parser.segments;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Ce;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.datatypes.Xon;

import java.util.List;

public class Pd1Segment extends Segment {
    public Pd1Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public String getLivingDependency() { return this.getFieldAsString(1); }
    public String getLivingArrangement() { return this.getFieldAsString(2); }
    public List<Xon> getPatientPrimaryCareFacility() { return this.getFieldAsDatatypes(3, Xon.class); }
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
