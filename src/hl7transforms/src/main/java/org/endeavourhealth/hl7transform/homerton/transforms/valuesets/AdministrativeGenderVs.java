package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.Enumerations;

public class AdministrativeGenderVs {

    public static Enumerations.AdministrativeGender convert(String sex) throws TransformException {
        if (StringUtils.isEmpty(sex))
            return null;

        switch (sex.trim().toLowerCase().substring(0, 1))
        {
            case "m": return Enumerations.AdministrativeGender.MALE;
            case "f": return Enumerations.AdministrativeGender.FEMALE;
            case "o": return Enumerations.AdministrativeGender.OTHER;
            case "u": return Enumerations.AdministrativeGender.UNKNOWN;

            //Homerton Specific
            case "n": return Enumerations.AdministrativeGender.UNKNOWN;
            case "i": return Enumerations.AdministrativeGender.OTHER;

            default: throw new TransformException(sex + " sex not recognised");
        }
    }
}
