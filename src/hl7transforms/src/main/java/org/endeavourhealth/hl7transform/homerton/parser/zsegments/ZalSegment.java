package org.endeavourhealth.hl7transform.homerton.parser.zsegments;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.Seperators;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.Hl7DateTime;

public class ZalSegment extends Segment {
    public ZalSegment(String segmentText, Seperators seperators) throws ParseException {
        super(segmentText, seperators);
    }

    public String getActionCode() { return this.getFieldAsString(1); }
    public Hl7DateTime getActivateDateTime() throws ParseException { return this.getFieldAsHl7Date(2); }
    public String getAllergyInstanceId() { return this.getFieldAsString(3); }
    public String getAllergyId() { return this.getFieldAsString(4); }
    public String getReactionClass() { return this.getFieldAsString(5); }
    public String getReactionStatus() { return this.getFieldAsString(6); }
    public String getReactionCodeIdentifier() { return this.getComponentAsString(7, 1); }
    public String getReactionCodeDescription() { return this.getComponentAsString(7, 2); }
    public String getReactionCodeScheme() { return this.getComponentAsString(7, 3); }
    public String getSourceOfInformation() { return this.getFieldAsString(8); }
    public String getCancelReasonCode() { return this.getFieldAsString(9); }
    public String getCancelReasonPersonnel() { return this.getFieldAsString(10); }
    public Hl7DateTime getReviewedDateTime() throws ParseException { return this.getFieldAsHl7Date(11); }
    public Xcn getReviewedPersonnel() { return this.getFieldAsDatatype(12, Xcn.class); }
    public String getVerifiedStatusFlag() { return this.getFieldAsString(13); }
}
