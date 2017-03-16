package org.endeavourhealth.hl7transform.transform;


import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;

public class ResourceContainer extends ArrayList<Resource> {
    final static long serialVersionUID = 1L;

    public Patient getPatient() {
        return this.stream()
                .filter(t -> ResourceType.Patient.equals(t.getResourceType()))
                .map(t -> (Patient)t)
                .collect(StreamExtension.firstOrNullCollector());
    }

    public EpisodeOfCare getEpisodeOfCare() {
        return this.stream()
                .filter(t -> ResourceType.EpisodeOfCare.equals(t.getResourceType()))
                .map(t -> (EpisodeOfCare)t)
                .collect(StreamExtension.firstOrNullCollector());
    }

    public <T extends Resource> boolean hasResource(Class<T> resourceType, String id) {
        return (getResource(resourceType, id) != null);
    }

    public <T extends Resource> T getResource(Class<T> resourceType, String id) {
        Validate.notBlank(id);

        return (T)this.stream()
                .filter(t -> id.equals(t.getId()))
                .filter(t -> resourceType.isAssignableFrom(t.getClass()))
                .collect(StreamExtension.singleOrNullCollector());
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : this)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
