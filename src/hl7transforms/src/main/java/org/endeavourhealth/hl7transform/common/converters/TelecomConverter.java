package org.endeavourhealth.hl7transform.common.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.Xtn;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.*;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class TelecomConverter {

    public static List<ContactPoint> convert(List<Xtn> contact, Mapper mapper) throws TransformException, MapperException {
        List<ContactPoint> result = new ArrayList<>();

        for (Xtn xtn : contact) {
            ContactPoint contactPoint = convert(xtn, mapper);

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

    public static ContactPoint convert(Xtn source, Mapper mapper) throws TransformException, MapperException {

        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getTelephoneNumber()))
            return null;

        ContactPoint target = new ContactPoint();

        ContactPoint.ContactPointSystem contactPointSystem = mapper.getCodeMapper().mapTelecomEquipmentType(source.getEquipmentType());

        if (contactPointSystem != null)
            target.setSystem(contactPointSystem);

        ContactPoint.ContactPointUse contactPointUse = mapper.getCodeMapper().mapTelecomUse(source.getUseCode());

        if (contactPointUse != null)
            target.setUse(contactPointUse);

        String phoneNumber = source.getTelephoneNumber().trim();

        if ((target.getSystem() == ContactPoint.ContactPointSystem.PHONE) || (target.getSystem() == ContactPoint.ContactPointSystem.FAX))
            phoneNumber = formatTelephoneNumber(source.getTelephoneNumber());

        target.setValue(phoneNumber);

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
}
