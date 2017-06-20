package org.endeavourhealth.hl7transform.transforms.homerton.pretransform;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.*;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7parser.segments.SegmentName;

import java.util.List;

public class HomertonPreTransform {
    public static AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        Validate.notNull(sourceMessage);

        // remove all fields with only "" in them
        removeEmptyDoubleQuotes(sourceMessage);

        // PID1.4  (Alternate patient identifiers) - clear field  as infrequently populated and when does contains historical data and anomolies
        for (Segment pidSegment : sourceMessage.getSegments(SegmentName.PID))
            clearPid1_4(pidSegment);

        // PID19 (populated with NHS number) - move from PID19 to PID3
        for (Segment pidSegment : sourceMessage.getSegments(SegmentName.PID))
            movePid19ToPid3(pidSegment);

        return sourceMessage;
    }

    private static void removeEmptyDoubleQuotes(AdtMessage sourceMessage) {
        List<Component> components = sourceMessage.getAllComponents();

        for (Component component : components)
            if (component.getAsString().equals("\"\""))
                component.setAsString("");
    }

    private static void clearPid1_4(Segment segment) {
        Field field = segment.getField(4);

        if (field != null)
            field.setAsString("");
    }

    private static void movePid19ToPid3(Segment segment) {
        Field field19 = segment.getField(19);
        Field field3 = segment.getField(3);

        if (field19 != null) {
            if (StringUtils.isNotBlank(field19.getAsString())) {
                if (field19.getAsString().trim().length() == 10) {
                    GenericDatatype datatype = field3.addDatatype();
                    datatype.setComponentAsString(1, field19.getAsString());
                    datatype.setComponentAsString(4, "NHS Number");
                    datatype.setComponentAsString(5, "NHS");
                }
            }
        }
    }
}
