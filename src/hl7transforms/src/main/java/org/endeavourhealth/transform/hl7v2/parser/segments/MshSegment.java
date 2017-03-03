package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.*;
import org.endeavourhealth.transform.hl7v2.transform.Hl7DateTime;

public class MshSegment extends Segment {
    public MshSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public String getFieldSeparator() {
        return this.getFieldAsString(1);
    }
    public String getEncodingCharacters() {
        return this.getFieldAsString(2);
    }
    public String getSendingApplication() { return this.getComponentAsString(3, 1); }
    public String getSendingFacility() { return this.getComponentAsString(4, 1); }
    public String getReceivingApplication() { return this.getComponentAsString(5, 1); };
    public String getReceivingFacility() { return this.getComponentAsString(6, 1); };
    public Hl7DateTime getDateTimeOfMessage() { try { return this.getFieldAsHl7Date(7); } catch (ParseException e) { throw new RuntimeParseException(e); } }
    public String getSecurity() { return this.getFieldAsString(8); }
    public String getMessageType() { return this.getFieldAsString(9); }
    public Field getMessageTypeField() { return this.getField(9); }
    public String getMessageControlId() { return this.getFieldAsString(10); }
    public String getProcessingId() { return this.getFieldAsString(11); }
    public String getVersionId() { return this.getFieldAsString(12); }
    public Integer getSequenceNumber() throws ParseException { return this.getFieldAsInteger(13); }
    public String getContinuationPointer() { return this.getFieldAsString(14); }
    public String getAcceptAcknowledgmentType() { return this.getFieldAsString(15); }
    public String getApplicationAcknowledgmentType() { return this.getFieldAsString(16); }
    public String getCountryCode() { return this.getFieldAsString(17); }
    public String getCharacterSet() { return this.getFieldAsString(18); }
    public String getPrincipalLanguageOfMessage() { return this.getFieldAsString(19); }
}
