package org.endeavourhealth.hl7transform.common.converters;

import org.hl7.fhir.instance.model.*;

public class ExtensionHelper {

    public static Extension createStringExtension(String uri, String value) {
        return new Extension()
                .setUrl(uri)
                .setValue(new StringType(value));
    }

    public static Extension createIntegerExtension(String uri, Integer value) {
        return new Extension()
                .setUrl(uri)
                .setValue(new IntegerType(value));
    }

    public static Extension createDateTimeTypeExtension(String uri, Type value) {
        return new Extension()
                .setUrl(uri)
                .setValue(value);
    }
}
