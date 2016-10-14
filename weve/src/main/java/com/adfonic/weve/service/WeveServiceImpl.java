package com.adfonic.weve.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.util.AdtruthUtil;
import com.adfonic.weve.dto.DeviceIdentifierTypeDto;

@Service
public class WeveServiceImpl implements WeveService {

    private static final Logger LOG = LogManager.getLogger(WeveServiceImpl.class.getName());

    @Autowired
    protected BeaconService beaconService;

    @Override
    public List<String> getDeviceIds(HttpServletRequest request) {
        List<String> deviceIds = new ArrayList<String>();
        for (Entry<Integer, DeviceIdentifierTypeDto> deviceIdTypeEntry : beaconService.retrieveDeviceIdentifiers()) {
            String id;
            String systemName = deviceIdTypeEntry.getValue().getSystemName();
            if (systemName.equals(ADTRUTH_ID_SYSTEM_NAME)) {
                id = generateAdtruthId(request);
            } else {
                id = request.getParameter(REQUEST_PREFIX + systemName);
            }
            if (StringUtils.isNotBlank(id) && isDeviceIdValid(id, deviceIdTypeEntry.getKey()) && deviceIdTypeEntry.getValue().isSecure()) {
                deviceIds.add(id + "~" + deviceIdTypeEntry.getKey());
            }
        }
        return deviceIds;
    }

    @Override
    public void logHeaders(HttpServletRequest request) {
        StringBuilder sbuf = new StringBuilder();
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            Object nextElement = headerNames.nextElement();
            sbuf.append("******Header ").append(nextElement).append("=").append(request.getHeader((String) nextElement)).append("\n");
        }
        LOG.debug("*******************************************************************************\n" + "******Timestamp: {}\n{}"
                + "*************************************************************************************", DateTime.now(), sbuf.toString());
    }

    private String generateAdtruthId(HttpServletRequest request) {
        String adTruthData = request.getParameter("d.adtruth_data");
        if (StringUtils.isNotBlank(adTruthData)) {
            LOG.debug("Found adtruth data on request: {} proceeding to generate atid", adTruthData);
            // code sourced from Tracker InstallController
            Map<String, String> metaDataMapforAdtruthLogging = new LinkedHashMap<String, String>();
            // AD-252 Adtruth want to log the event_type.
            metaDataMapforAdtruthLogging.put("event_type", AD_ACTION_IMPRESSION);
            String browserIp = request.getHeader("X-FORWARDED-FOR");
            if (browserIp == null) {
                browserIp = request.getRemoteAddr();
                LOG.debug("Couldn't find browserIp using X-FORWARDED-FOR, using request.RemoteAddress for client IP {}", browserIp);
            }
            // calculate the atid from the adTruthData
            String atid = AdtruthUtil.getAtid(request, adTruthData, browserIp, metaDataMapforAdtruthLogging);
            LOG.debug("ADTRUTH ID GENERATED: {} from AdTruthData: {}", atid, adTruthData);
            return atid;
        }
        LOG.debug("returning null for atid");
        return null;
    }

    private boolean isDeviceIdValid(String deviceIdValue, Integer deviceTypeId) {
        Pattern validationPattern = Pattern.compile(beaconService.retrieveValidationRegexForDeviceId(deviceTypeId));
        if (validationPattern.matcher(deviceIdValue).matches()) {
            return true;
        }
        LOG.debug("Device id {} did not match validation pattern for type {}", deviceIdValue, deviceTypeId);
        return false;
    }
}
