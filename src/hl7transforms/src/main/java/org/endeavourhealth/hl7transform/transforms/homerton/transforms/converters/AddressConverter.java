package org.endeavourhealth.hl7transform.transforms.homerton.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.datatypes.Xad;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressConverter {

    public static List<Address> convert(List<Xad> addresses, Mapper mapper) throws TransformException, MapperException {
        List<Address> result = new ArrayList<>();

        for (Xad xad : addresses)
            if (xad != null)
                result.add(AddressConverter.convert(xad, mapper));

        return result;
    }

    public static Address createWorkAddress(String addressLine1, String addressLine2, String city, String postcode) {
        if (StringUtils.isBlank(addressLine1)
                && (StringUtils.isBlank(addressLine2))
                && StringUtils.isBlank(city)
                && StringUtils.isBlank(postcode))
            return null;

        Address address = new Address();

        if (StringUtils.isNotBlank(addressLine1))
            address.addLine(formatAddressLine(addressLine1));

        if (StringUtils.isNotBlank(addressLine2))
            address.addLine(formatAddressLine(addressLine2));

        if (StringUtils.isNotBlank(city))
            address.setCity(formatAddressLine(city));

        if (StringUtils.isNotBlank(postcode))
            address.setPostalCode(formatPostcode(postcode));

        address.setUse(Address.AddressUse.WORK);

        return address;
    }

    public static Address convert(Xad source, Mapper mapper) throws TransformException, MapperException {

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

        Address.AddressUse addressUse = mapper.getCodeMapper().mapAddressType(source.getAddressType());

        if (addressUse != null)
            target.setUse(addressUse);

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

    public static String getPostcode(List<Address> addresses) {
        if (addresses == null)
            return null;

        if (addresses.size() == 0)
            return null;

        Address address = addresses.get(0);

        return addresses.get(0).getPostalCode();
    }
}
