package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ResourceUuidKey {
    private String resourceType;
    private String uniqueIdentifier;

    public ResourceUuidKey(String resourceType, String uniqueIdentifier) {
        this.resourceType = resourceType;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ResourceUuidKey that = (ResourceUuidKey) o;

        return new EqualsBuilder()
                .append(resourceType, that.resourceType)
                .append(uniqueIdentifier, that.uniqueIdentifier)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(resourceType)
                .append(uniqueIdentifier)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resourceType", resourceType)
                .append("uniqueIdentifier", uniqueIdentifier)
                .toString();
    }
}
