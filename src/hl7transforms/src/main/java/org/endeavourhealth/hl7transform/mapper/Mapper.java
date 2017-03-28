package org.endeavourhealth.hl7transform.mapper;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public abstract class Mapper {

    private ResourceMapper resourceMapper;

    public abstract CodeMapping mapCode(String sourceCodeContextName, String sourceCode, String sourceCodeSystemIdentifier, String sourceTerm) throws MapperException;
    public abstract UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException;

    public ResourceMapper getResourceMapper() {
        if (resourceMapper == null)
            this.resourceMapper = new ResourceMapper(this);

        return this.resourceMapper;
    }
}