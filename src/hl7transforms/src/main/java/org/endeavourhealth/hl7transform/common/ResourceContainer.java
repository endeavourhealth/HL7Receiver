package org.endeavourhealth.hl7transform.common;


import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class ResourceContainer {
    protected List<Resource> resources = new ArrayList<>();

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


    public void addResource(Resource resource) {
        Validate.notNull(resource);

        if (!this.hasResource(resource.getClass(), resource.getId()))
            this.resources.add(resource);
    }

    public <T extends Resource> boolean hasResource(Class<T> resourceType, String id) {
        return (getResource(resourceType, id) != null);
    }

    public <T extends Resource> T getResource(Class<T> resourceType, String id) {
        Validate.notBlank(id);

        return (T)this.resources
                .stream()
                .filter(t -> id.equals(t.getId()))
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .collect(StreamExtension.singleOrNullCollector());
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : this.resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
