package com.adfonic.retargeting.redis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractRedis {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AbstractRedis.class);
    
    public static final String AUDIENCE_RECENCY = "A";
    public static final String SEGMENT_RECENCY = "S";
    public static final String AUDIENCE = "a";
    public static final String OPTOUT = "o";

    protected ThreadLocal<MessageDigest> sha = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
                return md;
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("can not create MessageDigest", e);
            }
            return null;
        }
    };
}
