package org.endeavourhealth.transform.hl7v2.transform;


import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.segments.Dg1Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.CodeableConceptHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.DateHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

public class DiagnosisTransform {

    public static Observation fromHl7v2(Dg1Segment source) throws ParseException, TransformException {
        Observation observation = new Observation();

        observation.addIdentifier().setValue(IdentifierHelper.generateId(source.getDiagnosisCode().getAsString(), source.getDiagnosisDateTime().toString()));

        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem(convertCodeSystem(source.getDiagnosisCodingMethod()));
        coding.setCode(source.getDiagnosisCode().getAsString());
        coding.setDisplay(source.getDiagnosisDescription());

        cc.addCoding(coding);
        cc.setText(source.getDiagnosisDescription());

        observation.setCode(cc);

        observation.setEffective(new DateTimeType().setValue(DateHelper.fromLocalDateTime(source.getDiagnosisDateTime())));

        observation.setCategory(CodeableConceptHelper.getCodeableConceptFromString(source.getDiagnosisType()));

        return observation;
    }

    private static String convertCodeSystem(String codeSystem) throws TransformException {
        codeSystem = codeSystem.trim().toUpperCase();

        switch (codeSystem) {
            case "SNMCT": return "http://snomed.info/id";

            default: throw new TransformException(codeSystem + " code system not recognised");
        }
    }

}
