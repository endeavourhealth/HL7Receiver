package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.TelecomConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class OrganizationTransform extends ResourceTransformBase {

    public OrganizationTransform(Mapper mapper, ResourceContainer resourceContainer) {
        super(mapper, resourceContainer);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Organization;
    }

    public Organization createHomertonManagingOrganisation(AdtMessage source) throws MapperException, TransformException, ParseException {

        Organization organization = new Organization()
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(HomertonConstants.odsCode))
                .setType(getOrganisationType(OrganisationType.NHS_TRUST))
                .setName(HomertonConstants.organisationName)
                .addAddress(AddressConverter.createWorkAddress(HomertonConstants.addressLine, null, HomertonConstants.addressCity, HomertonConstants.addressPostcode));

        UUID id = mapper.getResourceMapper().mapOrganisationUuid(HomertonConstants.odsCode, HomertonConstants.organisationName);
        organization.setId(id.toString());

        return organization;
    }

    public Reference createHomertonHospitalServiceOrganisation(Pv1Segment pv1Segment) throws TransformException, ParseException, MapperException {
        Validate.notNull(pv1Segment);

        String hospitalServiceName = pv1Segment.getHospitalService();
        String servicingFacilityName = StringUtils.trim(pv1Segment.getServicingFacility()).toUpperCase();

        if (StringUtils.isBlank(hospitalServiceName))
            return null;

        if (StringUtils.isNotBlank(servicingFacilityName))
            if (!servicingFacilityName.equals(HomertonConstants.servicingFacility))
                throw new TransformException("Hospital servicing facility of " + servicingFacilityName + " not recognised");

        Reference managingOrganisationReference = this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);

        Organization organization = new Organization()
                .setName(hospitalServiceName)
                .setType(getOrganisationType(OrganisationType.NHS_TRUST_SERVICE))
                .addAddress(AddressConverter.createWorkAddress(HomertonConstants.organisationName, HomertonConstants.addressLine, HomertonConstants.addressCity, HomertonConstants.addressPostcode))
                .setPartOf(managingOrganisationReference);

        UUID id = mapper.getResourceMapper().mapOrganisationUuid(HomertonConstants.odsCode, HomertonConstants.organisationName, hospitalServiceName);
        organization.setId(id.toString());

        targetResources.addResource(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public static Zpd getZpd(AdtMessage adtMessage) {
        Validate.notNull(adtMessage);

        Pd1Segment pd1Segment = adtMessage.getPd1Segment();

        if (pd1Segment == null)
            return null;

        return pd1Segment.getFieldAsDatatype(HomertonConstants.homertonXpdPrimaryCarePd1FieldNumber, Zpd.class);
    }

    public Organization createMainPrimaryCareProviderOrganisation(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Zpd zpd = getZpd(adtMessage);

        if (zpd == null)
            return null;

        if (StringUtils.isBlank(zpd.getPracticeName()))
            return null;

        Organization organization = new Organization();

        UUID id = mapper.getResourceMapper().mapOrganisationUuid(zpd.getOdsCode(), zpd.getPracticeName());
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

        return organization;
    }

    private CodeableConcept getOrganisationType(OrganisationType organisationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(organisationType.getSystem())
                        .setDisplay(organisationType.getDescription())
                        .setCode(organisationType.getCode()));
    }
}
