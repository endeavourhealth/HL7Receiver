package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.Address;

public abstract class AddressUseVs {

    public static Address.AddressUse convert(String addressType) throws TransformException {
        addressType = addressType.trim().toLowerCase();

        switch (addressType) {
            case "home": return Address.AddressUse.HOME;
            case "temporary": return Address.AddressUse.TEMP;
            case "previous": return Address.AddressUse.OLD;

            //Homerton Specific
            case "mailing": return Address.AddressUse.HOME;
            case "alternate": return Address.AddressUse.TEMP;
            case "birth": return Address.AddressUse.OLD;

            default: throw new TransformException(addressType + " address type not recognised");
        }
    }
}
