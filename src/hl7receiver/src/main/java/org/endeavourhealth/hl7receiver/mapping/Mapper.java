package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.transform.hl7v2.mapper.Code;
import org.endeavourhealth.transform.hl7v2.mapper.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class Mapper extends org.endeavourhealth.transform.hl7v2.mapper.Mapper {

    private DataLayer dataLayer;
    private int channelId;

    public Mapper(int channelId, DataLayer dataLayer) {
        this.channelId = channelId;
        this.dataLayer = dataLayer;
    }

    @Override
    public Code mapCode(String mapContext, Code code) throws MapperException {
        return null;
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
