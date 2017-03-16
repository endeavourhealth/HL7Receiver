package org.endeavourhealth.hl7transform.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.Xtn;
import org.endeavourhealth.hl7transform.transform.TransformException;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class TelecomConverter {

    public static List<ContactPoint> convert(List<Xtn> contact) throws TransformException {
        List<ContactPoint> result = new ArrayList<>();

        for (Xtn xtn : contact) {
            ContactPoint contactPoint = convert(xtn);

            if (contactPoint != null)
                result.add(contactPoint);
        }

        return result;
    }

    public static ContactPoint createWorkPhone(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber))
            return null;

        return new ContactPoint()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setValue(TelecomConverter.formatTelephoneNumber(phoneNumber));
    }

    public static ContactPoint convert(Xtn source) throws TransformException {

        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getTelephoneNumber()))
            return null;

        ContactPoint target = new ContactPoint();

        if (StringUtils.isNotBlank(source.getEquipmentType()))
            target.setSystem(convertSystemType(source.getEquipmentType()));

        String phoneNumber = source.getTelephoneNumber().trim();

        if ((target.getSystem() == ContactPoint.ContactPointSystem.PHONE) || (target.getSystem() == ContactPoint.ContactPointSystem.FAX))
               phoneNumber = formatTelephoneNumber(source.getTelephoneNumber());

        target.setValue(phoneNumber);

        if (StringUtils.isNotBlank(source.getUseCode()))
            target.setUse(convertUseCode(source.getUseCode()));

        return target;
    }

    public static String formatTelephoneNumber(String phoneNumber) {
        if (phoneNumber == null)
            return null;

        if (!StringUtils.containsOnly("+()01234567890"))
            return phoneNumber.trim();

        return StringUtils
                    .deleteWhitespace(phoneNumber)
                    .replace("(", "")
                    .replace(")", "");
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
