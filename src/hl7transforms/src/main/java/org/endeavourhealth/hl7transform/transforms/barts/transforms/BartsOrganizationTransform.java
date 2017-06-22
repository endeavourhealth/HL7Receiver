package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Xon;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.transform.OrganisationCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.transforms.homerton.parser.zdatatypes.Zpd;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class BartsOrganizationTransform extends ResourceTransformBase {

    public BartsOrganizationTransform(Mapper mapper, ResourceContainer resourceContainer) {
        super(mapper, resourceContainer);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Organization;
    }

    public Organization createBartsManagingOrganisation(AdtMessage source) throws MapperException, TransformException, ParseException {

        Organization result = OrganisationCommon.createOrganisation(BartsConstants.odsCode, mapper);

        if (result == null)
            throw new TransformException("Could not create Barts managing organisation");

        return result;
    }

    public Reference createBartsHospitalServiceOrganisation(Pv1Segment pv1Segment) throws TransformException, ParseException, MapperException {
        Validate.notNull(pv1Segment);

        String hospitalServiceName = pv1Segment.getHospitalService();

        if (StringUtils.isBlank(hospitalServiceName))
            return null;

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

        UUID id = mapper.getResourceMapper().mapOrganisationUuidForHospitalService(BartsConstants.odsCode, hospitalServiceName);
        organization.setId(id.toString());

        targetResources.addResource(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public Organization createMainPrimaryCareProviderOrganisation(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Pd1Segment pd1Segment = adtMessage.getPd1Segment();

        if (pd1Segment == null)
            return null;

        if (pd1Segment.getPatientPrimaryCareFacility() == null)
            return null;

        List<Xon> primaryCareFacilities = pd1Segment.getPatientPrimaryCareFacility();

        if (primaryCareFacilities.size() == 0)
            return null;

        if (primaryCareFacilities.size() > 1)
            throw new TransformException("More than one patient primary care facility");

        Xon xon = primaryCareFacilities.get(0);

        String odsCode = xon.getIdNumber();

        if (StringUtils.isEmpty(odsCode)) {
            if (StringUtils.isEmpty(xon.getOrganizationName()))
                return null;

            throw new TransformException("ODS code blank but organisation name populated");
        }

        Organization result = OrganisationCommon.createOrganisation(odsCode, mapper);

        if (result == null)
            throw new TransformException("Could not create organisation " + odsCode);

        return result;
    }
}
