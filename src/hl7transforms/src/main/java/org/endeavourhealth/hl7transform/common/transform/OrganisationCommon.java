package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.mapper.resource.ResourceMapper;
import org.hl7.fhir.instance.model.*;

import java.util.UUID;

public class OrganisationCommon {

    public static Organization createOrganisation(String odsCode, Mapper mapper) throws MapperException, TransformException {
        MappedOrganisation mappedOrganisation = mapper.getOrganisationMapper().mapOrganisation(odsCode);

        if (mappedOrganisation == null)
            return null;

        return OrganisationCommon.createFromMappedOrganisation(mappedOrganisation, mapper.getResourceMapper());
    }

    private static Organization createFromMappedOrganisation(MappedOrganisation mappedOrganisation, ResourceMapper resourceMapper) throws MapperException {
        if (mappedOrganisation == null)
            return null;

        Organization organization = new Organization();

        UUID id = resourceMapper.mapOrganisationUuid(mappedOrganisation.getOdsCode(), mappedOrganisation.getOrganisationName());
        organization.setId(id.toString());

        Identifier identifier = IdentifierConverter.createOdsCodeIdentifier(mappedOrganisation.getOdsCode());

        if (identifier != null)
            organization.addIdentifier(identifier);

        organization.setName(StringHelper.formatName(mappedOrganisation.getOrganisationName()));

        Address address = AddressConverter.createWorkAddress(mappedOrganisation.getAddressLine1(), mappedOrganisation.getAddressLine2(), mappedOrganisation.getTown(), mappedOrganisation.getPostcode());

        if (address != null)
            organization.addAddress(address);

        if (mappedOrganisation.getOrganisationType() != null)
            organization.setType(getOrganisationType(mappedOrganisation.getOrganisationType()));

        return organization;
    }

    public static CodeableConcept getOrganisationType(OrganisationType organisationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(organisationType.getSystem())
                        .setDisplay(organisationType.getDescription())
                        .setCode(organisationType.getCode()));
    }
}
