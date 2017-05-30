package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceUuidCache {

    private List<ResourceType> resourceTypesToCache;

    private ConcurrentHashMap<ResourceUuidKey, UUID> hashMap = new ConcurrentHashMap<>();

    public ResourceUuidCache(ResourceType... resourceTypesToCache) {
        Validate.notNull(resourceTypesToCache);

        this.resourceTypesToCache = new ArrayList<>(Arrays.asList(resourceTypesToCache));
    }

    public UUID getResourceUuid(ResourceType resourceType, String identifier) {
        Validate.notNull(resourceType);
        Validate.notEmpty(identifier);

        if (!resourceTypesToCache.contains(resourceType))
            return null;

        ResourceUuidKey resourceUuidKey = new ResourceUuidKey(resourceType.toString(), identifier);

        return hashMap.getOrDefault(resourceUuidKey, null);
    }

    public void putResourceUuid(ResourceType resourceType, String identifier, UUID resourceUuid) throws MapperException {
        Validate.notNull(resourceType);
        Validate.notEmpty(identifier);
        Validate.notNull(resourceUuid);

        if (!resourceTypesToCache.contains(resourceType))
            return;

        ResourceUuidKey combinedKey = new ResourceUuidKey(resourceType.toString(), identifier);

        UUID previousValue = hashMap.putIfAbsent(combinedKey, resourceUuid);

        if (previousValue != null)
            if (!previousValue.equals(resourceUuid))
                throw new MapperException("Tried to put key " + combinedKey.toString() + " with resource UUID " + resourceUuid.toString() + " but different resource UUID already exists " + previousValue.toString());
    }

    private static String getCombinedKey(ResourceType resourceType, String identifier) {
        return resourceType.toString() + "~" + identifier;
    }
}
