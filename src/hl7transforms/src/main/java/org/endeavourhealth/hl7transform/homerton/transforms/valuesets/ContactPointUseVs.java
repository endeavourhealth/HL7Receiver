package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.ContactPoint;

public abstract class ContactPointUseVs {

    public static ContactPoint.ContactPointUse convert(String useCode) throws TransformException {
        useCode = useCode.trim().toUpperCase();

        switch (useCode) {
            case "H": return ContactPoint.ContactPointUse.HOME;
            case "PRN": return ContactPoint.ContactPointUse.HOME;
            case "ORN": return ContactPoint.ContactPointUse.HOME;
            case "VHN": return ContactPoint.ContactPointUse.HOME;
            case "WP": return ContactPoint.ContactPointUse.WORK;
            case "WPN": return ContactPoint.ContactPointUse.WORK;
            case "TMP": return ContactPoint.ContactPointUse.TEMP;
            case "OLD": return ContactPoint.ContactPointUse.OLD;
            case "MC": return ContactPoint.ContactPointUse.MOBILE;
            case "PRS": return ContactPoint.ContactPointUse.MOBILE;

            //Homerton Specific
            case "HOME": return ContactPoint.ContactPointUse.HOME;
            case "MOBILE NUMBER": return ContactPoint.ContactPointUse.MOBILE;
            case "PAGER PERSONAL": return ContactPoint.ContactPointUse.WORK;
            case "BUSINESS": return ContactPoint.ContactPointUse.WORK;
            case "TEMPORARY": return ContactPoint.ContactPointUse.TEMP;
            case "HOME PHONE": return ContactPoint.ContactPointUse.HOME;
            case "TEMP PHONE": return ContactPoint.ContactPointUse.TEMP;
            case "EMERGENCY NUMBER": return ContactPoint.ContactPointUse.TEMP;
            case "PAGER NUMBER": return ContactPoint.ContactPointUse.WORK;

            default: throw new TransformException(useCode + " use code not recognised");
        }
    }
}
