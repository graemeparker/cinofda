package com.adfonic.weve.dao;

import java.util.List;

import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

public interface BeaconServiceDao {

    public abstract List<WeveOperatorDto> getIpRangesAndHeaderNameForOperator();
    
    public abstract Long findWeveId(Integer operatorId, String endUserId);
    
    public abstract List<DeviceIdentifierTypeDto> getDeviceIdsAndRegexValidationString();

    public abstract Integer saveDeviceIds(Long weveId, String deviceIds, String adSpaceExternalId, String creativeExternalId);

    public abstract Integer saveDeviceIdsForUnknownUser(String encodedEndUserId, Integer operatorId, String deviceIds, String adSpaceExternalId, String creativeExternalId);

}
