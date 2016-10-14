package net.byyd.archive.model.v1;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import net.byyd.archive.model.annotation.ArchiveModel;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.fasterxml.jackson.annotation.JsonProperty;

@ArchiveModel(prefix = "1")
@JsonSerialize(include = Inclusion.NON_DEFAULT)
@JsonPropertyOrder(alphabetic = true)
public class AdEvent implements Cloneable {
    public static String VERSION = "v1";

    @JsonProperty("h")
    private String host;
    @JsonProperty("t")
    private Date eventTime;
    @JsonProperty("a")
    private AdAction adAction;
    @JsonProperty("c")
    private Long creativeId;
    @JsonProperty("ca")
    private Long campaignId;
    @JsonProperty("as")
    private long adSpaceId;
    @JsonProperty("ase")
    private String adSpaceExternalId;
    @JsonProperty("p")
    private long publicationId;
    @JsonProperty("pd")
    private String publicationDomain; // website domain or application bundle
    @JsonProperty("m")
    private Long modelId;
    @JsonProperty("co")
    private Long countryId;
    @JsonProperty("op")
    private Long operatorId;
    @JsonProperty("arf")
    private Integer ageFrom;
    @JsonProperty("art")
    private Integer ageTo;
    @JsonProperty("ari")
    private boolean ageIntegral;
    @JsonProperty("g")
    private Gender gender;
    @JsonProperty("gt")
    private Long geotargetId;
    @JsonProperty("it")
    private Long integrationTypeId;
    @JsonProperty("tm")
    private boolean testMode = false;
    @JsonProperty("ia")
    private String ipAddress;
    @JsonProperty("ur")
    private UnfilledReason unfilledReason;
    @JsonProperty("ua")
    private String userAgentHeader;
    @JsonProperty("ti")
    private String trackingIdentifier;
    @JsonProperty("sp")
    private BigDecimal rtbSettlementPrice;
    @JsonProperty("pc")
    private Long postalCodeId;
    @JsonProperty("pct")
    private String postalCode;
    @JsonProperty("av")
    private Integer actionValue;
    @JsonProperty("di")
    private Map<Long, String> deviceIdentifiers;

    // New fields as of SC-134
    @JsonProperty("ieid")
    private String impressionExternalID; // was here before but now stored
                                         // first-class
    @JsonProperty("bp")
    private BigDecimal rtbBidPrice;
    @JsonProperty("ut")
    private Integer userTimeId;
    @JsonProperty("s")
    private String strategy;
    @JsonProperty("dob")
    private Date dateOfBirth;
    @JsonProperty("gla")
    private Double latitude;
    @JsonProperty("glo")
    private Double longitude;
    @JsonProperty("gls")
    private String locationSource; // String makes this as fwd compatible as
                                   // possible

    @JsonProperty("ccd")
    private Long campaignCurrentDataFeeId;
    @JsonProperty("chd")
    private Long campaignHistoryDataFeeId;

    // added accounting fields
    @JsonProperty("ior")
    private String ioReference;
    @JsonProperty("apo")
    private BigDecimal accountingPayout;
    @JsonProperty("acc")
    private BigDecimal accountingCost;
    @JsonProperty("abp")
    private BigDecimal accountingBuyerPremium;
    @JsonProperty("adc")
    private BigDecimal accountingDirectCost;
    @JsonProperty("atf")
    private BigDecimal accountingTechFee;
    @JsonProperty("adr")
    private BigDecimal accountingDataRetail;
    @JsonProperty("adw")
    private BigDecimal accountingDataWholesale;
    @JsonProperty("adm")
    private BigDecimal accountingDspMargin;
    @JsonProperty("acm")
    private BigDecimal accountingCustMargin;
    @JsonProperty("acd")
    private BigDecimal accountingCampaignDiscount;
    @JsonProperty("a3p")
    private BigDecimal accountingThirdPartyAdServing;
    @JsonProperty("acp")
    private String accountingParameters;

    @JsonProperty("ad2")
    private BigDecimal accountingDirectCostRaw;
    @JsonProperty("abb")
    private Map<String, BigDecimal> accountingBidDeductionsByyd;

    @JsonProperty("abc")
    private Map<String, BigDecimal> accountingBidDeductionsCustomer;

    @JsonProperty("abv")
    private Map<String, BigDecimal> accountingBidDeductionsVendor;

    @JsonProperty("rc")
    private long responseController;
    @JsonProperty("ro")
    private long responseOverall;

    @JsonProperty("mdr")
    private String detailReason;
    @JsonProperty("mam")
    private String additionalMessage;
    @JsonProperty("rqh")
    private String requestHost;
    @JsonProperty("rqu")
    private String requestURL;

    @JsonProperty("dti")
    private Long displayTypeId;
    @JsonProperty("fmi")
    private Long formatId;
    @JsonProperty("advi")
    private Long advertiserId;
    @JsonProperty("advc")
    private Long advertiserCompanyId;

    @JsonProperty("cri")
    private Long carrierId;
    @JsonProperty("exi")
    private Long exchangeId;
    @JsonProperty("awi")
    private Long adOpsOwnerId;
    @JsonProperty("swi")
    private Long salesOwnerId;
    @JsonProperty("bte")
    private String bidType;
    @JsonProperty("adn")
    private String adomain;
    @JsonProperty("iul")
    private String iurl;
    @JsonProperty("clu")
    private String clickUrlCookie;
    @JsonProperty("hrf")
    private String headerReferrer;
    @JsonProperty("gpc")
    private String geoPostalCode;
    @JsonProperty("gcn")
    private String geoCountry;
    @JsonProperty("gct")
    private String geoCity;
    @JsonProperty("grn")
    private String geoRegion;
    @JsonProperty("gtp")
    private Long geoType;
    @JsonProperty("rtb")
    private Long rtb = 1L;
    @JsonProperty("app")
    private Long application = 1L;
    @JsonProperty("uct")
    private String userCity;
    @JsonProperty("ucr")
    private String userCountry;

    private String messageHash;

    @JsonProperty("sv")
    private String serverName;
    @JsonProperty("sr")
    private String shard;
    //Added for MAD-3118, click fields needed on datacollector
    @JsonProperty("pdu")
    private String pDestinationurl;
    @JsonProperty("ssl")
    private boolean sslRequired;
    private String rawMessage;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public AdAction getAdAction() {
        return adAction;
    }

    public void setAdAction(AdAction adAction) {
        this.adAction = adAction;
    }

    public Long getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(Long creativeId) {
        this.creativeId = creativeId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public long getAdSpaceId() {
        return adSpaceId;
    }

    public void setAdSpaceId(long adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(long publicationId) {
        this.publicationId = publicationId;
    }

    public void setPublicationDomain(String publicationDomain) {
        this.publicationDomain = publicationDomain;
    }

    public String getPublicationDomain() {
        return publicationDomain;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(Integer ageFrom) {
        this.ageFrom = ageFrom;
    }

    public Integer getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(Integer ageTo) {
        this.ageTo = ageTo;
    }

    public boolean isAgeIntegral() {
        return ageIntegral;
    }

    public void setAgeIntegral(boolean ageIntegral) {
        this.ageIntegral = ageIntegral;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Long getGeotargetId() {
        return geotargetId;
    }

    public void setGeotargetId(Long geotargetId) {
        this.geotargetId = geotargetId;
    }

    public Long getIntegrationTypeId() {
        return integrationTypeId;
    }

    public void setIntegrationTypeId(Long integrationTypeId) {
        this.integrationTypeId = integrationTypeId;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public UnfilledReason getUnfilledReason() {
        return unfilledReason;
    }

    public void setUnfilledReason(UnfilledReason unfilledReason) {
        this.unfilledReason = unfilledReason;
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }

    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }

    public String getTrackingIdentifier() {
        return trackingIdentifier;
    }

    public void setTrackingIdentifier(String trackingIdentifier) {
        this.trackingIdentifier = trackingIdentifier;
    }

    public BigDecimal getRtbSettlementPrice() {
        return rtbSettlementPrice;
    }

    public void setRtbSettlementPrice(BigDecimal rtbSettlementPrice) {
        this.rtbSettlementPrice = rtbSettlementPrice;
    }

    public Long getPostalCodeId() {
        return postalCodeId;
    }

    public void setPostalCodeId(Long postalCodeId) {
        this.postalCodeId = postalCodeId;
    }

    public Integer getActionValue() {
        return actionValue;
    }

    public void setActionValue(Integer actionValue) {
        this.actionValue = actionValue;
    }

    public Map<Long, String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(Map<Long, String> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public String getImpressionExternalID() {
        return impressionExternalID;
    }

    public void setImpressionExternalID(String impressionExternalID) {
        this.impressionExternalID = impressionExternalID;
    }

    public BigDecimal getRtbBidPrice() {
        return rtbBidPrice;
    }

    public void setRtbBidPrice(BigDecimal rtbBidPrice) {
        this.rtbBidPrice = rtbBidPrice;
    }

    public Integer getUserTimeId() {
        return userTimeId;
    }

    public void setUserTimeId(Integer userTimeId) {
        this.userTimeId = userTimeId;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationSource() {
        return locationSource;
    }

    public void setLocationSource(String locationSource) {
        this.locationSource = locationSource;
    }

    public Long getCampaignCurrentDataFeeId() {
        return campaignCurrentDataFeeId;
    }

    public void setCampaignCurrentDataFeeId(Long campaignCurrentDataFeeId) {
        this.campaignCurrentDataFeeId = campaignCurrentDataFeeId;
    }

    public Long getCampaignHistoryDataFeeId() {
        return campaignHistoryDataFeeId;
    }

    public void setCampaignHistoryDataFeeId(Long campaignHistoryDataFeeId) {
        this.campaignHistoryDataFeeId = campaignHistoryDataFeeId;
    }

    public BigDecimal getAccountingPayout() {
        return accountingPayout;
    }

    public void setAccountingPayout(BigDecimal accountingPayout) {
        this.accountingPayout = accountingPayout;
    }

    public BigDecimal getAccountingBuyerPremium() {
        return accountingBuyerPremium;
    }

    public void setAccountingBuyerPremium(BigDecimal accountingBuyerPremium) {
        this.accountingBuyerPremium = accountingBuyerPremium;
    }

    public BigDecimal getAccountingDirectCost() {
        return accountingDirectCost;
    }

    public void setAccountingDirectCost(BigDecimal accountingDirectCost) {
        this.accountingDirectCost = accountingDirectCost;
    }

    public BigDecimal getAccountingTechFee() {
        return accountingTechFee;
    }

    public void setAccountingTechFee(BigDecimal accountingTechFee) {
        this.accountingTechFee = accountingTechFee;
    }

    public BigDecimal getAccountingDataRetail() {
        return accountingDataRetail;
    }

    public void setAccountingDataRetail(BigDecimal accountingDataRetail) {
        this.accountingDataRetail = accountingDataRetail;
    }

    public BigDecimal getAccountingDataWholesale() {
        return accountingDataWholesale;
    }

    public void setAccountingDataWholesale(BigDecimal accountingDataWholesale) {
        this.accountingDataWholesale = accountingDataWholesale;
    }

    public BigDecimal getAccountingDspMargin() {
        return accountingDspMargin;
    }

    public void setAccountingDspMargin(BigDecimal accountingDspMargin) {
        this.accountingDspMargin = accountingDspMargin;
    }

    public BigDecimal getAccountingCustMargin() {
        return accountingCustMargin;
    }

    public void setAccountingCustMargin(BigDecimal accountingCustMargin) {
        this.accountingCustMargin = accountingCustMargin;
    }

    public BigDecimal getAccountingCampaignDiscount() {
        return accountingCampaignDiscount;
    }

    public void setAccountingCampaignDiscount(BigDecimal accountingCampaignDiscount) {
        this.accountingCampaignDiscount = accountingCampaignDiscount;
    }

    public long longgetResponseController() {
        return responseController;
    }

    public void setResponseController(long responseController) {
        this.responseController = responseController;
    }

    public long getResponseOverall() {
        return responseOverall;
    }

    public void setResponseOverall(long responseOverall) {
        this.responseOverall = responseOverall;
    }

    public String getDetailReason() {
        return detailReason;
    }

    public void setDetailReason(String detailReason) {
        this.detailReason = detailReason;
    }

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }

    public long getResponseController() {
        return responseController;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public Long getDisplayTypeId() {
        return displayTypeId;
    }

    public void setDisplayTypeId(Long displayTypeId) {
        this.displayTypeId = displayTypeId;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getIoReference() {
        return ioReference;
    }

    public void setIoReference(String ioReference) {
        this.ioReference = ioReference;
    }

    public Long getAdOpsOwnerId() {
        return adOpsOwnerId;
    }

    public void setAdOpsOwnerId(Long adOpsOwnerId) {
        this.adOpsOwnerId = adOpsOwnerId;
    }

    public Long getSalesOwnerId() {
        return salesOwnerId;
    }

    public void setSalesOwnerId(Long salesOwnerId) {
        this.salesOwnerId = salesOwnerId;
    }

    public String getBidType() {
        return bidType;
    }

    public void setBidType(String bidType) {
        this.bidType = bidType;
    }

    public String getAdomain() {
        return adomain;
    }

    public void setAdomain(String adomain) {
        this.adomain = adomain;
    }

    public String getIurl() {
        return iurl;
    }

    public void setIurl(String iurl) {
        this.iurl = iurl;
    }

    public String getClickUrlCookie() {
        return clickUrlCookie;
    }

    public void setClickUrlCookie(String clickUrlCookie) {
        this.clickUrlCookie = clickUrlCookie;
    }

    public String getHeaderReferrer() {
        return headerReferrer;
    }

    public void setHeaderReferrer(String headerReferrer) {
        this.headerReferrer = headerReferrer;
    }

    public Long getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(Long advertiserId) {
        this.advertiserId = advertiserId;
    }

    public String getGeoPostalCode() {
        return geoPostalCode;
    }

    public void setGeoPostalCode(String geoPostalCode) {
        this.geoPostalCode = geoPostalCode;
    }

    public String getGeoCountry() {
        return geoCountry;
    }

    public void setGeoCountry(String geoCountry) {
        this.geoCountry = geoCountry;
    }

    public String getGeoCity() {
        return geoCity;
    }

    public void setGeoCity(String geoCity) {
        this.geoCity = geoCity;
    }

    public String getGeoRegion() {
        return geoRegion;
    }

    public void setGeoRegion(String geoRegion) {
        this.geoRegion = geoRegion;
    }

    public Long getGeoType() {
        return geoType;
    }

    public void setGeoType(Long geoType) {
        this.geoType = geoType;
    }

    public String getAdSpaceExternalId() {
        return adSpaceExternalId;
    }

    public void setAdSpaceExternalId(String adSpaceExternalId) {
        this.adSpaceExternalId = adSpaceExternalId;
    }

    public Long getAdvertiserCompanyId() {
        return advertiserCompanyId;
    }

    public void setAdvertiserCompanyId(Long advertiserCompanyId) {
        this.advertiserCompanyId = advertiserCompanyId;
    }

    public BigDecimal getAccountingCost() {
        return accountingCost;
    }

    public void setAccountingCost(BigDecimal accountingCost) {
        this.accountingCost = accountingCost;
    }

    public Long getRtb() {
        return rtb;
    }

    public void setRtb(Long rtb) {
        this.rtb = rtb;
    }

    public void clearAccountingNumberZeros() {
        accountingPayout = clearZero(accountingPayout);
        accountingCost = clearZero(accountingCost);
        accountingBuyerPremium = clearZero(accountingBuyerPremium);
        accountingDirectCost = clearZero(accountingDirectCost);
        accountingTechFee = clearZero(accountingTechFee);
        accountingDataRetail = clearZero(accountingDataRetail);
        accountingDataWholesale = clearZero(accountingDataWholesale);
        accountingDspMargin = clearZero(accountingDspMargin);
        accountingCustMargin = clearZero(accountingCustMargin);
        accountingCampaignDiscount = clearZero(accountingCampaignDiscount);
        accountingThirdPartyAdServing = clearZero(accountingThirdPartyAdServing);
    }

    private BigDecimal clearZero(BigDecimal num) {
        BigDecimal c = num != null ? num.add(BigDecimal.ZERO) : BigDecimal.ZERO;
        c.round(new MathContext(12, RoundingMode.DOWN));
        return c.signum() == 0 ? null : num;
    }

    public Long getApplication() {
        return application;
    }

    public void setApplication(Long application) {
        this.application = application;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public void setMessageHash(String messageHash) {
        this.messageHash = messageHash;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Object copy() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean isNoCreative() {
        return detailReason != null && detailReason.toLowerCase().contains("no") && detailReason.toLowerCase().contains("creative");
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public BigDecimal getAccountingThirdPartyAdServing() {
        return accountingThirdPartyAdServing;
    }

    public void setAccountingThirdPartyAdServing(BigDecimal accountingThirdPartyAdServing) {
        this.accountingThirdPartyAdServing = accountingThirdPartyAdServing;
    }

    public Map<String, BigDecimal> getAccountingBidDeductionsByyd() {
        return accountingBidDeductionsByyd;
    }

    public void setAccountingBidDeductionsByyd(Map<String, BigDecimal> accountingBidDeductionsByyd) {
        this.accountingBidDeductionsByyd = accountingBidDeductionsByyd;
    }

    public Map<String, BigDecimal> getAccountingBidDeductionsCustomer() {
        return accountingBidDeductionsCustomer;
    }

    public void setAccountingBidDeductionsCustomer(Map<String, BigDecimal> accountingBidDeductionsCustomer) {
        this.accountingBidDeductionsCustomer = accountingBidDeductionsCustomer;
    }

    public Map<String, BigDecimal> getAccountingBidDeductionsVendor() {
        return accountingBidDeductionsVendor;
    }

    public void setAccountingBidDeductionsVendor(Map<String, BigDecimal> accountingBidDeductionsVendor) {
        this.accountingBidDeductionsVendor = accountingBidDeductionsVendor;
    }

    public BigDecimal getAccountingDirectCostRaw() {
        return accountingDirectCostRaw;
    }

    public void setAccountingDirectCostRaw(BigDecimal accountingDirectCostRaw) {
        this.accountingDirectCostRaw = accountingDirectCostRaw;
    }

    public String getAccountingParameters() {
        return accountingParameters;
    }

    public void setAccountingParameters(String accountingParameters) {
        this.accountingParameters = accountingParameters;
    }

    public String getpDestinationurl() {
        return pDestinationurl;
    }

    public void setpDestinationurl(String pDestinationurl) {
        this.pDestinationurl = pDestinationurl;
    }

    public boolean getSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(boolean sslRequired) {
        this.sslRequired = sslRequired;
    }

}
