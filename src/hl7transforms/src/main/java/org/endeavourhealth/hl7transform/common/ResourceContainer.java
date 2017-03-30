package org.endeavourhealth.hl7transform.common;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceContainer {

    protected List<ResourceContainerItem> resources = new ArrayList<>();

    public void addResource(Resource resource) throws TransformException {
        addResource(resource, null);
    }

    public void addResource(Resource resource, ResourceTag resourceTag) throws TransformException {
        Validate.notNull(resource);

        if (resourceTag != null) {

            if (!resource.getClass().equals(resourceTag.getResourceClass()))
                throw new TransformException("Cannot add a resource of class " + resource.getClass().getName() + " with tag " + resourceTag.name() + " to the ResourceContainer");

            if (containsTag(resourceTag))
                throw new TransformException("ResourceContainer already contains resource with tag " + resourceTag.name());
        }

        if (this.hasResource(resource.getId()))
            throw new TransformException("ResourceContainer already contains resource with id " + resource.getId());

        this.resources.add(new ResourceContainerItem()
                .setResource(resource)
                .setResourceTag(resourceTag));
    }

    public <T extends Resource> Reference getResourceReference(ResourceTag resourceTag, Class<T> resourceClass) throws TransformException {
        Resource resource = getResourceSingle(resourceTag, resourceClass);

        return ReferenceHelper.createReference(resource.getResourceType(), resource.getId());
    }

    public boolean hasResource(ResourceTag resourceTag) {
        Validate.notNull(resourceTag);

        return (containsTag(resourceTag));
    }

    public <T extends Resource> T getResourceSingleOrNull(ResourceTag resourceTag, Class<T> resourceClass) throws TransformException {
        Validate.notNull(resourceTag);
        Validate.notNull(resourceClass);

        if (!containsTag(resourceTag))
            return null;

        if (!resourceTag.getResourceClass().equals(resourceClass))
            throw new TransformException("ResourceTag class does not match resource class");

        return this.resources
                .stream()
                .filter(t -> resourceTag.equals(t.getResourceTag()))
                .map(t -> (T)t.getResource())
                .collect(StreamExtension.singleOrNullCollector());
    }

    public <T extends Resource> T getResourceSingle(ResourceTag resourceTag, Class<T> resourceClass) throws TransformException {
        T resource = getResourceSingleOrNull(resourceTag, resourceClass);

        if (resource == null)
            throw new TransformException("ResourceContain does not contain resource with tag " + resourceTag.name());

        return resource;
    }

    public boolean hasResource(String id) {
        return this.resources
                .stream()
                .anyMatch(t -> t.getResource().getId().equalsIgnoreCase(id));
    }

    public <T extends Resource> T getResourceSingle(Class<T> resourceType) throws TransformException {
        List<T> resources = getResourceContainerItems(resourceType)
                .stream()
                .map(t -> (T)t.getResource())
                .collect(Collectors.toList());

        if (resources.size() == 0)
            throw new TransformException("No resources found of type " + resourceType.getName());

        if (resources.size() > 1)
            throw new TransformException("Multiple resources found of type " + resourceType.getName());

        return resources.get(0);
    }

    protected <T extends Resource> List<ResourceContainerItem> getResourceContainerItems(Class<T> resourceType) {
        Validate.notNull(resourceType);

        return this.resources
                .stream()
                .filter(t -> resourceType.isAssignableFrom(t.getResource().getClass()))
                .collect(Collectors.toList());
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (ResourceContainerItem resource : this.resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource.getResource()));

        return bundle;
    }

    private boolean containsTag(ResourceTag resourceTag) {
        if (resourceTag == null)
            return false;

        return this.resources
                .stream()
                .anyMatch(t -> resourceTag.equals(t.getResourceTag()));
    }

    public ResourceContainer orderByResourceType() {
        List<ResourceContainerItem> orderedResources = Lists.newArrayList(Iterables.concat(
                getResourceContainerItems(MessageHeader.class),
                getResourceContainerItems(Patient.class),
                getResourceContainerItems(EpisodeOfCare.class),
                getResourceContainerItems(Encounter.class),
                getResourceContainerItems(Organization.class),
                getResourceContainerItems(Location.class),
                getResourceContainerItems(Practitioner.class)));

        for (ResourceContainerItem resource : this.resources)
            if (orderedResources.stream().allMatch(t -> t != resource))
                orderedResources.add(resource);

        this.resources = orderedResources;

        return this;
    }
}
