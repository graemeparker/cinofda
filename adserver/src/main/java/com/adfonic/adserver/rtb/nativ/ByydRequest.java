package com.adfonic.adserver.rtb.nativ;

import java.util.List;

import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public class ByydRequest extends ByydBase {

    private String id;

    private Long tmax;

    private String publicationRtbId;

    private Medium medium = Medium.SITE;

    private String publicationUrlString;

    // Publication (App/Site) categories (IAB codes)
    private List<String> iabIds;

    private String publicationName;

    //OpenRTB v1 publication string
    private String pub;

    private ByydDevice device = ByydDevice.EMPTY;

    private ByydUser user = ByydUser.EMPTY;

    private List<String> blockedCategoryIabIds;

    private List<String> blockedAdvertiserDomains;

    private List<String> blockedLanguageIsoCodes;

    private List<String> acceptedLanguageIsoCodes;

    private ByydImp imp;

    private boolean blockPlugins; // AdX only thing

    private boolean testMode;

    private ByydMarketPlace marketPlace;

    private List<String> currencies;

    private String fallbackPublicationRtbId;

    private String associateReference;
    private Integer sellerNetworkId = null;

    private String publisherExternalId;

    private String bundleName; // App bundle

    private AdSpaceDto adSpace;

    private boolean trackingDisabled = false;

    protected ByydRequest() {
        //marshalling friendly
    }

    public ByydRequest(String publisherExternalId, String requestId) {
        this.publisherExternalId = publisherExternalId;
        this.id = requestId;
    }

    public String getId() {
        return id;
    }

    public Long getTmax() {
        return tmax;
    }

    public void setTmax(Long tmax) {
        this.tmax = tmax;
    }

    public ByydDevice getDevice() {
        return device;
    }

    public void setDevice(ByydDevice device) {
        this.device = device;
    }

    public ByydUser getUser() {
        return user;
    }

    public void setUser(ByydUser user) {
        this.user = user;
    }

    public List<String> getBlockedCategoryIabIds() {
        return blockedCategoryIabIds;
    }

    public void setBlockedCategoryIabIds(List<String> blockedCategoryIabIds) {
        this.blockedCategoryIabIds = blockedCategoryIabIds;
    }

    public List<String> getBlockedAdvertiserDomains() {
        return blockedAdvertiserDomains;
    }

    public void setBlockedAdvertiserDomains(List<String> blockedAdvertiserDomains) {
        this.blockedAdvertiserDomains = blockedAdvertiserDomains;
    }

    public List<String> getBlockedLanguageIsoCodes() {
        return blockedLanguageIsoCodes;
    }

    public void setBlockedLanguageIsoCodes(List<String> blockedLanguageIsoCodes) {
        this.blockedLanguageIsoCodes = blockedLanguageIsoCodes;
    }

    public List<String> getAcceptedLanguageIsoCodes() {
        return acceptedLanguageIsoCodes;
    }

    public void setAcceptedLanguageIsoCodes(List<String> acceptedLanguageIsoCodes) {
        this.acceptedLanguageIsoCodes = acceptedLanguageIsoCodes;
    }

    public ByydImp getImp() {
        return imp;
    }

    public void setImp(ByydImp imp) {
        this.imp = imp;
    }

    public String getPublicationRtbId() {
        return publicationRtbId;
    }

    public void setPublicationRtbId(String publicationRtbId) {
        this.publicationRtbId = publicationRtbId;
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public String getPublicationUrlString() {
        return publicationUrlString;
    }

    public void setPublicationUrlString(String publicationUrlString) {
        this.publicationUrlString = publicationUrlString;
    }

    public List<String> getIabIds() {
        return iabIds;
    }

    public void setIabIds(List<String> iabIds) {
        this.iabIds = iabIds;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String siteOrAppName) {
        this.publicationName = siteOrAppName;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    //TODO - temporary arrangement till BidResponse format is set
    private boolean includeDestination = false;

    private boolean useOnlyRealDestination;

    public boolean doIncludeDestination() {
        return includeDestination;
    }

    public void doIncludeDestination(boolean includeDestination) {
        this.includeDestination = includeDestination;
    }

    public boolean arePluginsBlocked() {
        return blockPlugins;
    }

    public void blockPlugins() {
        this.blockPlugins = true;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public ByydMarketPlace getMarketPlace() {
        return marketPlace;
    }

    public void setMarketPlace(ByydMarketPlace seat) {
        this.marketPlace = seat;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }

    public boolean useOnlyRealDestination() {
        return useOnlyRealDestination;
    }

    public void setUseOnlyRealDestination(boolean useOnlyRealDestination) {
        this.useOnlyRealDestination = useOnlyRealDestination;
    }

    public String getFallbackPublicationRtbId() {
        return fallbackPublicationRtbId;
    }

    public void setFallbackPublicationRtbId(String fallbackPublicationRtbId) {
        this.fallbackPublicationRtbId = fallbackPublicationRtbId;
    }

    public String getAssociateReference() {
        return associateReference;
    }

    public void setAssociateReference(String associateReference) {
        this.associateReference = associateReference;
    }

    public String getPublisherExternalId() {
        return publisherExternalId;
    }

    public Integer getSellerNetworkId() {
        return sellerNetworkId;
    }

    public void setSellerNetworkId(Integer sellerNetworkId) {
        this.sellerNetworkId = sellerNetworkId;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public boolean isTrackingDisabled() {
        return trackingDisabled;
    }

    public void setTrackingDisabled(boolean trackingDisabled) {
        this.trackingDisabled = trackingDisabled;
    }

    public void setAdSpace(AdSpaceDto adSpace) {
        this.adSpace = adSpace;
    }

    public AdSpaceDto getAdSpace() {
        return adSpace;
    }
}
