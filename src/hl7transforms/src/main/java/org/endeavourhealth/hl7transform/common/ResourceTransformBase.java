package org.endeavourhealth.hl7transform.common;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

public abstract class ResourceTransformBase {

    protected Mapper mapper;
    protected ResourceContainer targetResources;

    public ResourceTransformBase(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();

    protected void saveToTargetResources(Resource resource) throws TransformException {
        saveToTargetResources(resource, null);
    }

    protected void saveToTargetResources(Resource resource, ResourceTag resourceTag) throws TransformException {
        if (resource != null)
            if (!targetResources.hasResource(resource.getId()))
                targetResources.addResource(resource, resourceTag);
    }
}
