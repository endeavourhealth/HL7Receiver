package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Extension;

public class EncounterCommon {

    public static Extension createMessageTypeExtension(String messageType, String versionId, Mapper mapper) throws MapperException, TransformException {
        CodeableConcept messageTypeCode = mapper.getCodeMapper().mapMessageType(messageType);

        Coding messageTypeCoding = CodeableConceptHelper.getFirstCoding(messageTypeCode);

        if (messageTypeCoding == null)
            throw new TransformException("Could not map message type '" + messageType + "'");

        CodeableConcept codeableConcept = new CodeableConcept().addCoding(
                new Coding()
                        .setCode(messageTypeCoding.getCode())
                        .setDisplay(messageTypeCoding.getDisplay())
                        .setVersion(versionId)
                        .setSystem(messageTypeCoding.getSystem()))
                .setText(messageTypeCoding.getDisplay());

        return ExtensionConverter.createExtension(FhirExtensionUri.HL7_MESSAGE_TYPE, codeableConcept);
    }
}
