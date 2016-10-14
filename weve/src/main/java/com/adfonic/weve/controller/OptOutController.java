package com.adfonic.weve.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.weve.dto.WeveOperatorDto;
import com.adfonic.weve.service.BeaconService;
import com.adfonic.weve.service.OptOutService;
import com.adfonic.weve.service.WeveService;

@Controller
public class OptOutController extends AbstractWeveController {
    
    private static final Logger LOG = LogManager.getLogger(OptOutController.class.getName());
    
    @Autowired
    private OptOutService optoutService;
    
    @Autowired
    private WeveService weveService;
    
    @Autowired
    protected BeaconService beaconService;
        
    private int permissionSource = 10;//radio
    private Set<Long> weveIds;
    
    private byte[] gifContentMonitor;
    
    private static final String MONITOR_GIF_RESOURCE = "/WEB-INF/images/monitor.gif";

    @RequestMapping("/weve/oo")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, boolean monitor) throws IOException {
        
        // MAX-2773: O2 monitoring
        if (monitor){
            LOG.warn("O2 Monitor header");
            if (StringUtils.isBlank(request.getHeader("x-up-calling-line-id#2"))) {
                // Spank out hard, no 200's here!
                LOG.warn("O2 Monitor no optout header SC_INTERNAL_SERVER_ERROR");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            } else {
                LOG.warn("O2 Monitor beacon header OK return image");
                // Return an image O2 can see on a handset.
                buildOKResponseAndServeImage(response, true);
            }    
            return;
        }
        
        weveIds = new HashSet<>();
        
        // Get opreator IP
        String ipAddressParameter = request.getRemoteAddr();
        if (StringUtils.isBlank(ipAddressParameter)) {
            LOG.info("Did not get an IP address from request.getRemoteAddr()");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Client IP lookup failed");
            return;
        }
        
        // Which operator is it?
        WeveOperatorDto operatorInfo = beaconService.retrieveOperatorInfoByIpAddressLookup(ipAddressParameter); 
        List<String> deviceIds = weveService.getDeviceIds(request);

        // We don't know the opreator (assume WIFI) so optout using device ids ONLY
        if (WeveService.OPERATOR_NOT_FOUND == operatorInfo.getOperatorId()) {
            LOG.warn("Operator lookup failed for ipAddress: {}", ipAddressParameter);
            
            // User is on WIFI and NOT on home radio network, set the value for SOURCE_ID here and proceed
            permissionSource = 11;//wifi

            // Weveid was not found using the display_uid, try getting weveid using the device ids
            if (!getWeveIdUsingDeviceIds(response, deviceIds)){
                return;
            }
        } else {
            LOG.info("Operator lookup passed for ipAddress: {} for operator {}", ipAddressParameter, operatorInfo.getOperatorId());

            // User is on home radio network and not on WIFI, thus we can try and fetch the header for weveenduserId
            if(operatorInfo.getOptOutFineLoggingOn()) {
                weveService.logHeaders(request);
            }
            
            if (!getWeveIdUsingDusplayUID(request, response, operatorInfo, ipAddressParameter)){
                return;
            }
        }
        
        if(weveIds.isEmpty()) {
            // On WIFI we should have had a device id we could get a weve id for. 
            // On radio network we should have had the header anyway.
            // We cannot continue!
            LOG.warn("Weve id not found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            
            if(operatorInfo.getOptOutFineLoggingOn()) {
                LOG.warn("About to optout from SAS API");
            }
            
            //if everything is okay with weve api then optout wevedb
            //wevedb optout store
            //we have devicesids to optout with wevedb storage api
            if (!optOutDeviceIds(response, operatorInfo, deviceIds)){
                return;
            }
            
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
    }

    @RequestMapping("/weve/oo/o2/")
    public void handleRequestO2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.warn("O2 Monitor optout header");
        handleRequest(request, response, true);
    }
    
    private boolean getWeveIdUsingDeviceIds(HttpServletResponse response, List<String> deviceIds) {
        Long weveUserId;
        if(!deviceIds.isEmpty()) {
            for(String deviceIdTildaType : deviceIds) {
                String[] splitString = deviceIdTildaType.split("~");
                weveUserId = optoutService.checkIfWeveIdExists(splitString[0], Integer.parseInt(splitString[1]));
                if(WeveService.WEVE_ID_NOT_FOUND == weveUserId || isNegative(weveUserId)) {
                    //weveid still not found, can't do much, log and move forward.
                    LOG.debug("Can't find Weve id for DI {}, returning 404", splitString[0]);
                } else {
                    //it could be possible that we get different weveIds for different DIs, 
                    //in which case store them in a set and optout later using the weve permission api
                    weveIds.add(weveUserId);
                }
            }
        }
        
        // return error as we can't find weveuserid using weveheader AND DIs on WIFI
        if(deviceIds.isEmpty() || weveIds.isEmpty()) {
            //returning 404
            LOG.warn("Weve id not found using Device Id lookup for {} DIs", deviceIds.size());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        
        return true;
    }
    
    private boolean getWeveIdUsingDusplayUID(HttpServletRequest request, HttpServletResponse response, WeveOperatorDto operatorInfo, String ipAddressParameter){
        String encodedEndUserId = request.getHeader(operatorInfo.getRequestHeaderName());
        if (StringUtils.isBlank(encodedEndUserId)) {
            //something went wrong and we couldn't fetch a valid header, we will be guessing it using deviceIds
            LOG.warn("Request header lookup failed for ipAddress: {}", ipAddressParameter);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        } else {
            // Looking weveUserId using display_uid in the header.
            Long weveUserId = beaconService.checkWeveIdExists(operatorInfo.getOperatorId(), encodedEndUserId);
            if (WeveService.WEVE_ID_NOT_FOUND == weveUserId || isNegative(weveUserId)) {
                LOG.warn("Weve id not found for endUserId: {} cannot continue on opreator network", encodedEndUserId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            } else {
                LOG.info("Weve id {} found for endUserId: {}", weveUserId, encodedEndUserId);
                //add it to the SET for optingout with the weve permission api
                weveIds.add(weveUserId);
            }
        }
        return true;
    }
    
    private boolean optOutDeviceIds(HttpServletResponse response, WeveOperatorDto operatorInfo, List<String> deviceIds) {
        if (!deviceIds.isEmpty()) {
            
            if(operatorInfo.getOptOutFineLoggingOn()) {
                LOG.warn("About to optout from wevedb using device ids");
            }

            int rowsInserted = optoutService.performOptOut(deviceIds);
            if(rowsInserted <= 0) {
                //all the DIs for this profile have already been opted out, hence no change
                LOG.warn("Something happened while opting out from WeveDB using device ids");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }else{
            if(operatorInfo.getOptOutFineLoggingOn()) {
                LOG.warn("About to optout from wevedb using header only");
            }

            // Optout using header (now a weve id) only - MAX-48
            int rowsInserted = optoutService.performOptOutEsk(weveIds);
            if(rowsInserted <= 0) {
                //all the DIs for this profile have already been opted out, hence no change
                LOG.info("Something happened while opting out from WeveDB using esks");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }
        return true;
    }

    private boolean isNegative(Long weveUserId) {
        return Long.signum(weveUserId) == -1;
    }
    
    //for testing purposes only
    protected int getPermissionSourceId() {
        return permissionSource;
    }
    
    protected int getSetSize() {
        return weveIds.size();
    }
    
    private void buildOKResponseAndServeImage(HttpServletResponse response, boolean monitor) throws IOException {
        if (monitor){
            if(null == gifContentMonitor) {
                initialiseGifContent();
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("image/gif");
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(gifContentMonitor);
            outputStream.flush();
        }
    }
    
    @PostConstruct
    private void initialiseGifContent() throws IOException {
        gifContentMonitor = IOUtils.toByteArray(getServletContext().getResourceAsStream(MONITOR_GIF_RESOURCE));
            LOG.info("Read {} bytes from {}", gifContentMonitor.length, MONITOR_GIF_RESOURCE);
    }    
}
