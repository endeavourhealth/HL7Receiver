package org.endeavourhealth.hl7transform.mapper;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapParameters {
    private static final String keyValuePairSeperator = "-";
    protected static final String repeatingValueSeperator = "~";

    private Map<String, String> parameters = new HashMap<>();

    public static MapParameters create() {
        return new MapParameters();
    }

    public MapParameters putExisting(MapParameters mapParameters) {
        parameters.putAll(mapParameters.parameters);
        return this;
    }

    public MapParameters put(String key, List<String> multiValues) {
        put(key, StringUtils.join(multiValues, repeatingValueSeperator));
        return this;
    }

    public MapParameters put(String key, String value) {
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
