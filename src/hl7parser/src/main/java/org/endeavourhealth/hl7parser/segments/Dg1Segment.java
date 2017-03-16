package org.endeavourhealth.hl7parser.segments;

import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Ce;
import org.endeavourhealth.hl7parser.datatypes.Xcn;

import java.util.List;

public class Dg1Segment extends Segment {
    public Dg1Segment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public String getDiagnosisCodingMethod() { return this.getFieldAsString(2); }
    public Ce getDiagnosisCode() { return this.getFieldAsDatatype(3, Ce.class); }
    public String getDiagnosisDescription() { return this.getFieldAsString(4); }
    public Hl7DateTime getDiagnosisDateTime() throws ParseException { return this.getFieldAsHl7Date(5); }
    public String getDiagnosisType() { return this.getFieldAsString(6); }
    public Ce getMajorDiagnosticCategory() { return this.getFieldAsDatatype(7, Ce.class); }
    public Ce getDiagnosticRelatedGroup() { return this.getFieldAsDatatype(8, Ce.class); }
    public String getDRGApprovalIndicator() { return this.getFieldAsString(9); }
    public String getDRGGrouperReviewCode() { return this.getFieldAsString(10); }
    public Ce getOutlierType() { return this.getFieldAsDatatype(11, Ce.class); }
    public int getOutlierDays() throws ParseException { return this.getFieldAsInteger(12); }
    public String getOutlierCost() { return this.getFieldAsString(13); }
    public String getGrouperVersionAndType() { return this.getFieldAsString(14); }
    public String getDiagnosisPriority() { return this.getFieldAsString(15); }
    public List<Xcn> getDiagnosingClinician() { return this.getFieldAsDatatypes(16, Xcn.class); }
    public String getDiagnosisClassification() { return this.getFieldAsString(17); }
    public String getConfidentialIndicator() { return this.getFieldAsString(18); }
    public Hl7DateTime getAttestationDateTime() throws ParseException { return this.getFieldAsHl7Date(19); }
    public String getDiagnosisIdentifier() { return this.getFieldAsString(20); }
    public String getDiagnosisActionCode() { return this.getFieldAsString(21); }
}
