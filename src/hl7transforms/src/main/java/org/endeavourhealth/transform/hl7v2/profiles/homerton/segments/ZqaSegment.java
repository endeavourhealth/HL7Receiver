package org.endeavourhealth.transform.hl7v2.profiles.homerton.segments;

import org.endeavourhealth.hl7parser.*;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.datatypes.Zqa;

import java.util.List;

public class ZqaSegment extends Segment {
    public ZqaSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    //Repeatable Segment

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public String getQuestionnaireId() { return this.getFieldAsString(2); }
    public List<Zqa> getQuestionAndAnswer() { return this.getFieldAsDatatypes(3, Zqa.class); }
    public String getCombinedAnswer() { return this.getFieldAsString(8); }
}
