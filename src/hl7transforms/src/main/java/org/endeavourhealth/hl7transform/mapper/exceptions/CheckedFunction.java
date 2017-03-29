package org.endeavourhealth.hl7transform.mapper.exceptions;

public interface CheckedFunction<T, R> {
    R execute(T value) throws Exception;
}
