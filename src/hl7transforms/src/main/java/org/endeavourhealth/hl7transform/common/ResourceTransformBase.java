package org.endeavourhealth.hl7transform.common;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.ResourceType;

public abstract class ResourceTransformBase {

    protected Mapper mapper;
    protected ResourceContainer targetResources;

    public ResourceTransformBase(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();
}
