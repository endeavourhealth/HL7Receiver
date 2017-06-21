package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

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
import org.endeavourhealth.hl7transform.transforms.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class HomertonPractitionerTransform extends ResourceTransformBase {

    public HomertonPractitionerTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Practitioner createMainPrimaryCareProviderPractitioner(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Zpd zpd = HomertonOrganizationTransform.getZpd(adtMessage);

        if (zpd == null)
            return null;

        if (StringUtils.isBlank(zpd.getSurname()))
            return null;

        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(zpd.getGmcCode())) {
            String gmcCode = StringUtils.deleteWhitespace(zpd.getGmcCode()).toUpperCase();

                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
        }

        practitioner.setName(NameConverter.createOfficialName(zpd.getSurname(), zpd.getForenames(), null));

        Organization primaryCareOrganization = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);

        if (primaryCareOrganization != null) {
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                    .setManagingOrganization(ReferenceHelper.createReferenceExternal(primaryCareOrganization)));
        }

        UUID id = getId(practitioner, primaryCareOrganization);
        practitioner.setId(id.toString());

        return practitioner;
    }

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public List<Reference> createPractitioners(List<Xcn> source) throws TransformException, MapperException, ParseException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .filter(t -> StringUtils.isNotBlank(t.getFamilyName()))
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

            Identifier identifier = IdentifierConverter.createIdentifier(source, getResourceType(), mapper);

            if (identifier != null)
                if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifier.getSystem()))
                    practitioner.addIdentifier(identifier);
        }

        // role - determine and set role

        Organization roleOrganisation = calculcatePractitionerRoleOrganisationFromDuplicates(practitioner, sources);

        if (roleOrganisation != null) {
            practitioner
                    .addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                            .setManagingOrganization(ReferenceHelper.createReferenceExternal(roleOrganisation)));
        }

        // id

        UUID id = getId(practitioner, roleOrganisation);
        practitioner.setId(id.toString());

        // add to resources collection

        if (!targetResources.hasResource(practitioner.getId()))
            targetResources.addResource(practitioner);

        return practitioner;
    }

    private Organization calculcatePractitionerRoleOrganisationFromDuplicates(Practitioner practitioner, List<Xcn> sources) throws TransformException {

        // role - collect identifiers to help determine role
        String gmcCode = getIdentifierValue(practitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
        String primaryPersonnelId = getIdentifierValue(practitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String postcode = sources  // practitioner's organisation postcode is stored as a identifier
                .stream()
                .filter(t -> StringUtils.isNotEmpty(t.getId()))
                .filter(t -> StringUtils.deleteWhitespace(StringUtils.defaultString(t.getIdentifierTypeCode())).equalsIgnoreCase("postcode"))
                .map(t -> StringUtils.trim(StringUtils.defaultString(t.getId())).toUpperCase())
                .collect(StreamExtension.firstOrNullCollector());

        if (StringUtils.isNotEmpty(gmcCode)) {
            // looks like a gp practitioner

            Organization primaryCareProviderOrganisation = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);
            Practitioner primaryCareProviderPractitioner = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderPractitioner, Practitioner.class);

            // attempt match on primary care provider practitioner GMC code
            if (primaryCareProviderPractitioner != null) {
                String primaryCarePractitionerGmcCode = getIdentifierValue(primaryCareProviderPractitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

                if (StringUtils.isNotEmpty(primaryCarePractitionerGmcCode))
                    if (gmcCode.equalsIgnoreCase(primaryCarePractitionerGmcCode))
                        return primaryCareProviderOrganisation;
            }

            // attempt match on primary care provider organisation postcode
            if (primaryCareProviderOrganisation != null) {
                String existingPrimaryCareOrganisationPostcode = StringUtils.defaultString(AddressConverter.getPostcode(primaryCareProviderOrganisation.getAddress()));

                if (StringUtils.isNotEmpty(postcode)) {

                    if ((StringUtils.deleteWhitespace(postcode)
                            .equalsIgnoreCase(StringUtils.deleteWhitespace(existingPrimaryCareOrganisationPostcode)))) {

                        return primaryCareProviderOrganisation;

                    }
                }
            }

        } else if (StringUtils.isNotEmpty(primaryPersonnelId)) {

            return this.targetResources.getResourceSingle(ResourceTag.MainHospitalOrganisation, Organization.class);
        }

        // else could not match

        return null;
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
        if (identifiers == null)
            return null;

        return identifiers
                .stream()
                .filter(t -> t.getSystem().equalsIgnoreCase(system))
                .collect(StreamExtension.firstOrNullCollector());
    }

    private UUID getId(Practitioner source, Organization sourceRoleOrganisation) throws MapperException, TransformException {

        String forename = NameConverter.getFirstGivenName(source.getName());
        String surname = NameConverter.getFirstSurname(source.getName());
        String primaryIdentifier = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String consultantCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);
        String gmcCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
        String odsCode = "";

        if (sourceRoleOrganisation != null)
            odsCode = getIdentifierValue(sourceRoleOrganisation.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);

        if (StringUtils.isBlank(primaryIdentifier)
            && StringUtils.isBlank(consultantCode)
                && StringUtils.isBlank(gmcCode)) {

            return mapper.getResourceMapper().mapPractitionerUuid(surname, forename, odsCode);
        }

        return mapper.getResourceMapper().mapPractitionerUuid(surname, forename, HomertonConstants.primaryPractitionerIdentifierTypeCode, primaryIdentifier, consultantCode, gmcCode);
    }
}
