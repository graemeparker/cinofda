package com.adfonic.weve.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.weve.WeveUtil;
import com.adfonic.weve.dto.WeveOperatorDto;
import com.adfonic.weve.service.BeaconService;
import com.adfonic.weve.service.CorrelationService;
import com.adfonic.weve.service.WeveService;

@Controller
public class BeaconController extends AbstractWeveController {
    
    @Autowired
    protected BeaconService beaconService;
    
    @Autowired
    private WeveService weveService;
    
    @Autowired
    private CorrelationService correlationService;
    
    @Value("${weve.log.uid}")
    private boolean logUserIdEnabled;
    
    private static final String GIF_RESOURCE = "/WEB-INF/images/clear.gif";
    
    private static final String MONITOR_GIF_RESOURCE = "/WEB-INF/images/monitor.gif";
    
    private static final transient Logger LOG = LogManager.getLogger(BeaconController.class.getName());
    
    private byte[] gifContent;
    
    private byte[] gifContentMonitor;
    
    @PostConstruct
    private void initialiseGifContent() throws IOException {
        gifContent = IOUtils.toByteArray(getServletContext().getResourceAsStream(GIF_RESOURCE));
            LOG.info("Read {} bytes from {}", gifContent.length, GIF_RESOURCE);
            
        gifContentMonitor = IOUtils.toByteArray(getServletContext().getResourceAsStream(MONITOR_GIF_RESOURCE));
            LOG.info("Read {} bytes from {}", gifContentMonitor.length, MONITOR_GIF_RESOURCE);
    }
    
    @RequestMapping("weve/bc/{adspaceExternalID}/{clickExternalID}/{creativeExternalID}")
    public void handleRequest(HttpServletRequest request,
                            HttpServletResponse response,
                             @PathVariable
                            String adspaceExternalID,
                            @PathVariable
                            String clickExternalID,
                            @PathVariable
                            String creativeExternalID) throws IOException {
        
        LOG.info("Processing request for adSpace/impression/creative: " + adspaceExternalID + "/" + clickExternalID + "/" + creativeExternalID);
        
        // MAX-2773: O2 monitoring
        if ("o2".equalsIgnoreCase(adspaceExternalID)){
            LOG.warn("O2 Monitor beacon header");
            if (StringUtils.isBlank(request.getHeader("x-up-calling-line-id#2"))) {
                // Spank out hard, no 200's here!
                LOG.warn("O2 Monitor no beacon header SC_INTERNAL_SERVER_ERROR");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            } else {
                // Return an image O2 can see on a handset.
                LOG.warn("O2 Monitor beacon header OK return image");
                buildOKResponseAndServeBeacon(response, true);
            }    
            return;
        }
        
        String ipAddressParameter = request.getRemoteAddr();
        if (StringUtils.isBlank(ipAddressParameter)) {
            LOG.warn("Client IP lookup failed. Did not get an IP address from request.getRemoteAddr()...returning 200 anyway");
            //AD-348 we don't want to sendError 403 in this case, always return 200 + pixel 
            buildOKResponseAndServeBeacon(response);
            return;
        }
        
        WeveOperatorDto operatorInfo = beaconService.retrieveOperatorInfoByIpAddressLookup(ipAddressParameter); 
        if (WeveService.OPERATOR_NOT_FOUND == operatorInfo.getOperatorId()) {
            LOG.warn("Operator lookup failed for ipAddress: {}, returning 200 anyway", ipAddressParameter);
            //AD-348 we don't want to sendError 403 in this case, always return 200 + pixel 
            buildOKResponseAndServeBeacon(response);
            return;
        }
        
        boolean isFineLoggingEnabled = operatorInfo.getBeaconServiceFineLoggingOn();
        if (isFineLoggingEnabled) {
            weveService.logHeaders(request);
        }
        
        String encodedEndUserId = request.getHeader(operatorInfo.getRequestHeaderName()); 
        if (StringUtils.isBlank(encodedEndUserId)) {
            LOG.warn("Request header lookup failed for ipAddress: {}", ipAddressParameter);
            if (isFineLoggingEnabled) {
                LOG.debug("{} not found on the request", operatorInfo.getRequestHeaderName());
            }
            //AD-335 apparently we don't want to send 500 in this case but send 200 + pixel anyway
            buildOKResponseAndServeBeacon(response);
            return;
        }
        
        Long weveUserId = beaconService.checkWeveIdExists(operatorInfo.getOperatorId(), encodedEndUserId);
        List<String> deviceIds = weveService.getDeviceIds(request);
        if (WeveService.WEVE_ID_NOT_FOUND == weveUserId) {
            // MAD-903 Configurable switch to capture device ids when weve user id not found
            if (logUserIdEnabled) {
                // encodedEndUserId should not be null at this point
                correlationService.recordDeviceIdsForUnknownUser(encodedEndUserId, operatorInfo.getOperatorId(), 
                        deviceIds, adspaceExternalID, creativeExternalID);
                LOG.debug("Saving device ids against endUserId: {}", encodedEndUserId);
            }
            
            if (isFineLoggingEnabled) {
                LOG.debug("Weve id not found for endUserId: {}  ipAddress: {}, returning 200 anyway", encodedEndUserId, ipAddressParameter);
            } else {
                LOG.info("Weve id not found, returning 200 anyway");
            }
        } else {
            if (isFineLoggingEnabled) {
                LOG.debug("Weve id {} found for endUserId: {} ipAddress: {}", weveUserId, encodedEndUserId, ipAddressParameter);
            } else {
                LOG.info("Weve id found.");
            }
            if (deviceIds.isEmpty()) {
                LOG.warn("No suitable device ids extracted from this request for ipAddress: {}, returning 200 anyway", ipAddressParameter);
            } else {
                LOG.debug("Device ids found: {}", WeveUtil.printableDeviceIds(deviceIds));
            }
            
            correlationService.correlateDeviceIdsWithEndUser(weveUserId, deviceIds, adspaceExternalID, creativeExternalID); 
        }
        
        buildOKResponseAndServeBeacon(response);
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public void handleJdbcConnectionError(HttpServletResponse response, 
                                          CannotGetJdbcConnectionException e) throws IOException {
        LOG.error("Cannot connect to the database: {}", e.getMessage());
        buildOKResponseAndServeBeacon(response);
    }
    
    @RequestMapping("/weve/bc/{adspaceExternalID}/{clickExternalID}") 
    public void handleOldBeaconRequest(HttpServletRequest request,
            HttpServletResponse response,
             @PathVariable
            String adspaceExternalID,
            @PathVariable
            String clickExternalID) throws IOException {
        handleRequest(request, response, adspaceExternalID, clickExternalID, null);
    }
    
    @RequestMapping("//weve/bc/{adspaceExternalID}/{clickExternalID}") 
    public void handleWeirdOldRequest(HttpServletRequest request,
            HttpServletResponse response,
             @PathVariable
            String adspaceExternalID,
            @PathVariable
            String clickExternalID) throws IOException {
        handleRequest(request, response, adspaceExternalID, clickExternalID, null);
    }
    
    @RequestMapping("//weve/bc/{adspaceExternalID}/{clickExternalID}/{creativeExternalID}") 
    public void handleWeirdNewRequest(HttpServletRequest request,
            HttpServletResponse response,
             @PathVariable
            String adspaceExternalID,
            @PathVariable
            String clickExternalID,
            @PathVariable
            String creativeExternalID) throws IOException {
        handleRequest(request, response, adspaceExternalID, clickExternalID, creativeExternalID);
    }
    
    private void buildOKResponseAndServeBeacon(HttpServletResponse response) throws IOException {
        buildOKResponseAndServeBeacon(response, false);
    }
    
    private void buildOKResponseAndServeBeacon(HttpServletResponse response, boolean monitor) throws IOException {
        if(null == gifContent) {
            initialiseGifContent();
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/gif");
        OutputStream outputStream = response.getOutputStream();
        if (monitor){
            outputStream.write(gifContentMonitor);
        }else{
            outputStream.write(gifContent);
        }
        outputStream.flush();
    }
        
}
