package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.endeavourhealth.hl7transform.common.transform.PractitionerCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class BartsPractitionerTransform extends ResourceTransformBase {

    public BartsPractitionerTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Practitioner createPractitioner(Xcn xcn) throws TransformException, MapperException, ParseException {
        if (xcn == null)
            return null;

        Practitioner practitioner = new Practitioner();

        // name
        if (StringUtils.isBlank(xcn.getFamilyName()))
            throw new TransformException("Family name is blank");

        practitioner.setName(NameConverter.convert(xcn, mapper));

        // identifiers
        Identifier identifier = IdentifierConverter.createIdentifier(xcn, getResourceType(), mapper);

        if (identifier != null)
            practitioner.addIdentifier(identifier);

        // role - determine and set role
        Organization roleOrganisation = calculcatePractitionerRoleOrganisation(practitioner);

        if (roleOrganisation != null) {
            practitioner
                    .addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                            .setManagingOrganization(ReferenceHelper.createReferenceExternal(roleOrganisation)));
        }

        // id
        UUID id = getId(practitioner, roleOrganisation);
        practitioner.setId(id.toString());

        // add to resources collection
        saveToTargetResources(practitioner);

        return practitioner;
    }

    private Organization calculcatePractitionerRoleOrganisation(Practitioner target) throws TransformException {

        // role - collect identifiers to help determine role
        String bartsOrgNumber = PractitionerCommon.getIdentifierValue(target.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_BARTS_ORG_DR_NUMBER);
        String bartsPersonnelId = PractitionerCommon.getIdentifierValue(target.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_BARTS_PERSONNEL_ID);
        String consultantCode = PractitionerCommon.getIdentifierValue(target.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);

        if (StringUtils.isNotEmpty(bartsOrgNumber) || StringUtils.isNotEmpty(bartsPersonnelId) || StringUtils.isNotEmpty(consultantCode)) {
            return targetResources.getResourceSingleOrNull(ResourceTag.MainHospitalOrganisation, Organization.class);
        }

        String gmcCode = PractitionerCommon.getIdentifierValue(target.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

        if (StringUtils.isNotEmpty(gmcCode)) {
            // looks like a gp practitioner

            Organization primaryCareProviderOrganisation = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);
            Practitioner primaryCareProviderPractitioner = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderPractitioner, Practitioner.class);

            // attempt match on primary care provider practitioner GMC code
            if (primaryCareProviderPractitioner != null) {
                String primaryCarePractitionerGmcCode = PractitionerCommon.getIdentifierValue(primaryCareProviderPractitioner.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);

                if (StringUtils.isNotEmpty(primaryCarePractitionerGmcCode))
                    if (gmcCode.equalsIgnoreCase(primaryCarePractitionerGmcCode))
                        return primaryCareProviderOrganisation;
            }

            return null;
        }

        return null;
    }

    public Practitioner createMainPrimaryCareProviderPractitioner(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Pd1Segment pd1Segment = adtMessage.getPd1Segment();

        if (pd1Segment == null)
            return null;

        List<Xcn> xcns = pd1Segment.getPatientPrimaryCareProvider();

        if (xcns.size() == 0)
            return null;

        if (xcns.size() > 1)
            throw new TransformException("More than one patient primary care provider");

        Xcn xcn = xcns.get(0);

        if (StringUtils.isEmpty(xcn.getFamilyName()))
            return null;

        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(xcn.getId())) {
            String gmcCode = StringUtils.deleteWhitespace(xcn.getId()).toUpperCase();

            if (gmcCode.startsWith("G")) {
                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
            }
        }

        practitioner.setName(NameConverter.createOfficialName(xcn.getFamilyName(), xcn.getGivenName(), xcn.getMiddleName(), null));

        Organization primaryCareOrganization = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);

        if (primaryCareOrganization != null) {
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                    .setManagingOrganization(ReferenceHelper.createReferenceExternal(primaryCareOrganization)));
        }

        UUID id = getId(practitioner, primaryCareOrganization);
        practitioner.setId(id.toString());

        return practitioner;
    }

    private UUID getId(Practitioner source, Organization sourceRoleOrganisation) throws MapperException, TransformException {

        String forename = NameConverter.getFirstGivenName(source.getName());
        String surname = NameConverter.getFirstSurname(source.getName());
        String bartsOrgDoctorNumber = PractitionerCommon.getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_BARTS_ORG_DR_NUMBER);
        String bartsPersonnelNumber = PractitionerCommon.getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_BARTS_PERSONNEL_ID);
        String consultantCode = PractitionerCommon.getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);
        String gmcCode = PractitionerCommon.getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
        String gdpCode = PractitionerCommon.getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GDP_NUMBER);
        String odsCode = (sourceRoleOrganisation != null) ? PractitionerCommon.getIdentifierValue(sourceRoleOrganisation.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE) : null;

        if (StringUtils.isNotEmpty(consultantCode)) {
            return mapper
                    .getResourceMapper()
                    .mapPractitionerUuidWithConsultantCode(surname, forename, consultantCode);
        } else if (StringUtils.isNotEmpty(gmcCode)) {
            return mapper
                    .getResourceMapper()
                    .mapPractitionerUuidWithGmcCode(surname, forename, gmcCode);
        } else if (StringUtils.isNotEmpty(gdpCode)) {
            return mapper
                    .getResourceMapper()
                    .mapPractitionerUuidWithGdpCode(surname, forename, gdpCode);
        } else if (StringUtils.isNotEmpty(bartsOrgDoctorNumber) || StringUtils.isNotEmpty(bartsPersonnelNumber)) {
            return mapper
                    .getResourceMapper()
                    .mapPractitionerUuidWithLocalHospitalIdentifiers(
                            surname,
                            forename,
                            BartsConstants.practitionerOrgDoctorNumberAssigningAuth,
                            bartsOrgDoctorNumber,
                            BartsConstants.practitionerPersonnelIdAssigningAuth,
                            bartsPersonnelNumber);
        } else {
            return mapper
                    .getResourceMapper()
                    .mapPractitionerUuid(surname, forename, odsCode);
        }
    }
}
