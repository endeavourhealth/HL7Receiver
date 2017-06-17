package org.endeavourhealth.hl7transform.mapper;

import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class EmptyMapper extends Mapper {
    @Override
    public MappedCode mapCode(String sourceCodeContextName, String sourceCode, String sourceCodeSystemIdentifier, String sourceTerm) throws MapperException {
        return new MappedCode()
                .setTargetAction(MappedCodeAction.NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM);
    }

    @Override
    public UUID mapGlobalResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return UUID.randomUUID();
    }

    @Override
    public UUID mapScopedResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return UUID.randomUUID();
    }

    @Override
    public MappedOrganisation mapOrganisation(String odsCode) throws MapperException {
        return null;
    }
}
