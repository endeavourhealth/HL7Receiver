package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

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
import org.endeavourhealth.hl7transform.common.transform.OrganisationCommon;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.transforms.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.common.converters.TelecomConverter;
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

        Organization result = OrganisationCommon.createOrganisation(HomertonConstants.odsCode, mapper);

        if (result == null)
            throw new TransformException("Could not create Homerton managing organisation");

        return result;
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

        Organization managingOrganisation = this.targetResources.getResourceSingle(ResourceTag.MainHospitalOrganisation, Organization.class);

        Organization organization = new Organization()
                .setName(hospitalServiceName)
                .setType(OrganisationCommon.getOrganisationType(OrganisationType.NHS_TRUST_SERVICE))
                .setPartOf(ReferenceHelper.createReference(managingOrganisation.getResourceType(), managingOrganisation.getId()));

        if (managingOrganisation.getAddress().size() > 0) {
            Address hospitalServiceAddress = managingOrganisation.getAddress().get(0).copy();
            hospitalServiceAddress.getLine().add(0, new StringType(managingOrganisation.getName()));
            organization.addAddress(hospitalServiceAddress);
        }

        UUID id = mapper.getResourceMapper().mapOrganisationUuid(HomertonConstants.odsCode, managingOrganisation.getName(), hospitalServiceName);
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

        if (StringUtils.isNotBlank(zpd.getOdsCode())) {
            Organization organization = OrganisationCommon.createOrganisation(zpd.getOdsCode(), mapper);

            if (organization != null)
                return organization;
        }

        return createFromZpd(zpd);
    }

    private Organization createFromZpd(Zpd zpd) throws MapperException {
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

        organization.setType(OrganisationCommon.getOrganisationType(OrganisationType.GP_PRACTICE));

        return organization;
    }
}
