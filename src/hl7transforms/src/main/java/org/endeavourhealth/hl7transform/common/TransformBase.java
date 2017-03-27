package org.endeavourhealth.hl7transform.common;

import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.ResourceType;

public abstract class TransformBase {

    protected Mapper mapper;
    protected ResourceContainer targetResources;

    public TransformBase(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();
}
