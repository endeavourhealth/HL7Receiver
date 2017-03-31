package org.endeavourhealth.hl7transform.mapper.code;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeMapperBase {

    public interface CodeSystemAccessor<T, R extends String> {
        R getCodeSystem(T enumValue);
    }

    public interface EnumFactory<T extends String, R> {
        R fromString(T value) throws Exception;
    }

    private Mapper mapper;

    public CodeMapperBase(Mapper mapper) {
        this.mapper = mapper;
    }

    protected CodeableConcept mapTerm(CodeContext codeContext, String term) throws MapperException {
        Validate.notNull(codeContext);

        if (StringUtils.isEmpty(StringUtils.defaultString(term).trim()))
            return null;

        MappedCode mappedCode = this.mapper.mapCode(codeContext.name(), null, null, term);

        if (mappedCode.getAction().equals(MappedCodeAction.NOT_MAPPED_FAIL_TRANSFORMATION))
            throw new MapperException("Term '" + term + "' in context " + codeContext.name() + " received action of " + mappedCode.getAction().name());

        if (mappedCode.getAction().equals(MappedCodeAction.NOT_MAPPED_EXCLUDE))
            return null;

        if (mappedCode.getAction().equals(MappedCodeAction.NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM))
            return new CodeableConcept().setText(term);

        if (mappedCode.getAction().equals(MappedCodeAction.MAPPED_INCLUDE)) {
            return new CodeableConcept()
                    .addCoding(new Coding()
                            .setCode(mappedCode.getCode())
                            .setDisplay(mappedCode.getTerm())
                            .setSystem(mappedCode.getSystem()))
                    .setText(term);
        }

        throw new MapperException(mappedCode.getAction().name() + " MappedCodeAction value not recognised");
    }

    protected <T extends String, R> R mapCodeToEnum(CodeContext codeContext, String code, EnumFactory<T, R> enumFromCode, CodeSystemAccessor<R, T> enumCodeSystemFromCode) throws MapperException {
        Validate.notNull(codeContext);

        ////////////////////////////////////////////////////////////////////////
        // ignore empty source codes
        //
        if (StringUtils.isEmpty(StringUtils.defaultString(code).trim()))
            return null;

        ////////////////////////////////////////////////////////////////////////
        // get mapping
        //
        MappedCode mappedCode = this.mapper.mapCode(codeContext.name(), code, null, null);

        ////////////////////////////////////////////////////////////////////////
        // process NOT_MAPPED actions
        //
        if (mappedCode.getAction() != MappedCodeAction.MAPPED_INCLUDE) {

            switch (mappedCode.getAction()) {
                case NOT_MAPPED_FAIL_TRANSFORMATION: throw new MapperException("Code '" + code + "' in context " + codeContext.name() + " received action of " + mappedCode.getAction().name());
                case NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM: throw new MapperException("Code '" + code + "' in context " + codeContext + " received unsupported action of " + mappedCode.getAction().name());
                case NOT_MAPPED_EXCLUDE: return null;
                default: throw new MapperException(mappedCode.getAction().name() + " MappedCodeAction value not recognised");
            }
        }

        ////////////////////////////////////////////////////////////////////////
        // convert target code to enum
        //
        R enumValue;

        try {
            if (StringUtils.isEmpty(mappedCode.getCode()))
                throw new MapperException("Target code in context " + codeContext.name() + " is empty (source code = '" + code + "')");

            enumValue = enumFromCode.fromString((T)mappedCode.getCode());
        } catch (Exception e) {
            throw new MapperException(e.getMessage(), e);
        }

        ////////////////////////////////////////////////////////////////////////
        // check target system matches enum value system
        //
        String enumCodeSystem = enumCodeSystemFromCode.getCodeSystem(enumValue);

        if (!mappedCode.getSystem().equals(enumCodeSystem))
            throw new MapperException("Conversion to enum failed.  Mapped code system '" + mappedCode.getSystem() + "' does not match enum code system '" + enumCodeSystem + "'");

        ////////////////////////////////////////////////////////////////////////
        // return enum value
        //
        return enumValue;
    }
}
