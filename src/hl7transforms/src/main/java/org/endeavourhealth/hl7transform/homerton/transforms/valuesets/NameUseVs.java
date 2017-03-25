package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.HumanName;

public abstract class NameUseVs {

    public static HumanName.NameUse convert(String nameTypeCode) throws TransformException {
        if (nameTypeCode == null)
            nameTypeCode = "";

        nameTypeCode = nameTypeCode.trim().toLowerCase();

        switch (nameTypeCode) {

            // HL7v2 table 0200
            case "a": return HumanName.NameUse.NICKNAME;  // alias
            case "l": return HumanName.NameUse.OFFICIAL;  // legal
            case "d": return HumanName.NameUse.USUAL;     // display
            case "m": return HumanName.NameUse.MAIDEN;    // maiden
            case "c": return HumanName.NameUse.OLD;       // adopted
            case "o": return HumanName.NameUse.TEMP;      // other

            // Cerner Millenium
            case "alternate": return HumanName.NameUse.NICKNAME;
            case "legal": return HumanName.NameUse.OFFICIAL;

            case "current":
            case "preferred":
            case "personnel": return HumanName.NameUse.USUAL;

            case "maiden": return HumanName.NameUse.MAIDEN;

            case "adopted":
            case "previous": return HumanName.NameUse.OLD;

            case "other": return HumanName.NameUse.TEMP;

            default:
                throw new TransformException(nameTypeCode + " name type code not recognised");
        }
    }
}
