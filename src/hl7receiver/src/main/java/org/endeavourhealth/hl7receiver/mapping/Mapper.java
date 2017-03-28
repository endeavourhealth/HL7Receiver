package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbCode;
import org.endeavourhealth.hl7transform.mapper.CodeMapping;
import org.endeavourhealth.hl7transform.mapper.CodeMappingAction;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class Mapper extends org.endeavourhealth.hl7transform.mapper.Mapper {

    private int channelId;
    private String sendingApplication;
    private DataLayer dataLayer;

    public Mapper(int channelId, String sendingApplication, DataLayer dataLayer) {
        this.channelId = channelId;
        this.sendingApplication = sendingApplication;
        this.dataLayer = dataLayer;
    }

    @Override
    public CodeMapping mapCode(String sourceCodeContextName, String sourceCode, String sourceCodeSystemIdentifier, String sourceTerm) throws MapperException {
        try {
            DbCode dbCode = this.dataLayer.getCode(this.sendingApplication, sourceCodeContextName, sourceCode, sourceCodeSystemIdentifier, sourceTerm);

            return new CodeMapping()
                    .setMapped(dbCode.isMapped())
                    .setTargetAction(CodeMappingAction.fromIdentifier(dbCode.getTargetAction()))
                    .setCode(dbCode.getCode())
                    .setSystem(dbCode.getSystem())
                    .setTerm(dbCode.getTerm());

        } catch (Exception e) {
            throw new MapperException("Exception while mapping code, see cause");
        }
    }

    @Override
    public UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        try {
            return this.dataLayer.getResourceUuid(channelId, resourceType.toString(), identifier);
        } catch (Exception e) {
            throw new MapperException("Exception while getting resource UUID, see cause", e);
        }
    }
}
