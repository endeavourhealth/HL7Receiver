package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.hl7v2.parser.Helpers;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.CxInterface;
import org.hl7.fhir.instance.model.Identifier;

public class IdentifierConverter {
    private static final String IDENTIFIER_SYSTEM_HL7V2_ASSIGNING_AUTHORITY = "http://endeavourhealth.org/fhir/v2-id-assigning-auth/{0}/{1}";

    public static Identifier convert(CxInterface source, String sendingFacility) {
        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getId()))
            return null;

        return new Identifier()
                .setValue(StringUtils.deleteWhitespace(source.getId()))
                .setSystem(getIdentifierSystem(source, sendingFacility));
    }

    private static String getIdentifierSystem(CxInterface source, String sendingFacility) {
        String identifierTypeCode = source.getIdentifierTypeCode();

        if (StringUtils.isEmpty(identifierTypeCode))
            identifierTypeCode = "UNKNOWN";

        if (identifierTypeCode.equals("NHS"))
           return FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER;

        return Helpers.formatString(IDENTIFIER_SYSTEM_HL7V2_ASSIGNING_AUTHORITY, sendingFacility, identifierTypeCode);
    }
}
