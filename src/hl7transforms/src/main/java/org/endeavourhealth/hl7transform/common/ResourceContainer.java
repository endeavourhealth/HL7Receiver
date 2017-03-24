package org.endeavourhealth.hl7transform.common;


import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceContainer {

    protected List<Resource> resources = new ArrayList<>();
    private Organization managingOrganisation = null;
    private Location managingLocation = null;

    public Patient getPatient() {
        return this.resources
                .stream()
                .filter(t -> ResourceType.Patient.equals(t.getResourceType()))
                .map(t -> (Patient)t)
                .collect(StreamExtension.firstOrNullCollector());
    }

    public EpisodeOfCare getEpisodeOfCare() {
        return this.resources
                .stream()
                .filter(t -> ResourceType.EpisodeOfCare.equals(t.getResourceType()))
                .map(t -> (EpisodeOfCare)t)
                .collect(StreamExtension.firstOrNullCollector());
    }

    public void addManagingOrganisation(Organization organization) {
        this.managingOrganisation = organization;
        this.addResource(organization);
    }

    public Reference getManagingOrganisationReference() throws TransformException {
        if (this.managingOrganisation == null)
            throw new TransformException("Managing organisation not created");

        return ReferenceHelper.createReference(ResourceType.Organization, this.managingOrganisation.getId());
    }

    public void addManagingLocation(Location managingLocation) {
        this.managingLocation = managingLocation;
        this.addResource(managingLocation);
    }

    public Location getManagingLocation() throws TransformException {
        if (this.managingOrganisation == null)
            throw new TransformException("Managing location not created");

        return this.managingLocation;
    }

    public void addResource(Resource resource) {
        Validate.notNull(resource);

        if (!this.hasResource(resource.getClass(), resource.getId()))
            this.resources.add(resource);
    }

    private <T extends Resource> boolean hasResource(Class<T> resourceType, String id) {
        return (getResource(resourceType, id) != null);
    }

    public <T extends Resource> Reference getResourceReference(Class<T> resourceType, String id) throws TransformException {
        T resource = getResource(resourceType, id);

        if (resource == null)
            throw new TransformException("Could not find resource " + resourceType.getName() + " with id " + id);

        return ReferenceHelper.createReference(resource.getResourceType(), id);
    }

    private <T extends Resource> T getResource(Class<T> resourceType, String id) {
        Validate.notBlank(id);

        return (T)this.resources
                .stream()
                .filter(t -> id.equals(t.getId()))
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .collect(StreamExtension.singleOrNullCollector());
    }

    public ResourceContainer orderByResourceType() {
        this.resources = this.
                resources.
                stream().
                sorted(Comparator.comparing(t -> t.getResourceType()))
                .collect(Collectors.toList());

        return this;
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : this.resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
