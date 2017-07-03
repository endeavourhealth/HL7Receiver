package org.endeavourhealth.hl7transform.mapper;

import org.endeavourhealth.hl7transform.mapper.code.CodeMapper;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.mapper.organisation.OrganisationMapper;
import org.endeavourhealth.hl7transform.mapper.resource.MappedResourceUuid;
import org.endeavourhealth.hl7transform.mapper.resource.ResourceMapper;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;
import java.util.UUID;

public abstract class Mapper {

    public static final String SCOPE_GLOBAL = "GLOBAL";

    private ResourceMapper resourceMapper;
    private CodeMapper codeMapper;
    private OrganisationMapper organisationMapper;

    protected Mapper() {
        this.resourceMapper = new ResourceMapper(this);
        this.codeMapper = new CodeMapper(this);
        this.organisationMapper = new OrganisationMapper(this);
    }

    public abstract MappedCode mapCode(String codeContext, String code, String codeSystem, String term) throws MapperException;
    public abstract UUID mapGlobalResourceUuid(ResourceType resourceType, String identifier) throws MapperException;
    public abstract UUID mapScopedResourceUuid(ResourceType resourceType, String identifier) throws MapperException;
    public abstract List<MappedResourceUuid> getScopedResourceUuidMappings(String uniqueIdentifierPrefix) throws MapperException;
    public abstract MappedOrganisation mapOrganisation(String odsCode) throws MapperException;

    public ResourceMapper getResourceMapper() {
        return this.resourceMapper;
    }

    public CodeMapper getCodeMapper() {
        return this.codeMapper;
    }

    public OrganisationMapper getOrganisationMapper() { return this.organisationMapper; }
}