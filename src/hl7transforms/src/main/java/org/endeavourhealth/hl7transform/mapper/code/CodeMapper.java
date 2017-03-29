package org.endeavourhealth.hl7transform.mapper.code;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.ContactPoint;
import org.hl7.fhir.instance.model.HumanName;

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

    public ContactPoint.ContactPointUse mapTelecomUseCode(String telecomUseCode) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_TELECOM_USE_CODE,
                        telecomUseCode,
                        (t) -> ContactPoint.ContactPointUse.fromCode(t),
                        (r) -> r.getSystem());
    }

    public HumanName.NameUse mapNameTypeCode(String nameTypeCode) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_PERSON_NAME_TYPE_CODE,
                        nameTypeCode,
                        (t) -> HumanName.NameUse.fromCode(t),
                        (r) -> r.getSystem());
    }
}
