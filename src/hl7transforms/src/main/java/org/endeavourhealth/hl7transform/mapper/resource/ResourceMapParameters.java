package org.endeavourhealth.hl7transform.mapper.resource;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceMapParameters {
    private static final String equalsSeperator = "=";
    private static final String keyValuePairSeperator = "-";
    private static final String repeatingValueSeperator = "~";

    private static final String equalsSeperatorEscape = "/$E/";
    private static final String keyValuePairSeperatorEscape = "/$H/";
    private static final String repeatingValueSeperatorEscape = "/$T/";

    private Map<String, String> parameters = new LinkedHashMap<>();

    public static ResourceMapParameters create() {
        return new ResourceMapParameters();
    }

    public static ResourceMapParameters parse(String identifierString) throws MapperException {
        ResourceMapParameters resourceMapParameters = new ResourceMapParameters();

        if (identifierString == null)
            return resourceMapParameters;

        String[] keyValuePairs = StringUtils.split(identifierString, keyValuePairSeperator);

        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = StringUtils.split(keyValuePair, equalsSeperator);

            if (keyValue.length != 2)
                throw new MapperException("Could not parse key value pair in resource map identifier string " + identifierString);

            String key = keyValue[0];
            String value = keyValue[1];

            resourceMapParameters.putUnescaped(key, value);
        }

        return resourceMapParameters;
    }

    public String get(String key) {
        return unescapeString(parameters.getOrDefault(escapeString(key), null));
    }

    public ResourceMapParameters putExisting(ResourceMapParameters resourceMapParameters) {
        parameters.putAll(resourceMapParameters.parameters);
        return this;
    }

    public ResourceMapParameters put(String key, List<String> multiValues) {
        putUnescaped(escapeString(key), StringUtils.join(multiValues
                .stream()
                .map(t -> escapeString(t))
                .collect(Collectors.toList()), repeatingValueSeperator));
        return this;
    }

    protected void putUnescaped(String key, String value) {
        parameters.put(key, value);
    }

    public ResourceMapParameters put(String key, String value) {
        putUnescaped(escapeString(key), escapeString(value));
        return this;
    }

    public String createIdentifyingString() {
        return StringUtils.join(parameters
                .keySet()
                .stream()
                .map(t -> t + equalsSeperator + StringUtils.deleteWhitespace(StringUtils.defaultString(parameters.get(t))).toUpperCase())
                .collect(Collectors.toList()), keyValuePairSeperator);
    }

    private String escapeString(String string) {
        string = StringUtils.replace(string, equalsSeperator, equalsSeperatorEscape);
        string = StringUtils.replace(string, keyValuePairSeperator, keyValuePairSeperatorEscape);
        string = StringUtils.replace(string, repeatingValueSeperator, repeatingValueSeperatorEscape);
        return string;
    }

    private String unescapeString(String string) {
        string = StringUtils.replace(string, equalsSeperatorEscape, equalsSeperator);
        string = StringUtils.replace(string, keyValuePairSeperatorEscape, keyValuePairSeperator);
        string = StringUtils.replace(string, repeatingValueSeperatorEscape, repeatingValueSeperator);
        return string;
    }
}
