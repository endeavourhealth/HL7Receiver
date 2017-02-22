package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xad;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
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

        if (StringUtils.isNotBlank(source.getPostCode()))
            target.setPostalCode(formatPostcode(source.getPostCode()));

        if (StringUtils.isNotBlank(source.getAddressType()))
            target.setUse(convertAddressType(source.getAddressType()));

        return target;
    }

    private static String formatAddressLine(String line) {
        return StringHelper.formatName(line);
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

    private static Address.AddressUse convertAddressType(String addressType) throws TransformException {
        addressType = addressType.trim().toLowerCase();

        switch (addressType) {
            case "home": return Address.AddressUse.HOME;
            case "temporary": return Address.AddressUse.TEMP;
            default: throw new TransformException(addressType + " address type not recognised");
        }
    }
}
