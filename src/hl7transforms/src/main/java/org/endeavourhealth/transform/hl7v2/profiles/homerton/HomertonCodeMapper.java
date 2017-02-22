package org.endeavourhealth.transform.hl7v2.profiles.homerton;

import org.endeavourhealth.transform.hl7v2.mapper.Code;
import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;

public class HomertonCodeMapper extends CodeMapper {
    public HomertonCodeMapper(String originatingSystemId) {
        super(originatingSystemId);
    }

    @Override
    public Code map(Code code, String contextId) {
        return null;
    }
}
