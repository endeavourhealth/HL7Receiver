package org.endeavourhealth.hl7receiver.model.db;

import java.util.UUID;

public class DbResourceUuidMapping {
    private String scopeId;
    private String resourceType;
    private String uniqueIdentifier;
    private UUID resourceUuid;

    public String getScopeId() {
        return scopeId;
    }

    public DbResourceUuidMapping setScopeId(String scopeId) {
        this.scopeId = scopeId;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public DbResourceUuidMapping setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public DbResourceUuidMapping setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
        return this;
    }

    public UUID getResourceUuid() {
        return resourceUuid;
    }

    public DbResourceUuidMapping setResourceUuid(UUID resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }
}
