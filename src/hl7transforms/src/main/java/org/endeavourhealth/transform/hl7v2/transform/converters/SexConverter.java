package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.Enumerations;

public class SexConverter {
    public static Enumerations.AdministrativeGender convert(String sex) throws TransformException {
        if (StringUtils.isEmpty(sex))
            return null;

        switch (sex.trim().toLowerCase().substring(0, 1))
        {
            case "m": return Enumerations.AdministrativeGender.MALE;
            case "f": return Enumerations.AdministrativeGender.FEMALE;
            case "o": return Enumerations.AdministrativeGender.OTHER;
            case "u": return Enumerations.AdministrativeGender.UNKNOWN;
            default: throw new TransformException(sex + " not recognised as a sex value");
        }
    }
}
