package com.byyd.middleware.iface.dao;


import org.apache.commons.lang.StringUtils;

public enum LikeSpec {
    STARTS_WITH,
    CONTAINS,
    ENDS_WITH,
    WORD_STARTS_WITH,
    WORD_ENDS_WITH;

    public String getPattern(String sz) {
        if(StringUtils.isEmpty(sz)) {
            return sz;
        }
        if(this.equals(CONTAINS)) {
            return "%" + sz + "%";
        } else if(this.equals(STARTS_WITH)) {
            return sz + "%";
        } else if(this.equals(ENDS_WITH)) {
            return "%" + sz;
        } else if(this.equals(WORD_STARTS_WITH)) {
            throw new IllegalArgumentException("WORD_STARTS_WITH is not supported for this query");
        } else if(this.equals(WORD_ENDS_WITH)) {
            throw new IllegalArgumentException("WORD_ENDS_WITH is not supported for this query");
        }
        return sz;
    }

    public String getRegex(String sz) {
        if(StringUtils.isEmpty(sz)) {
            return sz;
        }
        if(this.equals(WORD_STARTS_WITH)) {
            return "[[:<:]]" + sz;
        } else if(this.equals(WORD_ENDS_WITH)) {
            return sz + "[[:>:]]";
        }
        return sz;
    }

    public boolean isRegex() {
        return this.equals(WORD_STARTS_WITH) || this.equals(WORD_ENDS_WITH);
    }

}
