package org.endeavourhealth.hl7transform;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.JsonHelper;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.transforms.homerton.HomertonAdtTransform;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7parser.Message;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.hl7.fhir.instance.model.Bundle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Hl7v2Transform {

    private static List<Transform> transformTypes = Arrays.asList(new Transform[] {
        new HomertonAdtTransform()
    });

    public static String transform(String message, Mapper mapper) throws Exception {

        /////
        ///// get the sending facility and get the transform profile
        /////
        Transform transform = getTransform(message);

        /////
        ///// construct our message with including the transforms Z segments
        /////
        AdtMessage adtMessage = new AdtMessage(message, transform.getZSegments());

        /////
        ///// perform any pre transform activities
        /////
        adtMessage = transform.preTransform(adtMessage);

        /////
        ///// perform the actual transform and output as JSON
        /////
        Bundle bundle = transform.transform(adtMessage, mapper);
        return JsonHelper.getPrettyJson(bundle);
    }

    public static String preTransformOnly(String message) throws Exception {

        Transform transform = getTransform(message);

        AdtMessage adtMessage = new AdtMessage(message, transform.getZSegments());

        adtMessage = transform.preTransform(adtMessage);

        return adtMessage.compose();
    }

    public static String parseAndRecompose(String message) throws Exception {
        AdtMessage sourceMessage = new AdtMessage(message);

        return sourceMessage.compose();
    }

    private static Transform getTransform(String message) throws TransformException, ParseException {
        String sendingFacility = getSendingFacility(message);

        List<Transform> transforms = transformTypes
                .stream()
                .filter(t -> t.getSupportedSendingFacilities().contains(sendingFacility))
                .collect(Collectors.toList());

        if (transforms.size() == 0)
            throw new NotImplementedException("Transform for sending facility " + sendingFacility + " not found");

        if (transforms.size() > 1)
            throw new NotImplementedException("Multiple transforms for sending facility " + sendingFacility + " found");

        return transforms.get(0);
    }

    private static String getSendingFacility(String message) throws ParseException, TransformException {
        Message parsedMessage = new Message(message);
        MshSegment mshSegment = parsedMessage.getSegment(SegmentName.MSH, MshSegment.class);

        if (mshSegment == null)
            throw new TransformException("MSH segment not found");

        if (StringUtils.isBlank(mshSegment.getSendingFacility()))
            throw new TransformException("Sending facility is blank");

        return mshSegment.getSendingFacility();
    }
}
