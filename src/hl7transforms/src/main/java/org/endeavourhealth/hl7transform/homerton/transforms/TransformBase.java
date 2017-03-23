package org.endeavourhealth.hl7transform.homerton.transforms;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public abstract class TransformBase {

    protected Mapper mapper;
    protected ResourceContainer targetResources;

    public TransformBase(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();

    protected void mapAndSetId(String uniqueIdentifyingString, Resource target) throws MapperException, TransformException, ParseException {

        UUID resourceUuid = mapper.mapResourceUuid(getResourceType(), uniqueIdentifyingString);
        target.setId(resourceUuid.toString());
    }
}
