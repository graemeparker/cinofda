package com.adfonic.tools.beans.application;

import static com.adfonic.domain.DeviceGroup.DEVICE_GROUP_MOBILE_SYSTEM_NAME;
import static com.adfonic.domain.DeviceGroup.DEVICE_GROUP_TABLET_SYSTEM_NAME;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.jsf.FacesContextUtils;

import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.dto.campaign.creative.CreativeAttributeDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.browser.BrowserService;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.devicegroup.DeviceGroupService;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.security.SecurityUtils;

/***
 * Bean defined in file context to inject the maps
 *
 * */
public class ToolsApplicationBean extends GenericAbstractBean {

    private static final int BYTES_IN_KB = 1024;
    private Map<String, String> datePickerPresetsMap;
    private Map<String, String> optimisationDatePickerPresetsMap;
    private Map<String, String> externalUrls = new HashMap<String, String>();
    private Collection<BrowserDto> browsers;
    private BrowserDto operaBrowser;
    private DeviceGroupDto tabletDeviceGroup;
    private DeviceGroupDto mobileDeviceGroup;
    private List<DeviceIdentifierTypeDto> audienceDeviceIdentifierTypes;
    private List<CreativeAttributeDto> creativeAttributes;

    /** Should be keep in sync with 'audienceTypeOptions' on section_source.xhtml */
    private static final String[] AUDIENCE_DEVICE_IDENTIFIER_TYPES = {
            DeviceIdentifierType.SYSTEM_NAME_IFA,   DeviceIdentifierType.SYSTEM_NAME_ATID,      DeviceIdentifierType.SYSTEM_NAME_HIFA,
            DeviceIdentifierType.SYSTEM_NAME_ADID,  DeviceIdentifierType.SYSTEM_NAME_ADID_MD5,  DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5 };

    @Value("${toolsApplicationBean.advertiserOptimisationEnabled:true}")
    private boolean advertiserOptimisationEnabled;

    // Audience tab and campaign workflow audience section
    @Value("${toolsApplicationBean.audienceEnabled:true}")
    private boolean audienceEnabled;

    //  default audience recency
    @Value("${toolsApplicationBean.defaultAudienceRecency:60}")
    private int defaultAudienceRecency;

    @Value("${toolsApplicationBean.companyName}")
    private String companyName;

    @Value("${toolsApplicationBean.companyLegalName}")
    private String companyLegalName;
    
    // Show on Map functionality is only available for Location Audiences with up to this number
    @Value("${toolsApplicationBean.locationAudienceCoordsLimit:100}")
    private int locationAudienceCoordsLimit;
    
    // Add Bid deductions is only available on Bidding page up to this number
    @Value("${toolsApplicationBean.bidDeductionsLimit:10}")
    private int bidDeductionsLimit;
    
    @Override
    protected void init() {
        // nothing here
    }

    public Map<String, String> getDatePickerPresetsMap() {
        final Map<String, String> map = new LinkedHashMap<String, String>(0);
        if (!CollectionUtils.isEmpty(datePickerPresetsMap)) {
            Iterator<Entry<String, String>> bundleEntriesIt = datePickerPresetsMap.entrySet().iterator();
            while (bundleEntriesIt.hasNext()) {
                Entry<String, String> entry = bundleEntriesIt.next();
                String localized = FacesUtils.getLocalizedMessage(entry.getKey());
                map.put(localized, entry.getValue());
            }
        }
        return map;
    }

    public void setDatePickerPresetsMap(final Map<String, String> datePickerPresetsMap) {
        this.datePickerPresetsMap = datePickerPresetsMap;
    }

    public Map<String, String> getOptimisationDatePickerPresetsMap() {
        final Map<String, String> map = new LinkedHashMap<String, String>(0);
        if (!CollectionUtils.isEmpty(optimisationDatePickerPresetsMap)) {
            Iterator<Entry<String, String>> bundleEntriesIt = optimisationDatePickerPresetsMap.entrySet().iterator();
            while (bundleEntriesIt.hasNext()) {
                Entry<String, String> entry = bundleEntriesIt.next();
                String localized = FacesUtils.getLocalizedMessage(entry.getKey());
                map.put(localized, entry.getValue());
            }
        }
        return map;
    }

    public void setOptimisationDatePickerPresetsMap(final Map<String, String> optimisationDatePickerPresetsMap) {
        this.optimisationDatePickerPresetsMap = optimisationDatePickerPresetsMap;
    }

    public double bytesToKilobytes(Integer bytes) {
        return bytes / BYTES_IN_KB;
    }

    public double bytesToMegabytes(Integer bytes) {
        return bytes / (BYTES_IN_KB * BYTES_IN_KB);
    }

    public CampaignStatus[] getCampaignStatus() {
        return CampaignStatus.values();
    }

    public BidType[] getBidType() {
        return com.adfonic.dto.campaign.enums.BidType.values();
    }

    public PublicationStatus[] getPublicationStatus() {
        return com.adfonic.dto.publication.enums.PublicationStatus.values();
    }

    public AdvertiserStatus[] getAdvertiserStatus() {
        return com.adfonic.dto.advertiser.enums.AdvertiserStatus.values();
    }

    public Approval[] getApproval() {
        return Approval.values();
    }

    public Backfill[] getBackfill() {
        return Backfill.values();
    }

    public DestinationType[] getDestinationTypes() {
        return com.adfonic.dto.campaign.enums.DestinationType.values();
    }

    public String shortMessage(String message, int chars) {
        return Utils.shortMessage(message, chars);
    }

    public String wrapText(String message, int chars) {
        return Utils.wrapText(message, chars);
    }

    public Map<String, String> getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(Map<String, String> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public String getTimeZoneHour(Date date) {

        CompanyService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                com.adfonic.presentation.company.CompanyService.class);

        TimeZone tz = service.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());

        Date startDate = DateUtils.getTimezoneDate(date, tz);

        int minutes = DateUtils.getMinuteOffset(startDate);
        return getHourAtGivenMinutes(minutes);
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public Date getTimeZoneDate(Date date) {
        if (date == null) {
            return null;
        }
        CompanyService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                com.adfonic.presentation.company.CompanyService.class);
        TimeZone tz = service.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
        return DateUtils.getTimezoneDate(date, tz);
    }

    public String getHourAtGivenMinutes(Integer minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
    }

    public String encodeUrl(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8");
    }

    public Collection<BrowserDto> getBrowsers() {
        if (browsers == null) {
            BrowserService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.campaign.browser.BrowserService.class);
            browsers = service.getAllBrowsers();
        }
        return browsers;
    }

    public BrowserDto getOperaBrowser() {
        if (this.operaBrowser == null) {
            BrowserService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.campaign.browser.BrowserService.class);
            this.operaBrowser = service.getOperaBrowser();
        }
        return operaBrowser;
    }

    public boolean isAdvertiserOptimisationEnabled() {
        return advertiserOptimisationEnabled;
    }

    public void setAdvertiserOptimisationEnabled(boolean advertiserOptimisationEnabled) {
        this.advertiserOptimisationEnabled = advertiserOptimisationEnabled;
    }

    public boolean isAudienceEnabled() {
        return audienceEnabled;
    }

    public void setAudienceEnabled(boolean audienceEnabled) {
        this.audienceEnabled = audienceEnabled;
    }

    public DeviceGroupDto getTabletDeviceGroup() {
        if (this.tabletDeviceGroup == null) {
            DeviceGroupService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.devicegroup.DeviceGroupService.class);
            this.tabletDeviceGroup = service.getDeviceGroupBySystemName(DEVICE_GROUP_TABLET_SYSTEM_NAME);
        }
        return this.tabletDeviceGroup;
    }

    public DeviceGroupDto getMobileDeviceGroup() {
        if (this.mobileDeviceGroup == null) {
            DeviceGroupService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.devicegroup.DeviceGroupService.class);
            this.mobileDeviceGroup = service.getDeviceGroupBySystemName(DEVICE_GROUP_MOBILE_SYSTEM_NAME);
        }
        return this.mobileDeviceGroup;
    }

    public List<DeviceIdentifierTypeDto> getAudienceDeviceIdentifierTypes() {
        if (this.audienceDeviceIdentifierTypes == null) {
            List<DeviceIdentifierTypeDto> items = new LinkedList<DeviceIdentifierTypeDto>();
            DeviceIdentifierService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.deviceidentifier.DeviceIdentifierService.class);
            for (String ditName : AUDIENCE_DEVICE_IDENTIFIER_TYPES) {
                items.add(service.getDeviceIdentifierTypeBySystemName(ditName));
            }
            this.audienceDeviceIdentifierTypes = items;
        }
        return this.audienceDeviceIdentifierTypes;
    }
    
    public int getDefaultAudienceRecency() {
        return defaultAudienceRecency;
    }

    public void setDefaultAudienceRecency(int defaultAudienceRecency) {
        this.defaultAudienceRecency = defaultAudienceRecency;
    }

    public boolean hasCompanyRole(String role) {
        List<String> roles = new ArrayList<String>(0);
        roles.add(role);
        return SecurityUtils.hasUserRoles(roles);
    }

    public List<CreativeAttributeDto> getCreativeAttributes() {
        if (this.creativeAttributes == null) {
            CreativeService service = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance()).getBean(
                    com.adfonic.presentation.campaign.creative.CreativeService.class);
            this.creativeAttributes = service.getAllCreativeAttributes();
        }
        return this.creativeAttributes;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public int getLocationAudienceCoordsLimit() {
        return locationAudienceCoordsLimit;
    }

    public void setLocationAudienceCoordsLimit(int locationAudienceCoordsLimit) {
        this.locationAudienceCoordsLimit = locationAudienceCoordsLimit;
    }

	public int getBidDeductionsLimit() {
		return bidDeductionsLimit;
	}

	public void setBidDeductionsLimit(int bidDeductionsLimit) {
		this.bidDeductionsLimit = bidDeductionsLimit;
	}
    
}
