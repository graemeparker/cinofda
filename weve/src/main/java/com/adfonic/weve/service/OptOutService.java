package com.adfonic.weve.service;

import java.util.List;
import java.util.Set;


public interface OptOutService {
    
    public int performOptOut(List<String> deviceIds);
    
    public int performOptOutEsk(Set<Long> weveIds);
    
    public Long checkIfWeveIdExists(String deviceId, int deviceIdType);
}
