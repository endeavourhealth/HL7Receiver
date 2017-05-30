package org.endeavourhealth.hl7receiver.mapping;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CodeCacheKey extends Object {
    private String context;
    private String code;
    private String codeSystem;
    private String term;

    public CodeCacheKey(String context, String code, String codeSystem, String term) {
        this.context = StringUtils.trimToEmpty(context);
        this.code = StringUtils.trimToEmpty(code);
        this.codeSystem = StringUtils.trimToEmpty(codeSystem);
        this.term = StringUtils.trimToEmpty(term);
    }

    public String getContext() {
        return context;
    }

    public String getCode() {
        return code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public String getTerm() {
        return term;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CodeCacheKey that = (CodeCacheKey) o;

        return new EqualsBuilder()
                .append(context, that.context)
                .append(code, that.code)
                .append(codeSystem, that.codeSystem)
                .append(term, that.term)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(context)
                .append(code)
                .append(codeSystem)
                .append(term)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("context", context)
                .append("code", code)
                .append("codeSystem", codeSystem)
                .append("term", term)
                .toString();
    }
}
