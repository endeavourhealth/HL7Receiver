package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CodeCache {

    private ConcurrentHashMap<CodeCacheKey, MappedCode> hashMap = new ConcurrentHashMap<>();
    private List<MappedCodeAction> codeActionsToCache = new ArrayList<>();

    public CodeCache(MappedCodeAction... codeActionsToCache) {
        Validate.notNull(codeActionsToCache);

        this.codeActionsToCache = new ArrayList<>(Arrays.asList(codeActionsToCache));
    }

    public MappedCode getMappedCode(String context, String code, String codeSystem, String term) {
        Validate.notEmpty(context);
        Validate.notEmpty(code + term);

        CodeCacheKey codeCacheKey = new CodeCacheKey(context, code, codeSystem, term);

        return hashMap.getOrDefault(codeCacheKey, null);
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
    }
}
