package com.adfonic.weve.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adfonic.weve.WeveUtil;
import com.adfonic.weve.dao.BeaconServiceDao;

@Service
public class CorrelationServiceImpl implements CorrelationService {
    
    private static final Logger LOG = LogManager.getLogger(CorrelationServiceImpl.class.getName());

    @Autowired
    BeaconServiceDao dao;

    @Override
    @Async
    public void correlateDeviceIdsWithEndUser(Long weveId, List<String> deviceIds, String adSpaceExternalId, String creativeExternalId) {
        int rowsInserted = dao.saveDeviceIds(weveId, WeveUtil.normalizeDeviceIdList(deviceIds), adSpaceExternalId, creativeExternalId);

        if (rowsInserted < 0) {
            LOG.info("Malformed device id string, should be deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId ");
        } else if (rowsInserted == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("None of the following device ids {} were successfully saved against the weve id: {}", WeveUtil.printableDeviceIds(deviceIds), weveId);
            } else {
                LOG.info("No device ids were successfully saved against the found weve id.");
            }
        } else if (rowsInserted > 0) {
            if(rowsInserted < deviceIds.size()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The number ({}) of device ids saved against the weve id {} were less than the number supplied ({}).", rowsInserted, weveId, deviceIds.size());
                } else {
                    LOG.info("The number of device ids saved against the weve id were less than the number supplied.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The number ({}) of device ids saved against the weve id {} were SAME AS number supplied ({}).", rowsInserted, weveId, deviceIds.size());
                } else {
                    LOG.info("The number of device ids saved against the weve id were SAME AS number supplied.");
                }
            }
        } else {
            LOG.info("The number of device ids saved against the weve id were {}", rowsInserted);
        }
    }

    @Override
    public void recordDeviceIdsForUnknownUser(String encodedEndUserId, Integer operatorId, List<String> deviceIds, 
                                              String adSpaceExternalId, String creativeExternalId) {
        int rowsInserted = dao.saveDeviceIdsForUnknownUser(encodedEndUserId, operatorId, 
                WeveUtil.normalizeDeviceIdList(deviceIds), adSpaceExternalId, creativeExternalId);
        
        if (rowsInserted < 0) {
            LOG.info("Malformed device id string, should be deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId|deviceIdValue~deviceTypeId ");
        } else if (rowsInserted == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("None of the following device ids {} were successfully saved against the display_uid: {}", WeveUtil.printableDeviceIds(deviceIds), encodedEndUserId);
            } else {
                LOG.info("No device ids were successfully saved against the found encoded user id.");
            }
        } else if (rowsInserted > 0) {
            if(rowsInserted < deviceIds.size()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The number ({}) of device ids saved against the display_uid {} were less than the number supplied ({}).", rowsInserted, encodedEndUserId, deviceIds.size());
                } else {
                    LOG.info("The number of device ids saved against the encoded user id were less than the number supplied.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The number ({}) of device ids saved against the display_uid {} were SAME AS number supplied ({}).", rowsInserted, encodedEndUserId, deviceIds.size());
                } else {
                    LOG.info("The number of device ids saved against the encoded user id were SAME AS number supplied.");
                }
            }
        } else {
            LOG.info("The number of device ids saved against the encoded user id were {}", rowsInserted);
        }
    }
}
