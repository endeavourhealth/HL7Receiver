package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.ContactPoint;

public abstract class ContactPointSystemVs {

    public static ContactPoint.ContactPointSystem convert(String systemType) throws TransformException {
        systemType = systemType.trim().toUpperCase();

        switch (systemType) {
            case "PH": return ContactPoint.ContactPointSystem.PHONE;
            case "FX": return ContactPoint.ContactPointSystem.FAX;
            case "Internet": return ContactPoint.ContactPointSystem.EMAIL;
            case "BP": return ContactPoint.ContactPointSystem.PAGER;

            //Homerton Specific
            case "TEL": return ContactPoint.ContactPointSystem.PHONE;
            case "EMAIL": return ContactPoint.ContactPointSystem.EMAIL;
            case "TEXT PHONE": return ContactPoint.ContactPointSystem.PHONE;
            case "FAX": return ContactPoint.ContactPointSystem.FAX;

            default: throw new TransformException(systemType + " system type not recognised");
        }
    }
}
