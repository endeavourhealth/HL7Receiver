package org.endeavourhealth.transform.hl7v2.profiles;

import org.endeavourhealth.transform.hl7v2.mapper.Code;
import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;

public class DefaultCodeMapper extends CodeMapper {

    private String originatingSystemId;

    public DefaultCodeMapper(String originatingSystemId) {
        super(originatingSystemId);

        this.originatingSystemId = originatingSystemId;
    }

    @Override
    public Code map(Code code, String contextId) {
        return code;
    }
}
