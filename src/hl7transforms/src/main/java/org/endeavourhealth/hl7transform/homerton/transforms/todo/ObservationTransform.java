package org.endeavourhealth.hl7transform.homerton.transforms.todo;


import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.segments.ObxSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.CodeableConceptHelper;
import org.hl7.fhir.instance.model.*;

public class ObservationTransform {

    public static Observation fromHl7v2(ObxSegment source) throws ParseException, TransformException {
        Observation observation = new Observation();

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.setCode(CodeableConceptHelper.getCodeableConceptFromString(source.getObservationIdentifier().getAsString()));

        switch (source.getValueType()) {
            case "CE":
                observation.setValue(CodeableConceptHelper.getCodeableConceptFromString(source.getObservationValue()));
                break;
            case "DT":
                observation.setValue(DateTimeType.parseV3(source.getObservationValue()));
                break;
            case "ST":
                observation.setValue(new StringType().setValue(source.getObservationValue()));
                break;
            case "NM":
                observation.setValue(new IntegerType().setValue(Integer.parseInt(source.getObservationValue())));
                break;

            //Homerton specific
            case "CD":
                observation.setValue(CodeableConceptHelper.getCodeableConceptFromString(source.getObservationValue()));
                break;
            //Default to string type
            default:
                observation.setValue(new StringType().setValue(source.getObservationValue()));
                break;
        }

        return observation;
    }

}
