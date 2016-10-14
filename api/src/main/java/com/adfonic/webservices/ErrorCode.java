package com.adfonic.webservices;

import java.util.HashMap;
import java.util.Map;

public interface ErrorCode {
    int UNKNOWN = 0;
    
    //General error code for uncategorized faults
    int PROCESSING_ERROR = 5000;
    
    int AUTH_NO_AUTHORIZATION = 1000;
    int AUTH_INVALID_AUTHORIZATION = 1001;
    int AUTH_INVALID_EMAIL = 1002;
    int AUTH_INVALID_DEVELOPER_KEY = 1003;
    int AUTH_INVALID_IP_ADDRESS = 1004;

    int PARAMETER_MISSING = 1500;
    
    int ACCESS_DENIED = 1600;
    
    int ENTITY_NOT_FOUND = 2000;
    int NOT_OWNER = 2001;
    int INVALID_ARGUMENT = 2002;
    
    int FORBIDDEN_GENERAL = 4030;
    int FORBIDDEN_WRITE = 4033;

    // TODO - revisit - esp the values
    int INVALID_STATE = 4200;
    int ENTITY_ALREADY_EXISTS = 4201;
    int NOT_SUPPORTED = 4202;
    int GENERAL = 4203;
    int VALIDATION_ERROR = 4205;
    int REFERENCE_ENTITY_NOT_FOUND = 4005;
    
    public static class Status {
        public static Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        static {
            map.put(ENTITY_NOT_FOUND, 404);
            map.put(REFERENCE_ENTITY_NOT_FOUND, 403);
            map.put(VALIDATION_ERROR, 403);
        }

    }
}