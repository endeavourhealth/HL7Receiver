package org.endeavourhealth.hl7transform.common.transform;

import org.hl7.fhir.instance.model.Parameters;
import org.hl7.fhir.instance.model.StringType;

public class MergeCommon {
    public static Parameters.ParametersParameterComponent createStringParameter(String name, String value) {
        return new Parameters.ParametersParameterComponent()
                .setName(name)
                .setValue(new StringType(value));
    }
}
