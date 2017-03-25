package org.endeavourhealth.hl7transform.homerton.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.NameUseVs;
import org.hl7.fhir.instance.model.HumanName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NameConverter {
    public static List<HumanName> convert(List<? extends XpnInterface> name) throws TransformException {
        List<HumanName> result = new ArrayList<>();

        for (XpnInterface xpn : name)
            if (xpn != null)
                result.add(NameConverter.convert(xpn));

        return removeSuperfluousNameDuplicates(result);
    }

    public static HumanName createUsualName(String surname, String forenames, String title) {
        HumanName humanName = new HumanName();

        if (StringUtils.isNotBlank(surname))
            humanName.addFamily(formatSurname(surname));

        if (StringUtils.isNotBlank(forenames))
            humanName.addGiven(formatName(forenames));

        if (StringUtils.isNotBlank(title))
            humanName.addPrefix(formatTitle(title));

        humanName.setUse(HumanName.NameUse.USUAL);

        return humanName;
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
            humanName.setUse(NameUseVs.convert(source.getNameTypeCode()));

        return humanName;
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

    public static String getNameAsCuiString(XpnInterface source) {
        String name = "";

        if (StringUtils.isNotBlank(source.getPrefix()))
            name += formatTitle(source.getPrefix()) + " ";

        if (StringUtils.isNotBlank(source.getFamilyName()))
            name += formatSurname(source.getFamilyName()) + ", ";

        if (StringUtils.isNotBlank(source.getGivenName()))
            name += formatName(source.getGivenName());

        return name;
    }

    private static List<HumanName> removeSuperfluousNameDuplicates(List<HumanName> names) throws TransformException {
        names = removeDuplicateNames(names);

        List<HumanName> usualNames = getNamesByUse(names, HumanName.NameUse.USUAL);

        if (usualNames.size() == 0)
            throw new TransformException("Patient does not have a usual name");

        if (usualNames.size() > 1)
            throw new TransformException("Patient has more than one usual name");

        HumanName usualName = usualNames.get(0);

        return removeNamesMatchingUsualNameExcludingNameUseField(names, usualName);
    }

    private static List<HumanName> removeNamesMatchingUsualNameExcludingNameUseField(List<HumanName> names, HumanName usualName) {
        List<HumanName> result = new ArrayList<>();

        for (HumanName name : names) {
            if (name.equals(usualName)) {
                result.add(name);
            } else {
                boolean nameIsEqualToUsualName = false;
                HumanName.NameUse nameUse = name.getUse();
                try {
                    name.setUse(HumanName.NameUse.USUAL);   // temporarily set to usual

                    nameIsEqualToUsualName = name.equalsDeep(usualName);
                } finally {
                    name.setUse(nameUse);                   // set back to previous value
                }

                if (!nameIsEqualToUsualName)
                    result.add(name);
            }
        }

        return result;
    }

    private static List<HumanName> removeDuplicateNames(List<HumanName> names) {
        List<HumanName> result = new ArrayList<>();

        for (HumanName name : names)
            if (!result.stream().anyMatch(t -> name.equalsDeep(t)))
                result.add(name);

        return result;
    }

    private static List<HumanName> getNamesByUse(List<HumanName> names, HumanName.NameUse nameUse) {
        return names
                .stream()
                .filter(t -> nameUse == t.getUse())
                .collect(Collectors.toList());
    }
}
