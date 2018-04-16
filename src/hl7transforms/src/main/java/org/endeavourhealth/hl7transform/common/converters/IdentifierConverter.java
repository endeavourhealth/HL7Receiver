package org.endeavourhealth.hl7transform.common.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirIdentifierUri;
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
                .setSystem(FhirIdentifierUri.IDENTIFIER_SYSTEM_ODS_CODE)
                .setValue(StringUtils.deleteWhitespace(odsCode.toUpperCase()));
    }

    private static String getIdentifierSystem(CxInterface source, ResourceType resourceType, Mapper mapper) throws TransformException, MapperException {

        String assigningAuthority = StringUtils.trim(StringUtils.defaultString(source.getAssigningAuthority())).toLowerCase();
        String identifierTypeCode = StringUtils.trim(StringUtils.defaultString(source.getIdentifierTypeCode())).toLowerCase();

        if (StringUtils.isEmpty(assigningAuthority) && StringUtils.isEmpty(identifierTypeCode))
            return null;

        if (resourceType == ResourceType.Patient)
            return mapper.getCodeMapper().mapPatientIdentifierTypeAndAssigningAuth(assigningAuthority, identifierTypeCode);
        else if (resourceType == ResourceType.EpisodeOfCare)
            return mapper.getCodeMapper().mapEncounterIdentifierTypeAndAssigningAuth(assigningAuthority, identifierTypeCode);
        else if (resourceType == ResourceType.Practitioner)
            return mapper.getCodeMapper().mapDoctorIdentifierTypeAndAssigningAuth(assigningAuthority, identifierTypeCode);
        else
            throw new TransformException("Resource type " + resourceType.name() + " does not have identifier systems mapped");
    }
}
