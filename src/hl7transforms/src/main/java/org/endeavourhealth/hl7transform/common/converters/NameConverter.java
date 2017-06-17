package org.endeavourhealth.hl7transform.common.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.HumanName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NameConverter {
    public static List<HumanName> convert(List<? extends XpnInterface> name, Mapper mapper) throws TransformException, MapperException {
        List<HumanName> result = new ArrayList<>();

        for (XpnInterface xpn : name)
            if (xpn != null)
                result.add(NameConverter.convert(xpn, mapper));

        return removeSuperfluousNameDuplicates(result);
    }

    public static HumanName createOfficialName(String surname, String forenames, String title) {
        return createOfficialName(surname, forenames, null, title);
    }

    public static HumanName createOfficialName(String surname, String forenames, String middleNames, String title) {
        HumanName humanName = new HumanName();

        if (StringUtils.isNotBlank(surname))
            humanName.addFamily(formatSurname(surname));

        if (StringUtils.isNotBlank(forenames))
            humanName.addGiven(formatName(forenames));

        if (StringUtils.isNotBlank(middleNames))
            humanName.addGiven(formatName(middleNames));

        if (StringUtils.isNotBlank(title))
            humanName.addPrefix(formatTitle(title));

        humanName.setUse(HumanName.NameUse.OFFICIAL);

        return humanName;
    }

    public static HumanName convert(XpnInterface source, Mapper mapper) throws TransformException, MapperException {
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

        HumanName.NameUse nameUse = mapper
                .getCodeMapper()
                .mapNameType(source.getNameTypeCode());

        if (nameUse != null)
            humanName.setUse(nameUse);

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
        if (names.size() == 0)
            return names;

        names = removeDuplicateNames(names);

        List<HumanName> officialNames = getNamesByUse(names, HumanName.NameUse.OFFICIAL);

        if (officialNames.size() == 0)
            throw new TransformException("Person does not have a official name");

        // convert 2nd and subsequent official names to usual name - these will have come from the patient alias field
        if (officialNames.size() > 1)
            for (int i = 1; i < officialNames.size(); i++)
                officialNames.get(i).setUse(HumanName.NameUse.USUAL);

        HumanName officialName = officialNames.get(0);

        return removeNamesMatchingOfficialNameExcludingNameUseField(names, officialName);
    }

    private static List<HumanName> removeNamesMatchingOfficialNameExcludingNameUseField(List<HumanName> names, HumanName officialName) {
        List<HumanName> result = new ArrayList<>();

        for (HumanName name : names) {
            if (name.equals(officialName)) {
                result.add(name);
            } else {
                boolean nameIsEqualToOfficialName = false;
                HumanName.NameUse nameUse = name.getUse();
                try {
                    name.setUse(HumanName.NameUse.OFFICIAL);   // temporarily set to official

                    nameIsEqualToOfficialName = name.equalsDeep(officialName);
                } finally {
                    name.setUse(nameUse);                   // set back to previous value
                }

                if (!nameIsEqualToOfficialName)
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

    public static String getFirstGivenName(HumanName name) {
        if (name == null)
            return null;

        if (name.getGiven() == null)
            return null;

        if (name.getGiven().size() == 0)
            return null;

        return name.getGiven().get(0).getValue();
    }

    public static String getFirstSurname(HumanName name) {
        if (name == null)
            return null;

        if (name.getFamily() == null)
            return null;

        if (name.getFamily().size() == 0)
            return null;

        return name.getFamily().get(0).getValue();
    }
}
