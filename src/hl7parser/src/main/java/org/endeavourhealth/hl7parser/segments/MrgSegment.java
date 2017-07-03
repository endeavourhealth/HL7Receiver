package org.endeavourhealth.hl7parser.segments;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.Xpn;

import java.util.List;

public class MrgSegment extends Segment {
    public MrgSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public List<Cx> getPriorPatientIdentifierList() throws ParseException { return this.getFieldAsDatatypes(1, Cx.class); }
    public List<Cx> getPriorAlternatePatientIdList() throws ParseException { return this.getFieldAsDatatypes(2, Cx.class); }
    public Cx getPriorPatientAccountNumber() { return this.getFieldAsDatatype(3, Cx.class); }
    public Cx getPriorPatientId() { return this.getFieldAsDatatype(4, Cx.class); }
    public Cx getPriorVisitNumber() { return this.getFieldAsDatatype(5, Cx.class); }
    public Cx getPriorAlternateVisitId() { return this.getFieldAsDatatype(6, Cx.class); }
    public List<Xpn> getPriorPatientName() { return this.getFieldAsDatatypes(7, Xpn.class); }
}
