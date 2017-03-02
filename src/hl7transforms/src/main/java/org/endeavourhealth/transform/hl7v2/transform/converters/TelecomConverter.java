package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xtn;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class TelecomConverter {

    public static List<ContactPoint> convert(List<Xtn> contact) throws TransformException {
        List<ContactPoint> result = new ArrayList<>();

        for (Xtn xtn : contact)
            if (xtn != null)
                result.add(TelecomConverter.convert(xtn));

        return result;
    }

    public static ContactPoint convert(Xtn source) throws TransformException {
        ContactPoint target = new ContactPoint();

        if (StringUtils.isNotBlank(source.getEquipmentType()))
            target.setSystem(convertSystemType(source.getEquipmentType()));

        if (StringUtils.isNotBlank(source.getTelephoneNumber()))
            target.setValue(source.getTelephoneNumber());

        if (StringUtils.isNotBlank(source.getUseCode()))
            target.setUse(convertUseCode(source.getUseCode()));

        return target;
    }

    private static ContactPoint.ContactPointSystem convertSystemType(String systemType) throws TransformException {
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

    private static ContactPoint.ContactPointUse convertUseCode(String useCode) throws TransformException {
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
