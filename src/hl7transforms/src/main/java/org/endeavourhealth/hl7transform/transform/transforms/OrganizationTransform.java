package org.endeavourhealth.hl7transform.transform.transforms;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.transform.TransformException;

import java.util.ArrayList;
import java.util.List;

public class OrganizationTransform {
    public static List<OrganizationTransform> fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        return new ArrayList<>();
    }
}
