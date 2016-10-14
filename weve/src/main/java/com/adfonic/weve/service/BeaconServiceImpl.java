package com.adfonic.weve.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.adfonic.util.Range;
import com.adfonic.util.Subnet;
import com.adfonic.weve.OperatorEnum;
import com.adfonic.weve.dao.BeaconServiceDao;
import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

@Service
public class BeaconServiceImpl implements BeaconService {
    
    private static final Logger LOG = LogManager.getLogger(BeaconServiceImpl.class.getName());

    @Autowired
    BeaconServiceDao dao;
    List<WeveOperatorDto> operatorCache;
    Map<Integer, DeviceIdentifierTypeDto> deviceCache;
    
    @Value("${weve.operatorcache.refresh.enabled}")
    private boolean refreshEnabled;

    @PostConstruct
    private void initialiseCache() {
        populateDeviceIdValidationInfo();
        populateOperatorInfo();
    }
    
    public void populateOperatorInfo() {
        operatorCache = dao.getIpRangesAndHeaderNameForOperator();
        LOG.info("Operator cache size = " + operatorCache.size());
        for (WeveOperatorDto dto : operatorCache) {
            LOG.debug("Caching operator {} with ipRange: {} to {} and fine logging: [bs={}, oo={}]", dto.getOperatorId(), 
                            dto.getIpRangeStart(), dto.getIpRangeEnd(), dto.getBeaconServiceFineLoggingOn(), dto.getOptOutFineLoggingOn());
        }
    }
    
    @Scheduled(cron = "${weve.operatorcache.schedule}")
    public void refreshOperatorCache() {
        if (refreshEnabled) {
            LOG.debug("Refreshing cache now > {}", DateTime.now());
            populateOperatorInfo();
        }
    }

    @Override
    public Long checkWeveIdExists(Integer operatorId, String endUserId) {
        return dao.findWeveId(operatorId, endUserId);
    }

    public void populateDeviceIdValidationInfo() {
        List<DeviceIdentifierTypeDto> deviceIdsAndRegexes = dao.getDeviceIdsAndRegexValidationString();
        deviceCache = new HashMap<Integer, DeviceIdentifierTypeDto>();
        for (DeviceIdentifierTypeDto dto : deviceIdsAndRegexes) {
            deviceCache.put(dto.getTypeId(), dto);
        }
    }

    @Override
    public WeveOperatorDto retrieveOperatorInfoByIpAddressLookup(String ipAddress) {
        if (Subnet.isIpAddress(ipAddress)) {
            long ipAddressValue = Subnet.getIpAddressValue(ipAddress);
            for (WeveOperatorDto dto : getOperatorCache()) {
                Range<Long> ipRange = new Range<Long>(dto.getIpRangeStart(), dto.getIpRangeEnd());
                if (checkIpAddressIsInRange(ipAddressValue, ipRange)) {
                    LOG.debug("Confirmed ip {} is within range for Operator {}", ipAddress, OperatorEnum.getNameById(dto.getOperatorId()));
                    return dto;
                } 
            }
        }
        LOG.warn("Ip address {} did not fall within any defined Operator ranges", ipAddress);
        return new WeveOperatorDto(-1, OperatorEnum.OPERATOR_NOT_FOUND.name());
    }
    
    @Override
    public String retrieveValidationRegexForDeviceId(Integer deviceIdTypeId) {
        DeviceIdentifierTypeDto dto = getDeviceCache().get(deviceIdTypeId);
        return dto != null ? dto.getRegexPattern() : null;
    }
    
    @Override
    public Set<Entry<Integer, DeviceIdentifierTypeDto>> retrieveDeviceIdentifiers() {
        return getDeviceCache().entrySet();
    }
    
    private boolean checkIpAddressIsInRange(long ipAddressValue, Range<Long> ipRange) {
        if (ipAddressValue >= ipRange.getStart() && ipAddressValue <= ipRange.getEnd()) {
            return true;
        }
        return false;
    }
    
    protected List<WeveOperatorDto> getOperatorCache() {
        return operatorCache;
    }
    
    private Map<Integer, DeviceIdentifierTypeDto> getDeviceCache() {
        return deviceCache;
    }

}
