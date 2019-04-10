package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CodeCache {

    static int CACHE_TIMEOUT_SECONDS = 60 * 5;

    private ConcurrentHashMap<CodeCacheKey, MappedCode> hashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<CodeCacheKey, Date> hashMapExpiryDates = new ConcurrentHashMap<>();
    private List<MappedCodeAction> codeActionsToCache = new ArrayList<>();

    public CodeCache(MappedCodeAction... codeActionsToCache) {
        Validate.notNull(codeActionsToCache);

        this.codeActionsToCache = new ArrayList<>(Arrays.asList(codeActionsToCache));
    }

    public MappedCode getMappedCode(String context, String code, String codeSystem, String term) {
        Validate.notEmpty(context);
        Validate.notEmpty(code + term);

        CodeCacheKey codeCacheKey = new CodeCacheKey(context, code, codeSystem, term);

        MappedCode ret = hashMap.get(codeCacheKey);
        Date cacheTime = hashMapExpiryDates.get(codeCacheKey);
        if (isExpired(cacheTime)) {
            return null;
        } else {
            return ret;
        }
    }

    private static boolean isExpired(Date cacheTime) {
        long msNow = new Date().getTime();
        long msExpiry = DateUtils.addSeconds(cacheTime, CACHE_TIMEOUT_SECONDS).getTime();
        return msNow > msExpiry;
    }

    public void putMappedCode(String context, String code, String codeSystem, String term, MappedCode mappedCode) {
        Validate.notEmpty(context);
        Validate.notEmpty(code + term);
        Validate.notNull(mappedCode);

        CodeCacheKey codeCacheKey = new CodeCacheKey(context, code, codeSystem, term);

        if (!this.codeActionsToCache.contains(mappedCode.getAction())) {
            hashMap.remove(codeCacheKey);
            return;
        }

        hashMap.put(codeCacheKey, mappedCode);
        hashMapExpiryDates.put(codeCacheKey, new Date());
    }
}
