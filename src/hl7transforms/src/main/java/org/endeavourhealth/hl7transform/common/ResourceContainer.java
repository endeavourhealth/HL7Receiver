package org.endeavourhealth.hl7transform.common;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceContainer {

    protected List<Resource> resources = new ArrayList<>();

    public void addResource(Resource resource) throws TransformException {
        Validate.notNull(resource);

        if (!this.hasResource(resource.getClass(), resource.getId()))
            this.resources.add(resource);
    }

    protected <T extends Resource> boolean hasResource(Class<T> resourceType, String id) throws TransformException {
        Validate.notNull(resourceType);

        return (getResourceSingleOrNull(resourceType, id) != null);
    }

    protected <T extends Resource> List<Resource> getResources(Class<T> resourceType) {
        Validate.notNull(resourceType);

        return this
                .resources
                .stream()
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .collect(Collectors.toList());
    }

    protected <T extends Resource> T getResourceSingleOrNull(Class<T> resourceType) throws TransformException {
        Validate.notNull(resourceType);

        List<T> resources = this.resources
                .stream()
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .map(t -> (T)t)
                .collect(Collectors.toList());

        if (resources.size() > 1)
            throw new TransformException("Found multiple resources of type " + resourceType.getName());

        if (resources.size() == 0)
            return null;

        return resources.get(0);
    }

    protected <T extends Resource> T getResourceSingle(Class<T> resourceType) throws TransformException {
        T resource = getResourceSingleOrNull(resourceType);

        if (resource == null)
            throw new TransformException("Could not find resource of type " + resourceType.getName());

        return resource;
    }

    protected <T extends Resource> T getResourceSingleOrNull(Class<T> resourceType, String id) throws TransformException {
        Validate.notNull(resourceType);
        Validate.notBlank(id);

        List<T> resources = this.resources
                .stream()
                .filter(t -> id.equals(t.getId()))
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .map(t -> (T)t)
                .collect(Collectors.toList());

        if (resources.size() > 1)
            throw new TransformException("Found multiple resources of type " + resourceType.getName());

        if (resources.size() == 0)
            return null;

        return resources.get(0);
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : this.resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
