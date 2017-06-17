package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Xon;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
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

public class OrganizationTransform extends ResourceTransformBase {

    public OrganizationTransform(Mapper mapper, ResourceContainer resourceContainer) {
        super(mapper, resourceContainer);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Organization;
    }

    public Organization createBartsManagingOrganisation(AdtMessage source) throws MapperException, TransformException, ParseException {

        Organization organization = new Organization()
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(BartsConstants.odsCode))
                .setType(getOrganisationType(OrganisationType.NHS_TRUST))
                .setName(BartsConstants.organisationName)
                .addAddress(AddressConverter.createWorkAddress(BartsConstants.addressLine1, BartsConstants.addressLine2, BartsConstants.town, BartsConstants.addressPostcode));

        UUID id = mapper.getResourceMapper().mapOrganisationUuid(BartsConstants.odsCode, BartsConstants.organisationName);
        organization.setId(id.toString());

        return organization;
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

        MappedOrganisation mappedOrganisation = mapper.getOrganisationMapper().mapOrganisation(odsCode);

        if (mappedOrganisation == null)
            throw new TransformException("Could not map organisation " + odsCode);

        return OrganisationCommon.createFromMappedOrganisation(mappedOrganisation, mapper.getResourceMapper());
    }


    private CodeableConcept getOrganisationType(OrganisationType organisationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(organisationType.getSystem())
                        .setDisplay(organisationType.getDescription())
                        .setCode(organisationType.getCode()));
    }
}
