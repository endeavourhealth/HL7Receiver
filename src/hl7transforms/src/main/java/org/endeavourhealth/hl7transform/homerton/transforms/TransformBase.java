package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class TransformBase {

    protected static final String keyValuePairSeperator = "-";
    protected static final String repeatingValueSeperator = "~";

    protected Mapper mapper;
    protected ResourceContainer targetResources;

    public TransformBase(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public abstract ResourceType getResourceType();
    
    protected static String createIdentifyingString(String prefix, Map<String, String> keyValuePairs) {
        return StringUtils.join(Lists.newArrayList(prefix, createIdentifyingString(keyValuePairs)), keyValuePairSeperator);
    }

    protected static String createIdentifyingString(Map<String, String> keyValuePairs) {
        return StringUtils.join(keyValuePairs
                .keySet()
                .stream()
                .map(t -> t + "=" + StringUtils.deleteWhitespace(StringUtils.defaultString(keyValuePairs.get(t))).toUpperCase())
                .collect(Collectors.toList()), keyValuePairSeperator);
    }

    protected void mapAndSetId(String uniqueIdentifyingString, Resource target) throws MapperException, TransformException, ParseException {

        UUID resourceUuid = mapper.mapResourceUuid(getResourceType(), uniqueIdentifyingString);
        target.setId(resourceUuid.toString());
    }
}
