package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.homerton.HomertonResourceContainer;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.TelecomConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class OrganizationTransform extends HomertonTransformBase {

    public OrganizationTransform(Mapper mapper, HomertonResourceContainer resourceContainer) {
        super(mapper, resourceContainer);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Organization;
    }

    public Reference createHomertonManagingOrganisation(AdtMessage source) throws MapperException, TransformException, ParseException {

        Organization organization = new Organization()
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(HomertonConstants.odsCode))
                .setType(getOrganisationType(OrganisationType.NHS_TRUST))
                .setName(HomertonConstants.organisationName)
                .addAddress(AddressConverter.createWorkAddress(HomertonConstants.addressLine, null, HomertonConstants.addressCity, HomertonConstants.addressPostcode));

        UUID id = mapper.mapOrganisationUuid(HomertonConstants.odsCode, HomertonConstants.organisationName);
        organization.setId(id.toString());

        targetResources.setHomertonOrganisation(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public Reference createHomertonHospitalServiceOrganisation(Pv1Segment pv1Segment) throws TransformException, ParseException, MapperException {
        Validate.notNull(pv1Segment);

        String hospitalServiceName = pv1Segment.getHospitalService();
        String servicingFacilityName = StringUtils.trim(pv1Segment.getServicingFacility()).toUpperCase();

        if (StringUtils.isBlank(hospitalServiceName))
            return null;

        if (!servicingFacilityName.equals(HomertonConstants.servicingFacility))
            throw new TransformException("Hospital servicing facility of " + servicingFacilityName + " not recognised");

        Reference managingOrganisationReference = this.targetResources.getHomertonOrganisationReference();

        Organization organization = new Organization()
                .setName(hospitalServiceName)
                .setType(getOrganisationType(OrganisationType.NHS_TRUST_SERVICE))
                .addAddress(AddressConverter.createWorkAddress(HomertonConstants.organisationName, HomertonConstants.addressLine, HomertonConstants.addressCity, HomertonConstants.addressPostcode))
                .setPartOf(managingOrganisationReference);

        UUID id = mapper.mapOrganisationUuid(HomertonConstants.odsCode, HomertonConstants.organisationName, hospitalServiceName);
        organization.setId(id.toString());

        targetResources.addResource(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public Reference createPrimaryCareProviderOrganisation(Zpd zpd) throws MapperException, TransformException, ParseException {
        if (zpd == null)
            return null;

        if (StringUtils.isBlank(zpd.getPracticeName()))
            return null;

        Organization organization = new Organization();

        UUID id = mapper.mapOrganisationUuid(zpd.getOdsCode(), zpd.getPracticeName());
        organization.setId(id.toString());

        Identifier identifier = IdentifierConverter.createOdsCodeIdentifier(zpd.getOdsCode());

        if (identifier != null)
            organization.addIdentifier(identifier);

        organization.setName(StringHelper.formatName(zpd.getPracticeName()));

        if (StringUtils.isNotBlank(zpd.getPhoneNumber()))
            organization.addTelecom(TelecomConverter.createWorkPhone(zpd.getPhoneNumber()));

        Address address = AddressConverter.createWorkAddress(zpd.getAddressLine1(), zpd.getAddressLine2(), zpd.getTown(), zpd.getPostcode());

        if (address != null)
            organization.addAddress(address);

        organization.setType(getOrganisationType(OrganisationType.GP_PRACTICE));

        targetResources.setPrimaryCareProviderOrganisation(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    private CodeableConcept getOrganisationType(OrganisationType organisationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(organisationType.getSystem())
                        .setDisplay(organisationType.getDescription())
                        .setCode(organisationType.getCode()));
    }
}
