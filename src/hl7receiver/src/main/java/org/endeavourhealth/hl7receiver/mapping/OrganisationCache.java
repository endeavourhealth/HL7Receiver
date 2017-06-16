package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;

import java.util.concurrent.ConcurrentHashMap;

public class OrganisationCache {
    private ConcurrentHashMap<String, MappedOrganisation> hashMap = new ConcurrentHashMap<>();

    public OrganisationCache() {
    }

    public MappedOrganisation getMappedOrganisation(String odsCode) {
        Validate.notEmpty(odsCode);

        odsCode = normaliseOdsCode(odsCode);

        if (odsCode.length() == 0)
            return null;

        return hashMap.getOrDefault(odsCode, null);
    }

    public void putMappedOrganisation(String odsCode, MappedOrganisation mappedOrganisation) {
        Validate.notEmpty(odsCode);
        Validate.notNull(mappedOrganisation);

        odsCode = normaliseOdsCode(odsCode);

        if (odsCode.length() == 0)
            return;

        hashMap.put(odsCode, mappedOrganisation);
    }

    private static String normaliseOdsCode(String odsCode) {
        return StringUtils.trimToEmpty(odsCode).toUpperCase();
    }
}
