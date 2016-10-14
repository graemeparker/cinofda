package com.adfonic.weve.service;

import java.util.Map.Entry;
import java.util.Set;

import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

public interface BeaconService {
    
    public abstract Long checkWeveIdExists(Integer operatorId, String endUserId);
    
    /*
     * Use this method to check whether the given ip address is within the set ip ranges
     * for any operator. If ip address is within range return a dto with the operator info, 
     * otherwise operatorId -1. 
     */
    public abstract WeveOperatorDto retrieveOperatorInfoByIpAddressLookup(String ipAddress);
    
    public abstract String retrieveValidationRegexForDeviceId(Integer deviceIdTypeId);

    public Set<Entry<Integer, DeviceIdentifierTypeDto>> retrieveDeviceIdentifiers();
    
}
