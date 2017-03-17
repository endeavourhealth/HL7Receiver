package org.endeavourhealth.hl7transform.homerton.transforms.todo;


import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.segments.Dg1Segment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.CodeableConceptHelper;
import org.endeavourhealth.hl7transform.common.converters.DateConverter;
import org.hl7.fhir.instance.model.*;

public class DiagnosisTransform {

    public static Observation fromHl7v2(Dg1Segment source) throws ParseException, TransformException {
        Observation observation = new Observation();

        //observation.setIdElement(new IdType().setValue(IdentifierHelper.generateId(source.getDiagnosisCode().getAsString(), source.getDiagnosisDateTime().toString())));

        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem(convertCodeSystem(source.getDiagnosisCodingMethod()));
        coding.setCode(source.getDiagnosisCode().getAsString());
        coding.setDisplay(source.getDiagnosisDescription());

        cc.addCoding(coding);
        cc.setText(source.getDiagnosisDescription());

        observation.setCode(cc);

        observation.setEffective(DateConverter.getDateType(source.getDiagnosisDateTime()));

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
