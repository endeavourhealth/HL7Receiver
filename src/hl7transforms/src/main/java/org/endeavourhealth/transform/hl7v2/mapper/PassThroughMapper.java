package org.endeavourhealth.transform.hl7v2.mapper;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class PassThroughMapper extends Mapper {
    public PassThroughMapper() {
    }

    @Override
    public Code mapCode(String mapContext, Code code) throws MapperException {
        return null;
    }

    @Override
    public UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return null;
    }
}