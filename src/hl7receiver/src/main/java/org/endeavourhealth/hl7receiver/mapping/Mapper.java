package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbCode;
import org.endeavourhealth.hl7transform.mapper.code.CodeMapping;
import org.endeavourhealth.hl7transform.mapper.code.CodeMappingAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class Mapper extends org.endeavourhealth.hl7transform.mapper.Mapper {

    private int channelId;
    private String sendingFacility;
    private DataLayer dataLayer;

    public Mapper(int channelId, String sendingFacility, DataLayer dataLayer) {
        this.channelId = channelId;
        this.sendingFacility = sendingFacility;
        this.dataLayer = dataLayer;
    }

    @Override
    public CodeMapping mapCode(String context, String code, String codeSystem, String term) throws MapperException {
        try {
            DbCode dbCode = this.dataLayer.getCode(this.sendingFacility, context, code, codeSystem, term);

            return new CodeMapping()
                    .setTargetAction(CodeMappingAction.fromIdentifier(dbCode.getTargetAction()))
                    .setCode(dbCode.getCode())
                    .setSystem(dbCode.getSystem())
                    .setTerm(dbCode.getTerm());

        } catch (Exception e) {
            throw new MapperException("Exception while mapping code, see cause", e);
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
