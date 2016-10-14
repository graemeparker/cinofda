package com.adfonic.weve.service;

import java.util.List;

public interface CorrelationService {
    
    public abstract void correlateDeviceIdsWithEndUser(Long weveId, 
                                                       List<String> deviceIds, 
                                                       String adSpaceExternalId, 
                                                       String creativeExternalId);

    public abstract void recordDeviceIdsForUnknownUser(String encodedEndUserId,
                                                       Integer operatorId,
                                                       List<String> deviceIds,
                                                       String adSpaceExternalId, 
                                                       String creativeExternalId);
    
}
