package com.adfonic.weve.service;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.weve.WeveUtil;
import com.adfonic.weve.dao.OptOutServiceDao;

@Service
public class OptOutServiceImpl implements OptOutService {
    
    private static final transient Logger LOG = LogManager.getLogger(OptOutService.class.getName());
    
    private static final int OPT_OUT = 0;

    @Autowired
    OptOutServiceDao dao;
    
    @Override
    public int performOptOut(List<String> deviceIds) {
        LOG.debug("saving optout for deviceIds");
        int rowsInserted = dao.saveOptOut(WeveUtil.normalizeDeviceIdList(deviceIds), OPT_OUT);
        
        if (rowsInserted < 0) {
            LOG.warn("Malformed device id string, should be deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId ");
        } else if (rowsInserted == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("None of the following device ids {} were successfully saved", WeveUtil.printableDeviceIds(deviceIds));
            } else {
                LOG.warn("No device ids were successfully saved against the found weve id.");
            }
        } else if (rowsInserted > 0 && rowsInserted < deviceIds.size()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The number ({}) of device ids saved were less than the number supplied ({}).", rowsInserted, deviceIds.size());
            } else {
                LOG.warn("The number of device ids saved against the weve id were less than the number supplied.");
            }
        }
        return rowsInserted;
    }

    @Override
    public Long checkIfWeveIdExists(String deviceId, int deviceIdType) {
        return dao.findWeveId(deviceId, deviceIdType);
    }

    @Override
    public int performOptOutEsk(Set<Long> weveIds) {
        LOG.debug("saving optout for weveids");
        int rowsInserted = dao.saveOptOutEsk(WeveUtil.normalizeWeveIdList(weveIds), OPT_OUT);
        
        if (rowsInserted < 0) {
            LOG.warn("Malformed weve id string, should be weveIdValue|weveIdValue");
        } else if (rowsInserted == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("None of the following weve ids {} were successfully opted out", WeveUtil.printableWeveIds(weveIds));
            } else {
                LOG.warn("No weve ids were successfully opted out.");
            }
        } else if (rowsInserted > 0 && rowsInserted < weveIds.size()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The number ({}) of weve ids opted out were less than the number supplied ({}).", rowsInserted, weveIds.size());
            } else {
                LOG.warn("The number of weve ids opted out were less than the number supplied.");
            }
        }
        return rowsInserted;
    }
}
