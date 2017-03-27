package org.endeavourhealth.hl7transform.homerton.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.hl7parser.datatypes.CxInterface;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.ResourceType;

public class IdentifierConverter {

    public static Identifier createIdentifier(CxInterface source, ResourceType resourceType) throws TransformException {
        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getId()))
            return null;

        String identifierSystem = getIdentifierSystem(source, resourceType);

        if (StringUtils.isBlank(identifierSystem))
            return null;

        return new Identifier()
                .setSystem(identifierSystem)
                .setValue(StringUtils.deleteWhitespace(source.getId()));
    }

    public static Identifier createOdsCodeIdentifier(String odsCode) {
        if (StringUtils.isBlank(odsCode))
            return null;

        return new Identifier()
                .setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)
                .setValue(StringUtils.deleteWhitespace(odsCode.toUpperCase()));
    }

    public static boolean looksLikeGmcCode(String gmcCode) {
        return StringUtils.trim(StringUtils.defaultString(gmcCode)).toLowerCase().startsWith("g");
    }

    public static boolean looksLikeConsultantCode(String consultantCode) {
        return StringUtils.trim(StringUtils.defaultString(consultantCode)).toLowerCase().startsWith("c");
    }

    private static String getIdentifierSystem(CxInterface source, ResourceType resourceType) throws TransformException {
        String id = StringUtils.trim(StringUtils.defaultString(source.getId())).toLowerCase();
        String identifierTypeCode = StringUtils.trim(StringUtils.defaultString(source.getIdentifierTypeCode())).toLowerCase();
        String assigningAuthority = StringUtils.trim(StringUtils.defaultString(source.getAssigningAuthority())).toLowerCase();

        if (StringUtils.isEmpty(identifierTypeCode) && StringUtils.isEmpty(assigningAuthority))
            return null;

        if (resourceType == ResourceType.Patient) {

            switch (assigningAuthority + " | " + identifierTypeCode) {
                case "nhs number | nhs": return FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER;
                case "homerton case note number | cnn": return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_CNN_PATIENT_ID;
                case "homerton case note number | mrn": return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_MRN_PATIENT_ID;
                case "newham case note number | cnn": return FhirUri.IDENTIFIER_SYSTEM_NEWHAM_CNN_PATIENT_ID;
                case "person id | person id": return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PERSONID_PATIENT_ID;
                default: throw new TransformException("Patient identifier system not found for " + identifierTypeCode + " | " + assigningAuthority);
            }

        } else if (resourceType == ResourceType.EpisodeOfCare) {

            switch (assigningAuthority + " | " + identifierTypeCode) {
                case "homerton fin | encounter no.": return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_FIN_EPISODE_ID;
                case " | attendance no.": return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_ATTENDANCE_NO_EPISODE_ID;
                default: throw new TransformException("Episode identifier system not found for " + identifierTypeCode + " | " + assigningAuthority);
            }

        } else if (resourceType == ResourceType.Practitioner) {

            switch (assigningAuthority + " | " + identifierTypeCode) {
                case "nhs consultant number | non gp":
                case "community dr nbr | community dr nbr":
                case " | community dr nbr":
                    if (looksLikeConsultantCode(id))
                        return FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE;
                    else
                        return null;

                case "external id | external identifier":
                    if (looksLikeGmcCode(id))
                        return FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER;
                    else
                        return null;

                case "personnel primary identifier | personnel primary identifier":
                    return FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID;

                default: return null;
            }
        } else {
            throw new TransformException("Resource type " + resourceType.name() + " does not have identifier systems mapped");
        }
    }
}
