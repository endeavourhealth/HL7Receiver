package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.homerton.HomertonResourceContainer;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PractitionerTransform extends HomertonTransformBase {

    public PractitionerTransform(Mapper mapper, HomertonResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Reference createPrimaryCarePractitioner(Zpd zpd, Reference primaryCareOrganizationReference) throws MapperException, TransformException, ParseException {
        if (StringUtils.isBlank(zpd.getSurname()))
            return null;

        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(zpd.getGmcCode())) {
            String gmcCode = StringUtils.deleteWhitespace(zpd.getGmcCode()).toUpperCase();

            if (IdentifierConverter.looksLikeGmcCode(gmcCode)) {
                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
            }
        }

        practitioner.setName(NameConverter.createUsualName(zpd.getSurname(), zpd.getForenames(), null));

        UUID id = getId(practitioner);
        practitioner.setId(id.toString());

        if (primaryCareOrganizationReference != null)
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent().setManagingOrganization(primaryCareOrganizationReference));

        targetResources.addResource(practitioner);

        return ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId());
    }

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public List<Reference> createHospitalPractitioners(List<Xcn> source, Reference hospitalOrganisationReference) throws TransformException, MapperException, ParseException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .collect(Collectors.groupingBy(t -> t.getPrefix() + t.getFamilyName() + t.getGivenName()))
                .values();

        List<Reference> references = new ArrayList<>();

        for (List<Xcn> practitioners : practitionerGroups) {
            Practitioner practitioner = createPractitionerFromDuplicates(practitioners, hospitalOrganisationReference);

            references.add(ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId()));
        }

        return references;
    }

    private Practitioner createPractitionerFromDuplicates(List<Xcn> sources, Reference hospitalOrganisationReference) throws TransformException, MapperException, ParseException {
        Validate.notNull(sources);

        if (sources.size() == 0)
            return null;

        Practitioner practitioner = new Practitioner();
        practitioner.setName(NameConverter.convert(sources.get(0)));

        for (Xcn source : sources) {

            Identifier identifier = IdentifierConverter.createIdentifier(source, getResourceType());

            if (identifier != null)
                if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifier.getSystem()))
                    practitioner.addIdentifier(identifier);
        }

        if (hospitalOrganisationReference != null) {

            if ((practitioner.getIdentifier().size() == 0)
                    || ((practitioner.getIdentifier().size() == 1) && (practitioner.getIdentifier().get(0).getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER))))
                throw new TransformException("Could not find hospital practitioner identifiers");

            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                            .setManagingOrganization(hospitalOrganisationReference));
        }

        UUID id = getId(practitioner);
        practitioner.setId(id.toString());

        return practitioner;
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

    private UUID getId(Practitioner source) throws MapperException, TransformException {

        String forename = source.getName().getGiven().get(0).getValue();
        String surname = source.getName().getFamily().get(0).getValue();
        String primaryIdentifier = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String consultantCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);
        String gmcCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

        return mapper.mapPractitionerUuid(surname, forename, HomertonConstants.primaryPatientIdentifierTypeCode, primaryIdentifier, consultantCode, gmcCode);
    }
}
