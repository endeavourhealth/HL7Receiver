package org.endeavourhealth.hl7transform.mapper;

import org.endeavourhealth.hl7transform.mapper.code.CodeMapper;
import org.endeavourhealth.hl7transform.mapper.code.CodeMapping;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.resource.ResourceMapper;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public abstract class Mapper {

    private ResourceMapper resourceMapper;
    private CodeMapper codeMapper;

    protected Mapper() {
        this.resourceMapper = new ResourceMapper(this);
        this.codeMapper = new CodeMapper(this);
    }

    public abstract CodeMapping mapCode(String codeContext, String code, String codeSystem, String term) throws MapperException;
    public abstract UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException;

    public ResourceMapper getResourceMapper() {
        return this.resourceMapper;
    }

    public CodeMapper getCodeMapper() {
        return this.codeMapper;
    }
}