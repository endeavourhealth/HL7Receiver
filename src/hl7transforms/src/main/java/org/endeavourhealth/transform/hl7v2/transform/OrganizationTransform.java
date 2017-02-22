package org.endeavourhealth.transform.hl7v2.transform;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;

import java.util.ArrayList;
import java.util.List;

public class OrganizationTransform {
    public static List<OrganizationTransform> fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        return new ArrayList<>();
    }
}
