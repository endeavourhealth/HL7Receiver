package org.endeavourhealth.hl7transform.homerton.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.Xad;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.AddressUseVs;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressConverter {

    public static List<Address> convert(List<Xad> addresses) throws TransformException {
        List<Address> result = new ArrayList<>();

        for (Xad xad : addresses)
            if (xad != null)
                result.add(AddressConverter.convert(xad));

        return result;
    }

    public static Address createWorkAddress(List<String> addressLines, String city, String postcode) {
        if (addressLines.stream().allMatch(t -> StringUtils.isBlank(t))
                && StringUtils.isBlank(city)
                && StringUtils.isBlank(postcode))
            return null;

        Address address = new Address();

        for (String line : addressLines)
            if (!StringUtils.isEmpty(line))
                address.addLine(formatAddressLine(line));

        if (StringUtils.isNotBlank(city))
            address.setCity(formatAddressLine(city));

        if (StringUtils.isNotBlank(postcode))
            address.setPostalCode(formatPostcode(postcode));

        address.setUse(Address.AddressUse.WORK);

        return address;
    }

    public static Address convert(Xad source) throws TransformException {

        Address target = new Address();

        if (StringUtils.isNotBlank(source.getStreetAddress()))
            target.addLine(formatAddressLine(source.getStreetAddress()));

        if (StringUtils.isNotBlank(source.getOtherDesignation()))
            target.addLine(formatAddressLine(source.getOtherDesignation()));

        if (StringUtils.isNotBlank(source.getCity()))
            target.setCity(formatAddressLine(source.getCity()));

        if (StringUtils.isNotBlank(source.getProvince()))
            target.setDistrict(formatAddressLine(source.getProvince()));

        if (StringUtils.isNotBlank(source.getOtherGeographicDesignation())) {

            String otherGeographicDesignation = formatAddressLine(source.getOtherGeographicDesignation());

            if (StringUtils.isBlank(target.getDistrict()))
                target.setDistrict(otherGeographicDesignation);
            else
                target.setState(otherGeographicDesignation);
        }

        if (StringUtils.isNotBlank(source.getPostCode()))
            target.setPostalCode(formatPostcode(source.getPostCode()));

        if (StringUtils.isNotBlank(source.getAddressType()))
            target.setUse(AddressUseVs.convert(source.getAddressType()));

        return target;
    }

    private static String formatAddressLine(String line) {
        return insertSpaceAfterComma(StringHelper.formatName(line));
    }

    private static String insertSpaceAfterComma(String text) {
        boolean previousWasComma = false;
        String result = "";

        for (int i = 0; i < text.length(); i++) {

            char character = text.charAt(i);

            if (previousWasComma)
                if (character != ' ')
                    result += ' ';

            result += character;

            previousWasComma = (character == ',');
        }

        return result;
    }

    private static String formatPostcode(String postcode) {
        String formattedPostcode = StringUtils.deleteWhitespace(postcode.toUpperCase());

        if ((formattedPostcode.length() >= 5) && (formattedPostcode.length() <= 7)) {
            formattedPostcode =
                    StringUtils.substring(formattedPostcode, 0, formattedPostcode.length() - 3)
                            + " "
                            + StringUtils.substring(formattedPostcode, formattedPostcode.length() - 3);
        }

        return formattedPostcode;
    }
}
