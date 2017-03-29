package org.endeavourhealth.hl7transform.mapper;

import org.endeavourhealth.hl7transform.mapper.code.CodeMapping;
import org.endeavourhealth.hl7transform.mapper.code.CodeMappingAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class EmptyMapper extends Mapper {
    @Override
    public CodeMapping mapCode(String sourceCodeContextName, String sourceCode, String sourceCodeSystemIdentifier, String sourceTerm) throws MapperException {
        return new CodeMapping()
                .setTargetAction(CodeMappingAction.NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM);
    }

    @Override
    public UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return UUID.randomUUID();
    }
}
