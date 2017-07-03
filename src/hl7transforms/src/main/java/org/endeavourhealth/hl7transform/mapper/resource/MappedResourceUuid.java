package org.endeavourhealth.hl7transform.mapper.resource;

import java.util.UUID;

public class MappedResourceUuid {

    private String resourceType;
    private String uniqueIdentifier;
    private UUID resourceUuid;

    public String getResourceType() {
        return resourceType;
    }

    public MappedResourceUuid setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public MappedResourceUuid setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
        return this;
    }

    public UUID getResourceUuid() {
        return resourceUuid;
    }

    public MappedResourceUuid setResourceUuid(UUID resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }
}

