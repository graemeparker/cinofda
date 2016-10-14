package com.adfonic.tasks.combined.truste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardcodedTrusteIdTypeMapper implements TrusteIdTypeMapper {

    private final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public long mapAdfonicIdType(String idName) {

        String lowerCaseType = idName.toLowerCase();
        switch (lowerCaseType) {
        case "dpid":
            return 1;
        case "odin-1":
            return 2;
        case "openudid":
            return 3;
        case "android":
            return 4;
        case "udid":
            return 5;
        case "ifa":
            return 6;
        case "hifa":
            return 7;
        case "atid":
            return 8;
        case "adid":
            return 9;
        case "adid_md5":
            return 10;
        case "gouid":
            return 11;
        case "idfa":
            return 12;
        case "idfa_md5":
            return 13;
        default:
            logger.warn("unknown type: {}", idName);
        }

        return 0;
    }

}
