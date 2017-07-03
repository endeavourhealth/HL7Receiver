package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.hl7transform.mapper.resource.MappedResourceUuid;
import org.hl7.fhir.instance.model.Parameters;
import org.hl7.fhir.instance.model.StringType;

import java.util.HashMap;
import java.util.UUID;

public class MergeCommon {
    public static Parameters.ParametersParameterComponent createStringParameter(String name, String value) {
        return new Parameters.ParametersParameterComponent()
                .setName(name)
                .setValue(new StringType(value));
    }

    public static Parameters.ParametersParameterComponent createOldToNewResourceMap(HashMap<MappedResourceUuid, UUID> resourceUuidMap) {
        Parameters.ParametersParameterComponent resourceMap = new Parameters.ParametersParameterComponent()
                .setName("OldToNewResourceMap");

        for (MappedResourceUuid from : resourceUuidMap.keySet()) {
            UUID to = resourceUuidMap.get(from);
            String resourceType = from.getResourceType();

            resourceMap.addPart(createStringParameter(resourceType + "/" + from.getResourceUuid().toString(), resourceType + "/" + to.toString()));
        }

        return resourceMap;
    }
}
