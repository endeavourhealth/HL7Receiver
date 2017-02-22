package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;


public class CodeableConceptHelper {

    public static CodeableConcept getCodeableConceptFromString(String code) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding();
        codeableConcept.setText(code);

        return codeableConcept;
    }
}
