package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.hl7.fhir.instance.model.*;

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

    private CodeableConcept getOrganisationType(OrganisationType organisationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(organisationType.getSystem())
                        .setDisplay(organisationType.getDescription())
                        .setCode(organisationType.getCode()));
    }
}
