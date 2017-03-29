package org.endeavourhealth.hl7transform.mapper.resource;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceMapParameters {
    private static final String keyValuePairSeperator = "-";
    protected static final String repeatingValueSeperator = "~";

    private Map<String, String> parameters = new LinkedHashMap<>();

    public static ResourceMapParameters create() {
        return new ResourceMapParameters();
    }

    public ResourceMapParameters putExisting(ResourceMapParameters resourceMapParameters) {
        parameters.putAll(resourceMapParameters.parameters);
        return this;
    }

    public ResourceMapParameters put(String key, List<String> multiValues) {
        put(key, StringUtils.join(multiValues, repeatingValueSeperator));
        return this;
    }

    public ResourceMapParameters put(String key, String value) {
        parameters.put(key, value);
        return this;
    }

    public String createIdentifyingString() {
        return StringUtils.join(parameters
                .keySet()
                .stream()
                .map(t -> t + "=" + StringUtils.deleteWhitespace(StringUtils.defaultString(parameters.get(t))).toUpperCase())
                .collect(Collectors.toList()), keyValuePairSeperator);
    }

}
