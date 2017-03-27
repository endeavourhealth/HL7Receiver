package org.endeavourhealth.hl7transform.common;

import org.hl7.fhir.instance.model.Resource;

public class ResourceContainerItem {

    private Resource resource;
    private ResourceTag resourceTag;

    public Resource getResource() {
        return resource;
    }

    public ResourceContainerItem setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public ResourceTag getResourceTag() {
        return resourceTag;
    }

    public ResourceContainerItem setResourceTag(ResourceTag resourceTag) {
        this.resourceTag = resourceTag;
        return this;
    }
}
