package org.endeavourhealth.hl7transform.homerton;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class HomertonResourceContainer extends ResourceContainer {
    private Organization homertonOrganisation = null;
    private Location homertonLocation = null;
    private Organization generalPracticeOrganisation = null;

    public void setHomertonOrganisation(Organization organisation) throws TransformException {
        if (homertonOrganisation != null)
            throw new TransformException("Homerton organisation is already set");

        this.homertonOrganisation = organisation;
        this.addResource(organisation);
    }

    public Reference getHomertonOrganisationReference() throws TransformException {
        if (this.homertonOrganisation == null)
            throw new TransformException("Homerton organisation not set");

        return ReferenceHelper.createReference(ResourceType.Organization, this.homertonOrganisation.getId());
    }

    public void setHomertonLocation(Location homertonLocation) throws TransformException {
        if (homertonLocation != null)
            throw new TransformException("Homerton location is already set");

        this.homertonLocation = homertonLocation;
        this.addResource(homertonLocation);
    }

    public Location getHomertonLocation() throws TransformException {
        if (this.homertonLocation == null)
            throw new TransformException("Homerton location not set");

        return this.homertonLocation;
    }

    public void setGeneralPracticeOrganisation(Organization organisation) throws TransformException {
        if (organisation != null)
            throw new TransformException("General practice organisation is already set");

        this.generalPracticeOrganisation = organisation;
        this.addResource(organisation);
    }

    public Organization getGeneralPracticeOrganisation() throws TransformException {
        if (this.generalPracticeOrganisation == null)
            throw new TransformException("General practice organisation not set");

        return this.generalPracticeOrganisation;
    }

    public Patient getPatient() throws TransformException {
        return this.getResourceSingle(Patient.class);
    }

    public EpisodeOfCare getEpisodeOfCare() throws TransformException {
        return this.getResourceSingle(EpisodeOfCare.class);
    }

    public ResourceContainer orderByResourceType() {
        List<Resource> orderedResources = Lists.newArrayList(Iterables.concat(
                getResources(MessageHeader.class),
                getResources(Patient.class),
                getResources(EpisodeOfCare.class),
                getResources(Encounter.class),
                getResources(Organization.class),
                getResources(Location.class),
                getResources(Practitioner.class)));

        for (Resource resource : this.resources)
            if (!orderedResources.stream().anyMatch(t -> t.getId().equals(resource.getId())))
                orderedResources.add(resource);

        this.resources = orderedResources;

        return this;
    }
}
