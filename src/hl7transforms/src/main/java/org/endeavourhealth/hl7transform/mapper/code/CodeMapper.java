package org.endeavourhealth.hl7transform.mapper.code;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ContactPoint;

public class CodeMapper extends CodeMapperBase {

    public CodeMapper(Mapper mapper) {
        super(mapper);
    }

    public ContactPoint.ContactPointSystem mapTelecomEquipmentType(String telecomEquipmentType) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_TELECOM_EQUIPMENT_TYPE,
                        telecomEquipmentType,
                        (t) -> ContactPoint.ContactPointSystem.fromCode(t),
                        (r) -> r.getSystem());
    }
}
