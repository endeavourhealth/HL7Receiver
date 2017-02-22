package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.IntegerType;
import org.hl7.fhir.instance.model.StringType;
import java.util.Date;

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

    public static Extension createDateTimeExtension(String uri, Date value) {
        return new Extension()
                .setUrl(uri)
                .setValue(new DateTimeType(value));
    }
}
