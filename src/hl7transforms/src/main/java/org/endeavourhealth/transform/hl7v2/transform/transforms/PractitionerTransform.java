package org.endeavourhealth.transform.hl7v2.transform.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.hl7v2.mapper.Mapper;
import org.endeavourhealth.transform.hl7v2.mapper.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.transform.hl7v2.transform.ResourceContainer;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.endeavourhealth.transform.hl7v2.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PractitionerTransform {

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public static List<Reference> transformAndGetReferences(List<Xcn> source, String sendingFacility, Mapper mapper, ResourceContainer resourceContainer) throws TransformException, MapperException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .collect(Collectors.groupingBy(t -> t.getPrefix() + t.getFamilyName() + t.getGivenName()))
                .values();

        List<Reference> references = new ArrayList<>();

        for (List<Xcn> practitioners : practitionerGroups) {
            Practitioner practitioner = createPractitionerFromDuplicates(practitioners, sendingFacility, mapper);

            if (resourceContainer.getResource(Practitioner.class, practitioner.getId()) == null)
                resourceContainer.add(practitioner);

            references.add(ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId()));
        }

        return references;
    }

    private static Practitioner createPractitionerFromDuplicates(List<Xcn> sources, String sendingFacility, Mapper mapper) throws TransformException, MapperException {
        Validate.notNull(sources);

        if (sources.size() == 0)
            return null;

        Practitioner practitioner = new Practitioner();
        practitioner.setName(NameConverter.convert(sources.get(0)));

        for (Xcn source : sources) {
            if (StringUtils.isNotBlank(source.getId())) {

                String identifierSystem = getIdentifierSystem(source.getId(), source.getAssigningAuthority(), source.getIdentifierTypeCode(), sendingFacility);

                if (StringUtils.isNotBlank(identifierSystem)) {

                    if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifierSystem)) {

                        practitioner.addIdentifier(new Identifier()
                                .setValue(source.getId().trim())
                                .setSystem(identifierSystem));
                    }
                }
            }
        }

        UUID id = getId(practitioner, sendingFacility, mapper);

        practitioner.setId(id.toString());

        return practitioner;
    }

    private static UUID getId(Practitioner source, String sendingFacility, Mapper mapper) throws MapperException {

        String uniqueIdentifyingString = sendingFacility + "-Practitioner-";

        String forename = source.getName().getGiven().get(0).getValue().toUpperCase().trim();
        String surname = source.getName().getFamily().get(0).getValue().toUpperCase().trim();

        uniqueIdentifyingString += surname + "-" + forename;

        String primaryIdentifier = getIdentifierValue(source.getIdentifier(), FhirUri.getIdentifierSystemHl7v2LocalPractitionerIdentifier(sendingFacility, "personnel primary identifier", "personnel primary identifier"));

        if (StringUtils.isNotBlank(primaryIdentifier))
            uniqueIdentifyingString += "-PersonnelPrimaryIdentifier=" + primaryIdentifier;

        String consultantCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);

        if (StringUtils.isNotBlank(consultantCode))
            uniqueIdentifyingString += "-ConsultantCode=" + consultantCode;

        String gmcCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

        if (StringUtils.isNotBlank(gmcCode))
            uniqueIdentifyingString += "-GmcCode=" + gmcCode;

        return mapper.mapResourceUuid(ResourceType.Practitioner, uniqueIdentifyingString);
    }

    private static boolean hasIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return (getIdentifierValue(identifiers, system) != null);
    }

    private static String getIdentifierValue(List<Identifier> identifiers, String system) {
        return identifiers
                .stream()
                .filter(t -> t.getSystem().equals(system))
                .map(t -> t.getValue())
                .collect(StreamExtension.firstOrNullCollector());
    }


    private static String getIdentifierSystem(String id, String assigningAuth, String typeCode, String sendingFacility) {
        if (id == null)
            id = "";

        if (assigningAuth == null)
            assigningAuth = "";

        if (typeCode == null)
            typeCode = "";

        assigningAuth = assigningAuth.trim().toLowerCase();
        typeCode = typeCode.trim().toLowerCase();
        id = id.trim().toLowerCase();

        String combined = assigningAuth + " | " + typeCode;

        switch (combined) {
            case "nhs consultant number | non gp":
            case "community dr nbr | community dr nbr":
            case " | community dr nbr":
                return FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE;

            case "external id | external identifier":
                if (id.startsWith("g"))
                    return FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER;
                break;

            case "personnel primary identifier | personnel primary identifier":
                return FhirUri.getIdentifierSystemHl7v2LocalPractitionerIdentifier(sendingFacility, assigningAuth, typeCode);

            default: break;
        }

        return null;
    }
//
//    public static Reference transformAndGetReference(Xcn source, Mapper mapper, ResourceContainer resourceContainer) throws TransformException, MapperException {
//        if (source == null)
//            return null;
//
//        Practitioner practitioner = createPractitioner(source, mapper);
//
//        if (practitioner != null)
//            if (!resourceContainer.hasResource(Practitioner.class, practitioner.getId()))
//                resourceContainer.add(practitioner);
//
//        return ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId());
//    }
//
//    private static Practitioner createPractitioner(Xcn source, Mapper mapper) throws TransformException, MapperException {
//
//        UUID id = getId(source, mapper);
//
//        Practitioner practitioner = new Practitioner();
//        practitioner.setId(id.toString());
//        practitioner.setName(NameConverter.convert(source));
//
//        return practitioner;
//    }
}
