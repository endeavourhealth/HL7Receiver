package org.endeavourhealth.hl7transform.mapper.code;

public interface GetCodeSystemFunction<T, R extends String> {
    R getCodeSystem(T enumValue);
}
