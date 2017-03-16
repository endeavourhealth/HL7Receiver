package org.endeavourhealth.hl7transform.transform.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7transform.transform.ResourceContainer;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7transform.transform.TransformException;
import org.endeavourhealth.hl7transform.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PractitionerTransform {

    private String sendingFacility;
    private Mapper mapper;
    private ResourceContainer resourceContainer;

    public PractitionerTransform(String sendingFacility, Mapper mapper, ResourceContainer resourceContainer) {
        this.sendingFacility = sendingFacility;
        this.mapper = mapper;
        this.resourceContainer = resourceContainer;
    }

    public Reference createPrimaryCarePractitioner(String gmcCode, String surname, String forenames, Reference primaryCareOrganizationReference) throws MapperException {
        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(gmcCode)) {
            gmcCode = StringUtils.deleteWhitespace(gmcCode).toUpperCase();

            if (gmcCode.startsWith("G")) {
                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
            }
        }

        practitioner.setName(NameConverter.createUsualName(surname, forenames, null));

        UUID id = getId(practitioner);

        practitioner.setId(id.toString());

        if (resourceContainer.getResource(Practitioner.class, practitioner.getId()) == null)
            resourceContainer.add(practitioner);

        if (primaryCareOrganizationReference != null)
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent().setManagingOrganization(primaryCareOrganizationReference));

        return ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId());
    }

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public List<Reference> transformAndGetReferences(List<Xcn> source) throws TransformException, MapperException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .collect(Collectors.groupingBy(t -> t.getPrefix() + t.getFamilyName() + t.getGivenName()))
                .values();

        List<Reference> references = new ArrayList<>();

        for (List<Xcn> practitioners : practitionerGroups) {
            Practitioner practitioner = createPractitionerFromDuplicates(practitioners);

            if (resourceContainer.getResource(Practitioner.class, practitioner.getId()) == null)
                resourceContainer.add(practitioner);

            references.add(ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId()));
        }

        return references;
    }

    private Practitioner createPractitionerFromDuplicates(List<Xcn> sources) throws TransformException, MapperException {
        Validate.notNull(sources);

        if (sources.size() == 0)
            return null;

        Practitioner practitioner = new Practitioner();
        practitioner.setName(NameConverter.convert(sources.get(0)));

        for (Xcn source : sources) {
            if (StringUtils.isNotBlank(source.getId())) {

                String identifierSystem = getIdentifierSystem(source.getId(), source.getAssigningAuthority(), source.getIdentifierTypeCode());

                if (StringUtils.isNotBlank(identifierSystem)) {

                    if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifierSystem)) {

                        practitioner.addIdentifier(new Identifier()
                                .setValue(source.getId().trim())
                                .setSystem(identifierSystem));
                    }
                }
            }
        }

        UUID id = getId(practitioner);

        practitioner.setId(id.toString());

        return practitioner;
    }

    private UUID getId(Practitioner source) throws MapperException {

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

    private boolean hasIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return (getIdentifierValue(identifiers, system) != null);
    }

    private String getIdentifierValue(List<Identifier> identifiers, String system) {
        return identifiers
                .stream()
                .filter(t -> t.getSystem().equals(system))
                .map(t -> t.getValue())
                .collect(StreamExtension.firstOrNullCollector());
    }

    private String getIdentifierSystem(String id, String assigningAuth, String typeCode) {
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
}
