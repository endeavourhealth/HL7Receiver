package org.endeavourhealth.hl7transform.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7transform.transform.TransformException;
import org.hl7.fhir.instance.model.HumanName;

import java.util.ArrayList;
import java.util.List;

public class NameConverter {
    public static List<HumanName> convert(List<? extends XpnInterface> name) throws TransformException {
        List<HumanName> result = new ArrayList<>();

        for (XpnInterface xpn : name)
            if (xpn != null)
                result.add(NameConverter.convert(xpn));

        return result;
    }

    public static HumanName convert(XpnInterface source) throws TransformException {
        HumanName humanName = new HumanName();

        if (StringUtils.isNotBlank(source.getFamilyName()))
            humanName.addFamily(formatSurname(source.getFamilyName()));

        if (StringUtils.isNotBlank(source.getGivenName()))
            humanName.addGiven(formatName(source.getGivenName()));

        if (StringUtils.isNotBlank(source.getMiddleName()))
            humanName.addGiven(formatName(source.getMiddleName()));

        if (StringUtils.isNotBlank(source.getPrefix()))
            humanName.addPrefix(formatTitle(source.getPrefix()));

        if (StringUtils.isNotBlank(source.getSuffix()))
            humanName.addSuffix(formatTitle(source.getSuffix()));

        if (StringUtils.isNotBlank(source.getNameTypeCode()))
            humanName.setUse(convertNameTypeCode(source.getNameTypeCode()));

        return humanName;
    }

    private static HumanName.NameUse convertNameTypeCode(String nameTypeCode) throws TransformException {
        if (nameTypeCode == null)
            nameTypeCode = "";

        nameTypeCode = nameTypeCode.trim().toLowerCase();

        switch (nameTypeCode) {

            // HL7v2 table 0200
            case "a":                                     // alias
                return HumanName.NameUse.NICKNAME;
            case "l":                                     // legal
                return HumanName.NameUse.OFFICIAL;
            case "d":                                     // display
                return HumanName.NameUse.USUAL;
            case "m":                                     // maiden
                return HumanName.NameUse.MAIDEN;
            case "c":                                     // adopted
                return HumanName.NameUse.OLD;
            case "o":                                     // other
                return HumanName.NameUse.TEMP;

            // Cerner Millenium
            case "adopted":
                return HumanName.NameUse.OLD;
            case "alternate":
                return HumanName.NameUse.NICKNAME;
            case "current":
                return HumanName.NameUse.USUAL;
            case "legal":
                return HumanName.NameUse.OFFICIAL;
            case "maiden":
                return HumanName.NameUse.MAIDEN;
            case "other":
                return HumanName.NameUse.TEMP;
            case "previous":
                return HumanName.NameUse.OLD;
            case "preferred":
                return HumanName.NameUse.USUAL;
            case "personnel":                               // used in Xcn data type
                return HumanName.NameUse.USUAL;

            default:
                throw new TransformException(nameTypeCode + " name type code not recognised");
        }
    }

    public static String formatTitle(String title) {
        if (title == null)
            return null;

        String result = StringHelper.formatName(title);

        result = result.replace(".", "");

        return result;
    }

    private static String formatName(String name) {
        return StringHelper.formatName(name);
    }


    public static String formatSurname(String surname) {
        if (surname == null)
            return null;

        String result = StringHelper.formatName(surname);

        result = upperCaseAfterFragment(result, "Mc");
        result = upperCaseAfterFragment(result, "Mac");

        return result;
    }

    private static String upperCaseAfterFragment(String str, String fragment) {
        if (str == null)
            return null;

        if (fragment == null)
            return str;

        StringBuilder stringBuilder = new StringBuilder(str);

        int startIndex = 0;

        while (startIndex < str.length()) {
            int mcIndex = stringBuilder.indexOf(fragment, startIndex);

            if (mcIndex == -1)
                break;

            if ((mcIndex + fragment.length()) < (str.length()))
                stringBuilder.setCharAt(mcIndex + fragment.length(), Character.toUpperCase(stringBuilder.charAt(mcIndex + fragment.length())));

            startIndex = mcIndex + 1;
        }

        return stringBuilder.toString();
    }

    public static String getNameAsString(XpnInterface source) {
        String name = "";

        if (StringUtils.isNotBlank(source.getPrefix()))
            name += formatTitle(source.getPrefix()) + " ";

        if (StringUtils.isNotBlank(source.getFamilyName()))
            name += formatSurname(source.getFamilyName()) + ", ";

        if (StringUtils.isNotBlank(source.getGivenName()))
            name += formatName(source.getGivenName());

        return name;
    }
}
