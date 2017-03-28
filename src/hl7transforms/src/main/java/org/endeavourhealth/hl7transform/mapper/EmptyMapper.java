package org.endeavourhealth.hl7transform.mapper;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class EmptyMapper extends Mapper {
    @Override
    public CodeMapping mapCode(String sourceCodeContextName, String sourceCode, String sourceCodeSystemIdentifier, String sourceTerm) throws MapperException {
        return new CodeMapping()
                .setMapped(false)
                .setTargetAction(CodeMappingAction.INCLUDE_ONLY_SOURCE_TERM);
    }

    @Override
    public UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return UUID.randomUUID();
    }
}
