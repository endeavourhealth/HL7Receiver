package org.endeavourhealth.hl7transform.transforms.homerton.transforms.notused;


import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.segments.ObxSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

public class ObservationTransform {

    public static Observation fromHl7v2(ObxSegment source) throws ParseException, TransformException {
        Observation observation = new Observation();

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.setCode(new CodeableConcept().setText(source.getObservationIdentifier().getAsString()));

        switch (source.getValueType()) {
            case "CE":
                observation.setValue(new CodeableConcept().setText(source.getObservationValue()));
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
                observation.setValue(new CodeableConcept().setText(source.getObservationValue()));
                break;
            //Default to string type
            default:
                observation.setValue(new StringType().setValue(source.getObservationValue()));
                break;
        }

        return observation;
    }

}
