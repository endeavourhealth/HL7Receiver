package org.endeavourhealth.hl7transform.mapper.code;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.CheckedFunction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.exceptions.UncheckedMapperException;
import org.hl7.fhir.instance.model.ContactPoint;

public class CodeMapper {

    private Mapper mapper;

    public CodeMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public ContactPoint.ContactPointSystem mapTelecomEquipmentType(String telecomEquipmentType) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeMapContext.HL7_TELECOM_EQUIPMENT_TYPE,
                        telecomEquipmentType,
                        (t) -> ContactPoint.ContactPointSystem.fromCode(t),
                        (r) -> r.getSystem());
    }

    protected CodeMapping mapCode(String codeContext, String code) throws MapperException {
        Validate.notEmpty(codeContext);

        if (StringUtils.isEmpty(StringUtils.defaultString(code).trim()))
            return null;

        CodeMapping codeMapping = this.mapper.mapCode(codeContext, code, null, null);

        switch (codeMapping.getAction()) {
            case NOT_MAPPED_FAIL_TRANSFORMATION:
                throw new MapperException("Code '" + code + "' in context " + codeContext + " received action of " + CodeMappingAction.NOT_MAPPED_FAIL_TRANSFORMATION.name());
            case NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM:
                throw new MapperException("Code '" + code + "' in context " + codeContext + " received unsupported action of " + CodeMappingAction.NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM.name());
            case NOT_MAPPED_EXCLUDE:
                return null;
            case MAPPED_INCLUDE:
                return codeMapping;
            default:
                throw new MapperException("CodeMappingAction not recognised");
        }
    }

    protected <T extends String, R> R mapCodeToEnum(String codeContext, String code, CheckedFunction<T, R> enumFromCode, GetCodeSystemFunction<R, T> enumCodeSystemFromCode) throws MapperException {
        CodeMapping codeMapping = mapper.getCodeMapper().mapCode(codeContext, code);

        if (codeMapping == null)
            return null;

        R enumValue = convertMappedExceptionToUnmapped(t -> (R)enumFromCode, codeMapping.getCode());

        String enumCodeSystem = enumCodeSystemFromCode.getCodeSystem(enumValue);

        if (!codeMapping.getSystem().equals(enumCodeSystem))
            throw new MapperException("Conversion to enum failed.  Mapped code system '" + codeMapping.getSystem() + "' does not match enum code system '" + enumCodeSystem + "'");

        return enumValue;
    }

    protected static <T extends String, R> R convertMappedExceptionToUnmapped(CheckedFunction<T, R> function, T value) {
        try {
            return function.execute(value);
        } catch (Exception e) {
            throw new UncheckedMapperException(e.getMessage(), e);
        }
    }
}
