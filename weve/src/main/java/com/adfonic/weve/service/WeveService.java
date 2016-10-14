package com.adfonic.weve.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface WeveService {

    static final int OPERATOR_NOT_FOUND = -1;
    static final long WEVE_ID_NOT_FOUND = 0L;
    static final String REQUEST_PREFIX = "d.";
    static final String ADTRUTH_DATA = "adtruth_data";
    static final String AD_ACTION_IMPRESSION = "IMPRESSION";
    static final String ADTRUTH_ID_SYSTEM_NAME = "atid";

    List<String> getDeviceIds(HttpServletRequest request);
    
    public void logHeaders(HttpServletRequest request);
}
