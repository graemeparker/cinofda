package com.byyd.middleware.iface.service;

import org.apache.commons.lang.StringUtils;

import com.byyd.middleware.iface.dao.LikeSpec;

/**
 * This class contains methods that should be visible to all service layer level classes, but not methods having to do
 * with persistence. These belong in the base classes for managers in any persistence impl
 *
 * @author pierre
 *
 */
public abstract class BaseManagerImpl implements BaseManager {

    /**
    */
    protected Long makeLong(String s) {
        return makeDouble(s).longValue();
    }

    /**
    */
    protected Integer makeInteger(String s) {
        return makeDouble(s).intValue();
    }
    /**
    */
    protected Double makeDouble(String s) {
        if(s == null || s.length() == 0) {
            return Double.valueOf(0);
        }
        boolean foundPeriod = false;
        boolean foundMinusSign = false;
        StringBuilder buf = new StringBuilder();
        for(int i = 0;i < s.length();i++) {
            char c = s.charAt(i);
            if ((c == '.') && (!foundPeriod)) {
                buf.append(c);
                foundPeriod = true;
            }
            if ((c == '-')&&(!foundMinusSign)) {
                buf.append(c);
                foundMinusSign = true;
            }
            if(c >= '0' && c <= '9') {
                buf.append(c);
            }
        }
        if(buf.length() == 0) {
            return Double.valueOf(0);
        }
        try {
            return new Double(buf.toString());
        } catch(Exception e) {
            return Double.valueOf(0);
        }
    }
    /**
     *
     * @param email
     * @return
     */
    protected String formatEmailForStorage(String email) {
        if(StringUtils.isEmpty(email)) {
            return "";
        }
        return email.toLowerCase();
    }
    /**
     *
     * @param email
     * @return
     */
    protected String formatEmailForSearches(String email) {
        if(StringUtils.isEmpty(email)) {
            return "";
        }
        return email.toLowerCase();
    }
    /**
     *
     * @param target
     * @param likeSpec
     * @return
     */
    protected String formatLikeSearchTarget(String target, LikeSpec likeSpec) {
        LikeSpec localLikeSpec = likeSpec;
        if(localLikeSpec == null) {
            localLikeSpec = LikeSpec.CONTAINS;
        }
        return localLikeSpec.getPattern(target);
    }
}
