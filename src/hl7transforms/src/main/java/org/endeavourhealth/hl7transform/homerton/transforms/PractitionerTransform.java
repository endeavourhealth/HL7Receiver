package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.AddressConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PractitionerTransform extends ResourceTransformBase {

    public PractitionerTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Practitioner createMainPrimaryCareProviderPractitioner(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Zpd zpd = OrganizationTransform.getZpd(adtMessage);

        if (zpd == null)
            return null;

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

        Reference primaryCareOrganizationReference = targetResources.getResourceReference(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);

        if (primaryCareOrganizationReference != null)
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent().setManagingOrganization(primaryCareOrganizationReference));

        return practitioner;
    }

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public List<Reference> createPractitioners(List<Xcn> source) throws TransformException, MapperException, ParseException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .collect(Collectors.groupingBy(t -> t.getPrefix() + t.getFamilyName() + t.getGivenName()))
                .values();

        List<Reference> references = new ArrayList<>();

        for (List<Xcn> practitioners : practitionerGroups) {
            Practitioner practitioner = createPractitionerFromDuplicates(practitioners);

            references.add(ReferenceHelper.createReferenceExternal(practitioner));
        }

        return references;
    }

    private Practitioner createPractitionerFromDuplicates(List<Xcn> sources) throws TransformException, MapperException, ParseException {
        Validate.notNull(sources);

        if (sources.size() == 0)
            return null;

        Practitioner practitioner = new Practitioner();

        // name
        if (StringUtils.isBlank(sources.get(0).getFamilyName()))
            throw new TransformException("Family name is blank");

        practitioner.setName(NameConverter.convert(sources.get(0), mapper));


        // identifiers

        for (Xcn source : sources) {

            Identifier identifier = IdentifierConverter.createIdentifier(source, getResourceType());

            if (identifier != null)
                if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifier.getSystem()))
                    practitioner.addIdentifier(identifier);
        }

        // role - collect identifiers to help determine role

        String gmcCode = getIdentifierValue(practitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
        String primaryPersonnelId = getIdentifierValue(practitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String postcode = sources  // practitioner's organisation postcode is stored as a identifier
                .stream()
                .filter(t -> StringUtils.isNotEmpty(t.getId()))
                .filter(t -> StringUtils.deleteWhitespace(StringUtils.defaultString(t.getIdentifierTypeCode())).equalsIgnoreCase("postcode"))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());


        // role - determine and set role

        boolean practitionerAlreadyExists = false;

        if (StringUtils.isNotEmpty(gmcCode)) {

            Organization primaryCareProviderOrganisation = targetResources.getResource(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);
            Practitioner primaryCareProviderPractitioner = targetResources.getResource(ResourceTag.MainPrimaryCareProviderPractitioner, Practitioner.class);

            String existingPrimaryCarePractitionerGmcCode = getIdentifierValue(primaryCareProviderPractitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);


            if (gmcCode.equalsIgnoreCase(existingPrimaryCarePractitionerGmcCode)) {

                // attempt match on primary care provider practitioner GMC code

                practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                    .setManagingOrganization(ReferenceHelper.createReferenceExternal(primaryCareProviderOrganisation)));

                practitionerAlreadyExists = true;

            } else {

                // attempt match on primary care provider organisation postcode
                if (StringUtils.isNotEmpty(postcode)) {

                    String existingPrimaryCareOrganisationPostcode = AddressConverter.getPostcode(primaryCareProviderOrganisation.getAddress());

                    if ((StringUtils.deleteWhitespace(postcode)
                            .equalsIgnoreCase(StringUtils.deleteWhitespace(StringUtils.defaultString(existingPrimaryCareOrganisationPostcode))))) {

                        // post code matches existing primary care provider organisation - add role

                        practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                                .setManagingOrganization(ReferenceHelper.createReferenceExternal(primaryCareProviderOrganisation)));

                    } else {
                        throw new TransformException("Could not determine GP organisation the practitioner has a role with (no match on postcode)");
                    }

                } else {
                    throw new TransformException("Could not determine GP organisation the practitioner has a role with (no postcode)");
                }
            }

        } else if (StringUtils.isNotEmpty(primaryPersonnelId)) {

            Reference hospitalOrganisationReference = this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);

            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                    .setManagingOrganization(hospitalOrganisationReference));

        } else {
            throw new TransformException("Could not determine which organisation the practitioner has a role with");
        }

        // id

        UUID id = getId(practitioner);
        practitioner.setId(id.toString());


        // add to resources collection

        if (!practitionerAlreadyExists)
            targetResources.addResource(practitioner);

        return practitioner;
    }

    private boolean hasIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return (getIdentifierWithSystem(identifiers, system) != null);
    }

    private String getIdentifierValue(List<Identifier> identifiers, String system) {
        Identifier identifier = getIdentifierWithSystem(identifiers, system);

        if (identifier == null)
            return null;

        return identifier.getValue();
    }

    private String getGmcCode(Practitioner practitioner) {
        return getIdentifierValue(practitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
    }

    private Identifier getIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return identifiers
                .stream()
                .filter(t -> t.getSystem().equalsIgnoreCase(system))
                .collect(StreamExtension.firstOrNullCollector());
    }

    private UUID getId(Practitioner source) throws MapperException, TransformException {

        String forename = source.getName().getGiven().get(0).getValue();
        String surname = source.getName().getFamily().get(0).getValue();
        String primaryIdentifier = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String consultantCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);
        String gmcCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

        return mapper.getResourceMapper().mapPractitionerUuid(surname, forename, HomertonConstants.primaryPatientIdentifierTypeCode, primaryIdentifier, consultantCode, gmcCode);
    }
}
