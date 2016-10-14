package com.adfonic.tools.beans.campaign.targeting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.campaign.targeting.CampaignTargetingConnectionMBean.IpValidationResult.ValidationError;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class CampaignTargetingConnectionMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingConnectionMBean.class);

    // Pattern for allowed IPs: XXX.XXX.XXX.XXX or XXX.XXX.XXX.XXX/N  :  0<=XXX<=255 and 0<=N<=32
    Pattern IP_ALLOWED_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(\\d|[1-2]\\d|3[0-2]))?$");
    // Pattern for private IPs: 127.0.0.1 (loopback) or 10.XXX.XXX.XXX or (172.16.XXX.XXX to 172.31.XXX.XXX) or 192.168.XXX.XXX   :  0<=XXX<=255    
    Pattern IP_PRIVATE_PATTERN = Pattern.compile("(^127\\.0\\.0\\.1)|(^10\\..*)|(^172\\.1[6-9]\\..*)|(^172\\.2[0-9]\\..*)|(^172\\.3[0-1]\\..*)|(^192\\.168\\..*)"); 

    private List<OperatorAutocompleteDto> mobileOperatorsList = new ArrayList<OperatorAutocompleteDto>(0);
    private List<OperatorAutocompleteDto> ispOperatorsList = new ArrayList<OperatorAutocompleteDto>(0);

    private String connectionType = "NONE";

    private List<String> connectionTypes = new ArrayList<String>(0);

    private boolean mobileOperatorsWhiteList;
    private boolean ispOperatorsWhiteList;

    private CampaignDto campaignDto;

    private boolean excludedOpera;
    private boolean oldExcludeOpera;

    private List<BrowserDto> targetedBrowsers = new ArrayList<BrowserDto>(0);

    private BrowserDto operaBrowser;
    
    private boolean ipAddressesListWhitelist;
    private String ipAddressesString;
    private String addIpAddressesTextArea;
    private List<IpValidationResult> ipValidationResult = new ArrayList<>();

    @Override
    public void init() {
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (campaignDto != null) {
            this.mobileOperatorsWhiteList = campaignDto.getCurrentSegment().getMobileOperatorListIsWhitelist();
            this.mobileOperatorsList = new ArrayList<OperatorAutocompleteDto>(campaignDto.getCurrentSegment().getMobileOperators());
            this.ispOperatorsWhiteList  = campaignDto.getCurrentSegment().getIspOperatorListIsWhitelist();;
            this.ispOperatorsList = new ArrayList<OperatorAutocompleteDto>(campaignDto.getCurrentSegment().getIspOperators());
            if (campaignDto.getCurrentSegment().getBrowsers().contains(getOperaBrowser())) {
                excludedOpera = true;
            } else {
                excludedOpera = false;
            }

            if (CollectionUtils.isNotEmpty(campaignDto.getCurrentSegment().getBrowsers())) {
                targetedBrowsers = new ArrayList<BrowserDto>(campaignDto.getCurrentSegment().getBrowsers());
            }
            loadConnections();
            
            this.ipAddressesListWhitelist = campaignDto.getCurrentSegment().isIpAddressesListWhitelist();
            this.ipAddressesString = flatIpList(campaignDto.getCurrentSegment().getIpAddresses());
        }
        LOGGER.debug("loadCampaignDto<--");
    }
    
    public boolean isValid() {
        Set<String> ipAddressesAsSet = expandIpAddressString(this.ipAddressesString);

        // Checking IP Targeting
        if (!validateIpAddressesAsSet(ipAddressesAsSet)) {
            RequestContext.getCurrentInstance().execute("nonMatchedIpAddressesDialogWidget.show()");
            return false;
        }
        
        this.ipAddressesString = flatIpList(ipAddressesAsSet);
        return true;
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        campaignDto = dto;

        SegmentDto segment = campaignDto.getCurrentSegment();

        // Operators
        if (connectionType.equals("NONE")) {
            getMobileOperatorsList().clear();
            getIspOperatorsList().clear();
            excludedOpera = false;
        }
        else if(connectionType.equals("WIFI")){
            getMobileOperatorsList().clear();
            excludedOpera = false;
        }
        else if(connectionType.equals("OPERATOR")){
            getIspOperatorsList().clear();
        }
        
        Utils.fillSetWithList(getMobileOperatorsList(), segment.getMobileOperators());
        segment.setMobileOperatorListIsWhitelist(mobileOperatorsWhiteList);
        Utils.fillSetWithList(getIspOperatorsList(), segment.getIspOperators());
        segment.setIspOperatorListIsWhitelist(ispOperatorsWhiteList);
        
        // Connections
        fillConnections(segment);

        // start by clear/add from the advanced control
        segment.getBrowsers().clear();
        segment.getBrowsers().addAll(targetedBrowsers);

        // separate control for opera
        if (excludedOpera) {
            if (!segment.getBrowsers().contains(getOperaBrowser())) {
                segment.getBrowsers().add(getOperaBrowser());
            }
        } else if (segment.getBrowsers().contains(getOperaBrowser())) {
            segment.getBrowsers().remove(getOperaBrowser());
        }
        
        //Ip Addresses targeting
        segment.setIpAddressesListWhitelist(ipAddressesListWhitelist);
        segment.setIpAddresses(expandIpAddressString(this.ipAddressesString));
        
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    @SuppressWarnings("unchecked")
    public void onConnectionEvent(ValueChangeEvent event) {
        LOGGER.debug("onConnectionEvent-->");
        List<String> newValue = (List<String>) event.getNewValue();
        if (newValue.contains("WIFI") && newValue.contains("OPERATOR")) {
            connectionType = "BOTH";
        } else if (newValue.contains("WIFI") && !newValue.contains("OPERATOR")) {
            connectionType = "WIFI";
        } else if (newValue.contains("OPERATOR") && !newValue.contains("WIFI")) {
            connectionType = "OPERATOR";
        } else {
            connectionType = "NONE";
        }
        LOGGER.debug("Connection type: " + connectionType);
        LOGGER.debug("onConnectionEvent-->");
    }

    public String getConnectionsSummary(boolean spaces) {
        if (campaignDto != null && campaignDto.getSegments() != null) {
            String space = "";
            if (spaces) {
                space = " ";
            }
            String message = "";

            // All
            if (getCampaignDto().getCurrentSegment().getConnectionType().equals("BOTH") || connectionType.equals("NONE")) {
                return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
            } else {
                if (connectionTypes.contains("WIFI")) {
                    message += FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.wifi.label") + "," + space;
                }
                if (connectionTypes.contains("OPERATOR")) {
                    message += FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.operator.label") + "," + space;
                }
                if (excludedOpera) {
                    message += FacesUtils.getBundleMessage("page.campaign.menu.targeting.connection.connectionoptions.excludeopera.label") + ","
                            + space;
                }
                message = message.substring(0, message.length() - (1 + space.length()));
                return message;
            }
        }
        return notSet();
    }
    
    public String getMobileOperatorsSummary(boolean spaces) {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null
                && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getMobileOperators())) {
            return getOperatorsSummary(spaces, campaignDto.getCurrentSegment().getMobileOperators(),campaignDto.getCurrentSegment().getMobileOperatorListIsWhitelist());
        }
        else{
            return notSet();
        }
    }
    
    public String getIspOperatorsSummary(boolean spaces) {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null
                && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getIspOperators())) {
            return getOperatorsSummary(spaces, campaignDto.getCurrentSegment().getIspOperators(),campaignDto.getCurrentSegment().getIspOperatorListIsWhitelist());
        }
        else{
            return notSet();
        }
    }

    private String getOperatorsSummary(boolean spaces, Set<OperatorAutocompleteDto> operators, boolean isWhiteList) {
        String space = "";
        if (spaces) {
            space = " ";
        }
        String message = "";

        if (!isWhiteList) {
            message = "Excluded:" + space;
        }
        for (OperatorAutocompleteDto o : operators) {
            message += o.getName() + "," + space;
        }
        message = message.substring(0, message.length() - (1 + space.length()));
        return message;
    }

    public String getMobileOperatorsStyle() {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null
                && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getMobileOperators())) {
            return "display:block";
        } else {
            return "display:none";
        }
    }
    
    public String getIspOperatorsStyle() {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null
                && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getIspOperators())) {
            return "display:block";
        } else {
            return "display:none";
        }
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public List<OperatorAutocompleteDto> getMobileOperatorsList() {
        if (mobileOperatorsList == null) {
            mobileOperatorsList = new ArrayList<OperatorAutocompleteDto>();
        }
        return mobileOperatorsList;
    }

    public void setMobileOperatorsList(List<OperatorAutocompleteDto> mobileOperatorsList) {
        this.mobileOperatorsList = mobileOperatorsList;
    }

    public List<OperatorAutocompleteDto> getIspOperatorsList() {
        if (ispOperatorsList == null) {
            ispOperatorsList = new ArrayList<OperatorAutocompleteDto>();
        }
        return ispOperatorsList;
    }

    public void setIspOperatorsList(List<OperatorAutocompleteDto> ispOperatorsList) {
        this.ispOperatorsList = ispOperatorsList;
    }

    
    public boolean getMobileOperatorsWhiteList() {
        return mobileOperatorsWhiteList;
    }

    public void setMobileOperatorsWhiteList(boolean mobileOperatorsWhiteList) {
        this.mobileOperatorsWhiteList = mobileOperatorsWhiteList;
    }

    public boolean isIspOperatorsWhiteList() {
        return ispOperatorsWhiteList;
    }

    public void setIspOperatorsWhiteList(boolean ispOperatorsWhiteList) {
        this.ispOperatorsWhiteList = ispOperatorsWhiteList;
    }

    public List<String> getConnectionTypes() {
        if (connectionTypes == null) {
            connectionTypes = new ArrayList<String>();
        }
        return connectionTypes;
    }

    public void setConnectionTypes(List<String> connectionTypes) {
        this.connectionTypes = connectionTypes;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public boolean isExcludedOpera() {
        return excludedOpera;
    }

    public void setExcludedOpera(boolean excludedOpera) {
        this.excludedOpera = excludedOpera;
    }

    public List<BrowserDto> getTargetedBrowsers() {
        if (targetedBrowsers == null) {
            targetedBrowsers = new ArrayList<BrowserDto>();
        }
        return targetedBrowsers;
    }

    public void setTargetedBrowsers(List<BrowserDto> targetedBrowsers) {
        this.targetedBrowsers = targetedBrowsers;
    }

    public void cancelBrowsers(ActionEvent event) {
        SegmentDto segment = campaignDto.getCurrentSegment();
        targetedBrowsers.clear();
        targetedBrowsers = new ArrayList<BrowserDto>(segment.getBrowsers());
        excludedOpera = oldExcludeOpera;
    }

    // sync the separate controls
    public void addBrowsers(ActionEvent event) {
        if (CollectionUtils.isNotEmpty(targetedBrowsers) && targetedBrowsers.contains(getOperaBrowser())) {
            excludedOpera = true;
        } else {
            excludedOpera = false;
        }
        oldExcludeOpera = excludedOpera;
    }

    // sync the separate controls
    public void operaChanged() {
        if (excludedOpera) {
            if (targetedBrowsers.contains(getOperaBrowser())) {
                targetedBrowsers.add(getOperaBrowser());
            }
        } else if (targetedBrowsers.contains(getOperaBrowser())) {
            targetedBrowsers.remove(getOperaBrowser());
        }
        oldExcludeOpera = excludedOpera;
    }

    public boolean isIpAddressesListWhitelist() {
        return ipAddressesListWhitelist;
    }

    public void setIpAddressesListWhitelist(boolean ipAddressesListWhitelist) {
        this.ipAddressesListWhitelist = ipAddressesListWhitelist;
    }

    public String getIpAddressesString() {
        return ipAddressesString;
    }

    public void setIpAddressesString(String ipAddressesString) {
        this.ipAddressesString = ipAddressesString;
    }
    
    public boolean isIpAddressTargetingEnabled(){
        if (campaignDto!=null){
            return CollectionUtils.isNotEmpty(campaignDto.getCurrentSegment().getIpAddresses());
        }
        return false;
    }
    
    public String getAddIpAddressesTextArea() {
        this.addIpAddressesTextArea = this.ipAddressesString.replaceAll(", ", "\r\n");
        return addIpAddressesTextArea;
    }

    public void setAddIpAddressesTextArea(String addIpAddressesTextArea) {
        this.addIpAddressesTextArea = addIpAddressesTextArea;
    }

    public List<IpValidationResult> getIpValidationResult() {
        return ipValidationResult;
    }

    public void addIpAddresses(ActionEvent event) {
        String ipAddressesAsString = this.addIpAddressesTextArea.replaceAll("\r\n", ",");
        Set<String> newIpAddresesSet = expandIpAddressString(ipAddressesAsString);
        validateIps(newIpAddresesSet);
    }
    
    public void validateIpAddressesInputText() {
        validateIps(expandIpAddressString(this.ipAddressesString));
    }
    
    public void addNonMatchesIpAddresses(ActionEvent event){
        validateIps(expandIpValidatedAddresses());
    }

    public void validateIps(Set<String> ips) {        
        // Validating IPs Targeting
        if (validateIpAddressesAsSet(ips)) {
            this.ipAddressesString = flatIpList(ips);
        }else{
            RequestContext.getCurrentInstance().execute("nonMatchedIpAddressesDialogWidget.show()");
        }
    }

    /** Private methods **/
    private void fillConnections(SegmentDto segment) {
        if ("OPERATOR".equals(connectionType)) {
            segment.setConnectionType("OPERATOR");
        } else if ("WIFI".equals(connectionType)) {
            segment.setConnectionType("WIFI");
        } else if ("BOTH".equals(connectionType) || "NONE".equals(connectionType)) {
            segment.setConnectionType("BOTH");
        }
    }

    private void loadConnections() {
        connectionTypes = new ArrayList<String>();
        this.connectionType = campaignDto.getCurrentSegment().getConnectionType();
        if ("OPERATOR".equals(connectionType)) {
            connectionTypes.add("OPERATOR");
            connectionType = "OPERATOR";
        } else if ("WIFI".equals(connectionType)) {
            connectionTypes.add("WIFI");
            connectionType = "WIFI";
        } else if ("BOTH".equals(connectionType)) {
            if (excludedOpera || !CollectionUtils.isEmpty(mobileOperatorsList)) {
                connectionTypes.add("OPERATOR");
                connectionTypes.add("WIFI");
                connectionType = "BOTH";
            } else {
                connectionTypes.clear();
                connectionType = "NONE";
            }
        }
    }

    private BrowserDto getOperaBrowser() {
        if (operaBrowser == null) {
            operaBrowser = getToolsApplicationBean().getOperaBrowser();
        }
        return operaBrowser;
    }
    
    private String flatIpList(Set<String> ipAddressesSet) {
        return StringUtils.join(ipAddressesSet, ", ");
    }

    private boolean validateIpAddressesAsSet(Set<String> ipAddressesAsSet) {
        boolean isValid = true;
        if (CollectionUtils.isNotEmpty(ipAddressesAsSet)){
            Iterator<String> it = ipAddressesAsSet.iterator();
            ipValidationResult.clear();
            while (it.hasNext()){
                String ip = it.next();
                if (IP_ALLOWED_PATTERN.matcher(ip).matches()){
                    if (IP_PRIVATE_PATTERN.matcher(ip).matches()){
                        this.ipValidationResult.add(new IpValidationResult(ip, ValidationError.PRIVATE));
                        isValid = false;
                    }else{
                        this.ipValidationResult.add(new IpValidationResult(ip));
                    }
                }else{
                    this.ipValidationResult.add(new IpValidationResult(ip, ValidationError.FORMAT));
                    isValid = false;
                }
            }
        }
        return isValid;
    }
    
    private Set<String> expandIpAddressString(String ipAddressesString) {
        Set<String> ipAddressesSet = new HashSet<String>();
        if (StringUtils.isNotEmpty(ipAddressesString)){
            StringTokenizer st = new StringTokenizer(ipAddressesString, ","); 
            while(st.hasMoreElements()){
                ipAddressesSet.add(st.nextToken().trim());
            }
        }
        return ipAddressesSet;
    }
    
    private Set<String> expandIpValidatedAddresses() {
        Set<String> set = new HashSet<>(this.ipValidationResult.size());
        for (IpValidationResult result : this.ipValidationResult){
            if (StringUtils.isNotBlank(result.ip)){
                set.add(result.ip);
            }
        }
        return set;
    }
    
    public static class IpValidationResult{
        enum ValidationError{FORMAT, PRIVATE};
        String ip;
        Boolean valid;
        ValidationError error;
        
        public IpValidationResult(String ip) {
            super();
            this.ip = ip;
            this.valid = true;
        }

        public IpValidationResult(String ip, ValidationError error) {
            super();
            this.ip = ip;
            this.valid = false;
            this.error = error;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public Boolean getValid() {
            return valid;
        }

        public ValidationError getError() {
            return error;
        }
    }
}
