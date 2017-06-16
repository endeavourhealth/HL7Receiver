package org.endeavourhealth.hl7transform.mapper.organisation;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;

public class OrganisationMapper {
    private Mapper mapper;

    public OrganisationMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MappedOrganisation mapOrganisation(String odsCode) throws MapperException {
        return this.mapper.mapOrganisation(odsCode);
    }
}
