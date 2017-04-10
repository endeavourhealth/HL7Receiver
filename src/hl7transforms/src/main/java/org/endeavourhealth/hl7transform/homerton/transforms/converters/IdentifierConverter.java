package org.endeavourhealth.hl7transform.homerton.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.hl7parser.datatypes.CxInterface;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.ResourceType;

public class IdentifierConverter {

    public static Identifier createIdentifier(CxInterface source, ResourceType resourceType, Mapper mapper) throws TransformException, MapperException {
        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getId()))
            return null;

        String identifierSystem = getIdentifierSystem(source, resourceType, mapper);

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

    private static String getIdentifierSystem(CxInterface source, ResourceType resourceType, Mapper mapper) throws TransformException, MapperException {

        String identifierTypeCode = StringUtils.trim(StringUtils.defaultString(source.getIdentifierTypeCode())).toLowerCase();
        String assigningAuthority = StringUtils.trim(StringUtils.defaultString(source.getAssigningAuthority())).toLowerCase();

        if (StringUtils.isEmpty(identifierTypeCode) && StringUtils.isEmpty(assigningAuthority))
            return null;

        if (resourceType == ResourceType.Patient)
            return mapper.getCodeMapper().mapPatientIdentifierTypeAndAssigningAuth(identifierTypeCode, assigningAuthority);
        else if (resourceType == ResourceType.EpisodeOfCare)
            return mapper.getCodeMapper().mapEncounterIdentifierTypeAndAssigningAuth(identifierTypeCode, assigningAuthority);
        else if (resourceType == ResourceType.Practitioner)
            return mapper.getCodeMapper().mapDoctorIdentifierTypeAndAssigningAuth(identifierTypeCode, assigningAuthority);
        else
            throw new TransformException("Resource type " + resourceType.name() + " does not have identifier systems mapped");
    }
}
