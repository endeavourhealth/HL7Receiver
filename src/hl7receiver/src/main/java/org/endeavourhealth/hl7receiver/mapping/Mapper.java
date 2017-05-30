package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class Mapper extends org.endeavourhealth.hl7transform.mapper.Mapper {

    private int channelId;
    private String sendingFacility;
    private DataLayer dataLayer;
    private CodeCache codeCache;
    private ResourceUuidCache resourceUuidCache;

    public Mapper(int channelId, String sendingFacility, DataLayer dataLayer) {
        this.channelId = channelId;
        this.sendingFacility = sendingFacility;
        this.dataLayer = dataLayer;
        this.codeCache = new CodeCache(MappedCodeAction.MAPPED_INCLUDE);
        this.resourceUuidCache = new ResourceUuidCache(ResourceType.Organization, ResourceType.Location, ResourceType.Practitioner);
    }

    @Override
    public MappedCode mapCode(String context, String code, String codeSystem, String term) throws MapperException {
        try {
            MappedCode mappedCode = this.codeCache.getMappedCode(context, code, codeSystem, term);

            if (mappedCode == null) {

                DbCode dbCode = this.dataLayer.getCode(this.sendingFacility, context, code, codeSystem, term);

                mappedCode = new MappedCode()
                        .setTargetAction(MappedCodeAction.fromIdentifier(dbCode.getTargetAction()))
                        .setCode(dbCode.getCode())
                        .setSystem(dbCode.getSystem())
                        .setTerm(dbCode.getTerm());

                this.codeCache.putMappedCode(context, code, codeSystem, term, mappedCode);
            }

            return mappedCode;

        } catch (Exception e) {
            throw new MapperException("Exception while mapping code, see cause", e);
        }
    }

    @Override
    public UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        try {
            UUID resourceUuid = resourceUuidCache.getResourceUuid(resourceType, identifier);

            if (resourceUuid == null) {
                resourceUuid = this.dataLayer.getResourceUuid(channelId, resourceType.toString(), identifier);
                resourceUuidCache.putResourceUuid(resourceType, identifier, resourceUuid);
            }

            return resourceUuid;

        } catch (Exception e) {
            throw new MapperException("Exception while getting resource UUID, see cause", e);
        }
    }
}
