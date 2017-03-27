package org.endeavourhealth.hl7transform.homerton.transforms;

import org.endeavourhealth.hl7transform.homerton.HomertonResourceContainer;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.hl7.fhir.instance.model.ResourceType;

public abstract class HomertonTransformBase {

    protected Mapper mapper;
    protected HomertonResourceContainer targetResources;

    public HomertonTransformBase(Mapper mapper, HomertonResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();
}
