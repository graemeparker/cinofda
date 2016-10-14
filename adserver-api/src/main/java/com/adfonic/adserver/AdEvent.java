package com.adfonic.adserver;

import static com.adfonic.adserver.KryoUtils.*;
import static com.adfonic.util.BitMaskUtils.*;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.FastDateFormat;

import com.adfonic.domain.AdAction;
import com.adfonic.domain.Gender;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.util.CsvUtils;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;

import com.esotericsoftware.kryo.CustomSerialization;
import com.esotericsoftware.kryo.Kryo;

public class AdEvent implements CustomSerialization {
    private static final transient Logger LOG = Logger.getLogger(AdEvent.class.getName());

    // In order to optimize the serialized size of the Impression object, we
    // make use of a bitmask at the very beginning of the serialized form.
    // The bitmask holds flags that are used to preclude the need for "is this
    // set or is this null" bytes later on.  By packing this info into the
    // little bitmask up front we save about 24 bytes, 20%+ smaller than before.
    // These constants are indexes into the bitmask.
    // NOTE: these are package instead of private so unit tests can use them.
    static final int BITMASK_HOST = 0;
    static final int BITMASK_CREATIVE_ID = 1;
    static final int BITMASK_CAMPAIGN_ID = 2;
    static final int BITMASK_MODEL_ID = 3;
    static final int BITMASK_COUNTRY_ID = 4;
    static final int BITMASK_OPERATOR_ID = 5;
    static final int BITMASK_AGE_RANGE = 6;
    static final int BITMASK_GENDER = 7;
    static final int BITMASK_DEVICE_IDENTIFIERS = 8;
    static final int BITMASK_GEOTARGET_ID = 9;
    static final int BITMASK_INTEGRATION_TYPE_ID = 10;
    static final int BITMASK_TEST_MODE = 11;
    static final int BITMASK_UNFILLED_REASON = 12;
    static final int BITMASK_USER_AGENT_HEADER = 13;
    static final int BITMASK_TRACKING_IDENTIFIER = 14;
    static final int BITMASK_RTB_SETTLEMENT_PRICE = 15;
    static final int BITMASK_POSTAL_CODE_ID = 16;
    static final int BITMASK_ACTION_VALUE = 17;

    // New fields as of SC-134
    static final int BITMASK_IMPRESSION_EXTERNAL_ID = 18;
    static final int BITMASK_RTB_BID_PRICE = 19;
    static final int BITMASK_USER_TIME_ID = 20;
    static final int BITMASK_STRATEGY = 21;
    static final int BITMASK_DATE_OF_BIRTH = 22;
    static final int BITMASK_LATITUDE_LONGITUDE = 23; // one bit for both fields
    static final int BITMASK_LOCATION_SOURCE = 24;
    
    //New field as of MAD-1048
    static final int BITMASK_DATA_FEE = 25;
    
    // Stored fields
    private String host;
    private Date eventTime;
    private AdAction adAction;
    private Long creativeId;
    private Long campaignId;
    private long adSpaceId;
    private long publicationId;
    private Long modelId;
    private Long countryId;
    private Long operatorId;
    private Range<Integer> ageRange;
    private Gender gender;
    private Long geotargetId;
    private Long integrationTypeId;
    private boolean testMode = false;
    private String ipAddress;
    private UnfilledReason unfilledReason;
    private String userAgentHeader;
    private String trackingIdentifier;
    private BigDecimal rtbSettlementPrice;
    private Long postalCodeId;
    private Integer actionValue;
    private Map<Long,String> deviceIdentifiers;

    // New fields as of SC-134
    private String impressionExternalID; // was here before but now stored first-class
    private BigDecimal rtbBidPrice;
    private Integer userTimeId;
    private String strategy;
    private Date dateOfBirth;
    private Double latitude;
    private Double longitude;
    private String locationSource; // String makes this as fwd compatible as possible
    
    //New field as of MAD-1048
    private Long campaignHistoryDataFeeId;
    
    /**
     * Empty constructor.  This version of the constructor should never be used
     * directly.
     *
     * NOTE: You should use AdEventFactory.newInstance if you're creating an
     * instance of AdEvent for the purposes of populating the fields yourself.
     *
     * This default constructor exists only for kryo deserialization and for "from CSV"
     * operations.  Kryo deserialization instantiates using Class.newInstance, and
     * then it populates the fields values, so this constructor does not set any field
     * values (that would be wasteful), and needs to be as efficient as possible.
     *
     * This constructor does not initialize any variables such as adAction, host,
     * or eventTime, which do get initialized by AdEventFactory.newInstance.
     *
     * Technically we could get away with making this default constructor private,
     * enforcing a better contract with users of this class, and kryo can deal.
     * But...it falls back on catching the newInstance exception and grabbing the
     * declared constructor and doing setAccessible(true) and then calling its
     * newInstance method.  Unfortunately, that has to happen *every* time a new
     * instance is needed.  I tested it for speed, and when this constructor was
     * public, it took 107ms to instantiate 10m instances.  When this constructor
     * was private, it took 27 seconds!!!  So yeah, I'm leaving this public for
     * performance reasons...DON'T USE IT!
     */
    public AdEvent() {}

    /**
     * Normal constructor.  This version of the constructor will initialize
     * the values of adAction, host, and eventTime.  This gets invoked by
     * AdEventFactory.newInstance.
     */
    /*package */ AdEvent(AdAction adAction, String host, Date eventTime, TimeZone userTimeZone) {
        this.adAction = adAction;
        this.host = host;
        this.eventTime = eventTime;
        if (userTimeZone != null) {
            this.userTimeId = DateUtils.getTimeID(eventTime, userTimeZone);
        }
    }

    /**
     * Copy constructor, used only by AdEventFactory.cloneAdEvent.  Not exposed
     * directly (package access only), so as to make mock testing easier.
     */
    /*package*/ AdEvent(AdEvent copyMe) {
        this.host = copyMe.host;
        this.eventTime = copyMe.eventTime;
        this.adAction = copyMe.adAction;
        this.creativeId = copyMe.creativeId;
        this.campaignId = copyMe.campaignId;
        this.adSpaceId = copyMe.adSpaceId;
        this.publicationId = copyMe.publicationId;
        this.modelId = copyMe.modelId;
        this.countryId = copyMe.countryId;
        this.operatorId = copyMe.operatorId;
        if (copyMe.ageRange != null) {
            this.ageRange = new Range<Integer>(copyMe.ageRange);
        }
        this.gender = copyMe.gender;
        this.geotargetId = copyMe.geotargetId;
        this.integrationTypeId = copyMe.integrationTypeId;
        this.testMode = copyMe.testMode;
        this.ipAddress = copyMe.ipAddress;
        this.unfilledReason = copyMe.unfilledReason;
        this.userAgentHeader = copyMe.userAgentHeader;
        this.trackingIdentifier = copyMe.trackingIdentifier;
        this.rtbSettlementPrice = copyMe.rtbSettlementPrice;
        this.postalCodeId = copyMe.postalCodeId;
        this.actionValue = copyMe.actionValue;
        if (MapUtils.isNotEmpty(copyMe.deviceIdentifiers)) {
            this.deviceIdentifiers = new LinkedHashMap<Long,String>(copyMe.deviceIdentifiers);
        }
        this.impressionExternalID = copyMe.impressionExternalID;

        // As of SC-134
        this.rtbBidPrice = copyMe.rtbBidPrice;
        this.userTimeId = copyMe.userTimeId;
        this.strategy = copyMe.strategy;
        this.dateOfBirth = copyMe.dateOfBirth;
        this.latitude = copyMe.latitude;
        this.longitude = copyMe.longitude;
        this.locationSource = copyMe.locationSource;
        
        //As of MAD-1048
        this.campaignHistoryDataFeeId = copyMe.campaignHistoryDataFeeId;
    }
    
    public void readObjectData(Kryo kryo, ByteBuffer buffer) {
        // We jump through a couple of hoops to do our best to deserialize this
        // puppy even if it's not the current "version" of the object in our eyes.
        int initialPosition = buffer.position();
        try {
            // First try the current version...
            readObjectDataCurrentVersion(kryo, buffer);
        } catch (Exception e) {
            // No joy.  Let's assume this serialized form is from the
            // last version of this object.
            LOG.warning("Deserialization failed due to: " + e.getMessage() + ", falling back on previous version");
            buffer.position(initialPosition); // rewind to the initial position
            readObjectDataPreviousVersion(kryo, buffer);
        }
    }

    private void readObjectDataCurrentVersion(Kryo kryo, ByteBuffer buffer) {
        // First read in the bitmask with all the "is this field set?" type info
        int bitmask = getInt(buffer);

        testMode = isSet(bitmask, BITMASK_TEST_MODE);

        if (isSet(bitmask, BITMASK_HOST)) {
            host = getString(buffer);
        }
        eventTime = getDate(buffer);
        adAction = getEnum(buffer, AdAction.class);
        if (isSet(bitmask, BITMASK_CREATIVE_ID)) {
            creativeId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_CAMPAIGN_ID)) {
            campaignId = getLong(buffer);
        }
        adSpaceId = getLong(buffer);
        publicationId = getLong(buffer);
        if (isSet(bitmask, BITMASK_MODEL_ID)) {
            modelId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_COUNTRY_ID)) {
            countryId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_OPERATOR_ID)) {
            operatorId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_AGE_RANGE)) {
            ageRange = getIntRange(buffer);
        }
        if (isSet(bitmask, BITMASK_GENDER)) {
            gender = getEnum(buffer, Gender.class);
        }
        if (isSet(bitmask, BITMASK_DEVICE_IDENTIFIERS)) {
            deviceIdentifiers = getMap(kryo, buffer, LinkedHashMap.class, Long.class, String.class);
        }
        if (isSet(bitmask, BITMASK_GEOTARGET_ID)) {
            geotargetId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_INTEGRATION_TYPE_ID)) {
            integrationTypeId = getLong(buffer);
        }
        ipAddress = getString(buffer);
        if (isSet(bitmask, BITMASK_UNFILLED_REASON)) {
            unfilledReason = getEnum(buffer, UnfilledReason.class);
        }
        if (isSet(bitmask, BITMASK_USER_AGENT_HEADER)) {
            userAgentHeader = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_TRACKING_IDENTIFIER)) {
            trackingIdentifier = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_RTB_SETTLEMENT_PRICE)) {
            rtbSettlementPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_POSTAL_CODE_ID)) {
            postalCodeId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_ACTION_VALUE)) {
            actionValue = getInt(buffer);
        }

        // As of SC-134
        if (isSet(bitmask, BITMASK_IMPRESSION_EXTERNAL_ID)) {
            impressionExternalID = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_RTB_BID_PRICE)) {
            rtbBidPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_USER_TIME_ID)) {
            userTimeId = getInt(buffer);
        }
        if (isSet(bitmask, BITMASK_STRATEGY)) {
            strategy = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_DATE_OF_BIRTH)) {
            dateOfBirth = getDate(buffer);
        }
        if (isSet(bitmask, BITMASK_LATITUDE_LONGITUDE)) {
            latitude = getDouble(buffer);
            longitude = getDouble(buffer);
        }
        if (isSet(bitmask, BITMASK_LOCATION_SOURCE)) {
            locationSource = getString(buffer);
        }
        
        // As of MAD-1048
        if (isSet(bitmask, BITMASK_DATA_FEE)) {
            campaignHistoryDataFeeId = getLong(buffer);
        }
        
    }

    public void writeObjectData(Kryo kryo, ByteBuffer buffer) {
        // Generate a bitmask with all the "is this field set?" type info
        int bitmask = 0;
        bitmask = set(bitmask, BITMASK_HOST, host != null);
        bitmask = set(bitmask, BITMASK_CREATIVE_ID, creativeId != null);
        bitmask = set(bitmask, BITMASK_CAMPAIGN_ID, campaignId != null);
        bitmask = set(bitmask, BITMASK_MODEL_ID, modelId != null);
        bitmask = set(bitmask, BITMASK_COUNTRY_ID, countryId != null);
        bitmask = set(bitmask, BITMASK_OPERATOR_ID, operatorId != null);
        bitmask = set(bitmask, BITMASK_AGE_RANGE, ageRange != null);
        bitmask = set(bitmask, BITMASK_GENDER, gender != null);
        bitmask = set(bitmask, BITMASK_DEVICE_IDENTIFIERS, MapUtils.isNotEmpty(deviceIdentifiers));
        bitmask = set(bitmask, BITMASK_GEOTARGET_ID, geotargetId != null);
        bitmask = set(bitmask, BITMASK_INTEGRATION_TYPE_ID, integrationTypeId != null);
        bitmask = set(bitmask, BITMASK_TEST_MODE, testMode);
        bitmask = set(bitmask, BITMASK_UNFILLED_REASON, unfilledReason != null);
        bitmask = set(bitmask, BITMASK_USER_AGENT_HEADER, userAgentHeader != null);
        bitmask = set(bitmask, BITMASK_TRACKING_IDENTIFIER, StringUtils.isNotBlank(trackingIdentifier));
        bitmask = set(bitmask, BITMASK_RTB_SETTLEMENT_PRICE, rtbSettlementPrice != null);
        bitmask = set(bitmask, BITMASK_POSTAL_CODE_ID, postalCodeId != null);
        bitmask = set(bitmask, BITMASK_ACTION_VALUE, actionValue != null);

        // As of SC-134
        bitmask = set(bitmask, BITMASK_IMPRESSION_EXTERNAL_ID, impressionExternalID != null);
        bitmask = set(bitmask, BITMASK_RTB_BID_PRICE, rtbBidPrice != null);
        bitmask = set(bitmask, BITMASK_USER_TIME_ID, userTimeId != null);
        bitmask = set(bitmask, BITMASK_STRATEGY, strategy != null);
        bitmask = set(bitmask, BITMASK_DATE_OF_BIRTH, dateOfBirth != null);
        bitmask = set(bitmask, BITMASK_LATITUDE_LONGITUDE, latitude != null && longitude != null);
        bitmask = set(bitmask, BITMASK_LOCATION_SOURCE, locationSource != null);
        
        // As of MAD-1048
        bitmask = set(bitmask, BITMASK_DATA_FEE, campaignHistoryDataFeeId != null);

        // Write the bitmask first
        putInt(buffer, bitmask);

        if (host != null) {
            putString(buffer, host);
        }
        putDate(buffer, eventTime);
        putEnum(buffer, adAction);
        if (creativeId != null) {
            putLong(buffer, creativeId);
        }
        if (campaignId != null) {
            putLong(buffer, campaignId);
        }
        putLong(buffer, adSpaceId);
        putLong(buffer, publicationId);
        if (modelId != null) {
            putLong(buffer, modelId);
        }
        if (countryId != null) {
            putLong(buffer, countryId);
        }
        if (operatorId != null) {
            putLong(buffer, operatorId);
        }
        if (ageRange != null) {
            putIntRange(buffer, ageRange);
        }
        if (gender != null) {
            putEnum(buffer, gender);
        }
        if (MapUtils.isNotEmpty(deviceIdentifiers)) {
            putMap(kryo, buffer, deviceIdentifiers, Long.class, String.class);
        }
        if (geotargetId != null) {
            putLong(buffer, geotargetId);
        }
        if (integrationTypeId != null) {
            putLong(buffer, integrationTypeId);
        }
        putString(buffer, ipAddress);
        if (unfilledReason != null) {
            putEnum(buffer, unfilledReason);
        }
        if (userAgentHeader != null) {
            putString(buffer, userAgentHeader);
        }
        if (StringUtils.isNotBlank(trackingIdentifier)) {
            putString(buffer, trackingIdentifier);
        }
        if (rtbSettlementPrice != null) {
            putBigDecimal(buffer, rtbSettlementPrice);
        }
        if (postalCodeId != null) {
            putLong(buffer, postalCodeId);
        }
        if (actionValue != null) {
            putInt(buffer, actionValue);
        }
        
        // As of SC-134
        if (impressionExternalID != null) {
            putString(buffer, impressionExternalID);
        }
        if (rtbBidPrice != null) {
            putBigDecimal(buffer, rtbBidPrice);
        }
        if (userTimeId != null) {
            putInt(buffer, userTimeId);
        }
        if (strategy != null) {
            putString(buffer, strategy);
        }
        if (dateOfBirth != null) {
            putDate(buffer, dateOfBirth);
        }
        if (latitude != null && longitude != null) {
            putDouble(buffer, latitude);
            putDouble(buffer, longitude);
        }
        if (locationSource != null) {
            putString(buffer, locationSource);
        }
        
        // As of MAD-1048
        if (campaignHistoryDataFeeId != null) {
            putLong(buffer, campaignHistoryDataFeeId);
        }
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public Date getEventTime() {
        return eventTime;
    }
    public void setEventTime(Date eventTime, TimeZone userTimeZone) {
        if (eventTime == null) {
            this.eventTime = null;
            this.userTimeId = null;
        } else {
            this.eventTime = new Date(eventTime.getTime());
            if (userTimeZone != null) {
                this.userTimeId = DateUtils.getTimeID(eventTime, userTimeZone);
            }
        }
    }
    
    public void populateEventTime(Date eventTime, Integer userTimeId){
        this.eventTime = eventTime;
        this.userTimeId = userTimeId;
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

    public Range<Integer> getAgeRange() {
        return ageRange;
    }
    public void setAgeRange(Range<Integer> ageRange) {
        this.ageRange = ageRange;
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

    public boolean isRtb() {
        return rtbBidPrice != null;
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

    public String getImpressionExternalID() {
        return impressionExternalID;
    }
    public void setImpressionExternalID(String impressionExternalID) {
        this.impressionExternalID = impressionExternalID;
    }

    public Map<Long,String> getDeviceIdentifiers() {
        return deviceIdentifiers == null ? Collections.EMPTY_MAP : deviceIdentifiers;
    }
    public void setDeviceIdentifiers(Map<Long,String> deviceIdentifiers) {
        if (MapUtils.isNotEmpty(deviceIdentifiers)) {
            this.deviceIdentifiers = new LinkedHashMap<Long,String>(deviceIdentifiers);
        } else {
            this.deviceIdentifiers = null;
        }
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

    public Long getCampaignHistoryDataFeeId() {
        return campaignHistoryDataFeeId;
    }

    public void setCampaignHistoryDataFeeId(Long campaignHistoryDataFee) {
        this.campaignHistoryDataFeeId = campaignHistoryDataFee;
    }

    public void populate(Impression impression, String ipAddress, String userAgentHeader, Long campaignId, long publicationId) {
        this.testMode = impression.isTestMode();
        this.trackingIdentifier = impression.getTrackingIdentifier();
        this.adSpaceId = impression.getAdSpaceId();
        this.creativeId = impression.getCreativeId() == 0 ? null : impression.getCreativeId();
        this.modelId = impression.getModelId();
        this.countryId = impression.getCountryId();
        this.operatorId = impression.getOperatorId();
        this.ageRange = impression.getAgeRange();
        this.gender = impression.getGender();
        this.geotargetId = impression.getGeotargetId();
        this.integrationTypeId = impression.getIntegrationTypeId();
        this.impressionExternalID = impression.getExternalID();
        this.rtbSettlementPrice = impression.getRtbSettlementPrice();
        this.postalCodeId = impression.getPostalCodeId();
        if (MapUtils.isNotEmpty(impression.getDeviceIdentifiers())) {
            this.deviceIdentifiers = new LinkedHashMap<Long,String>(impression.getDeviceIdentifiers());
        } else {
            this.deviceIdentifiers = null;
        }

        // As of SC-134
        this.rtbBidPrice = impression.getRtbBidPrice();
        this.host = impression.getHost();
        this.strategy = impression.getStrategy();
        this.dateOfBirth = impression.getDateOfBirth();
        this.latitude = impression.getLatitude();
        this.longitude = impression.getLongitude();
        this.locationSource = impression.getLocationSource();
        
        // As of MAD-1048
        this.campaignHistoryDataFeeId = impression.getCampaignDataFeeId();

        // Determine userTimeId based on the Impression's userTimeZoneId, if present
        TimeZone userTimeZone = impression.getUserTimeZone();
        if (userTimeZone != null) {
            this.userTimeId = DateUtils.getTimeID(eventTime, userTimeZone);
        }

        // These are passed directly, not on the Impression object
        this.ipAddress = ipAddress;
        this.userAgentHeader = userAgentHeader;
        this.campaignId = campaignId;
        this.publicationId = publicationId;
    }

    public void populate(Click click, Long campaignId, Long publicationId) {
        populate(click, click.getIpAddress(), click.getUserAgentHeader(), campaignId, publicationId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("host", host)
            .append("eventTime", eventTime)
            .append("adAction", adAction)
            .append("creativeId", creativeId)
            .append("campaignId", campaignId)
            .append("adSpaceId", adSpaceId)
            .append("publicationId", publicationId)
            .append("modelId", modelId)
            .append("countryId", countryId)
            .append("operatorId", operatorId)
            .append("ageRange", ageRange)
            .append("gender", gender)
            .append("geotargetId", geotargetId)
            .append("integrationTypeId", integrationTypeId)
            .append("testMode", testMode)
            .append("ipAddress", ipAddress)
            .append("unfilledReason", unfilledReason)
            .append("userAgentHeader", userAgentHeader)
            .append("trackingIdentifier", trackingIdentifier)
            .append("rtbSettlementPrice", rtbSettlementPrice)
            .append("postalCodeId", postalCodeId)
            .append("actionValue", actionValue)
            .append("deviceIdentifiers", deviceIdentifiers)
            .append("impressionExternalID", impressionExternalID)
            // Added as of SC-134
            .append("rtbBidPrice", rtbBidPrice)
            .append("userTimeId", userTimeId)
            .append("strategy", strategy)
            .append("dateOfBirth", dateOfBirth)
            .append("latitude", latitude)
            .append("longitude", longitude)
            .append("locationSource", locationSource)
            //Added as of MAD-1048
            .append("campaignHistoryDataFee",campaignHistoryDataFeeId)
            .toString();
    }

    private static final String EVENT_TIME_FORMAT = "yyyyMMddHHmmss"; //.SSS
    private static final FastDateFormat eventTimeFormatter = FastDateFormat.getInstance(EVENT_TIME_FORMAT);

    private static final String DATE_OF_BIRTH_FORMAT = "yyyyMMdd";
    private static final FastDateFormat DATE_OF_BIRTH_FORMATTER = FastDateFormat.getInstance(DATE_OF_BIRTH_FORMAT);

    public String toCsv() {
        StringBuilder bld = new StringBuilder();
        bld.append(eventTimeFormatter.format(eventTime))
            .append(',')
            .append(csv(adAction))
            .append(',')
            .append(csv(creativeId))
            .append(',')
            .append(csv(campaignId))
            .append(',')
            .append(String.valueOf(adSpaceId))
            .append(',')
            .append(String.valueOf(publicationId))
            .append(',')
            .append(csv(modelId))
            .append(',')
            .append(csv(countryId))
            .append(',')
            .append(csv(operatorId))
            .append(',')
            .append(CsvUtils.escape(ageRange))
            .append(',')
            .append(csv(gender))
            .append(',')
            .append(csv(geotargetId))
            .append(',')
            .append(csv(integrationTypeId))
            .append(',')
            .append(testMode ? "1" : "0")
            .append(',')
            .append(CsvUtils.escape(ipAddress))
            .append(',')
            .append(csv(unfilledReason))
            .append(',')
            .append(CsvUtils.escape(userAgentHeader))
            .append(',')
            .append(CsvUtils.escape(trackingIdentifier))
            .append(',')
            .append(CsvUtils.escape(impressionExternalID))
            .append(',')
            .append(csv(rtbSettlementPrice))
            .append(',')
            .append(csv(postalCodeId))
            .append(',')
            .append(csv(actionValue))
            // Added as of SC-134
            .append(',')
            .append(csv(rtbBidPrice))
            .append(',')
            .append(CsvUtils.escape(host))
            .append(',')
            .append(csv(userTimeId))
            .append(',')
            .append(CsvUtils.escape(strategy))
            .append(',')
            .append(csvDateOfBirth(dateOfBirth))
            .append(',')
            .append(csv(latitude))
            .append(',')
            .append(csv(longitude))
            .append(',')
            .append(CsvUtils.escape(locationSource))
            // Added as of MAD-1048
            .append(',')
            .append(csv(campaignHistoryDataFeeId))
            ;
        return bld.toString();
    }

    public static AdEvent fromCsv(String[] line) throws java.text.ParseException {
        final AdEvent msg = new AdEvent();
        int p = 0;
        msg.setEventTime(new SimpleDateFormat(EVENT_TIME_FORMAT).parse(line[p++]), null);
        msg.setAdAction(AdAction.valueOf(line[p++]));
        msg.setCreativeId(makeLong(nullable(line[p++])));
        msg.setCampaignId(makeLong(nullable(line[p++])));
        msg.setAdSpaceId(makeLong(nullable(line[p++])));
        msg.setPublicationId(makeLong(line[p++]));
        msg.setModelId(makeLong(nullable(line[p++])));
        msg.setCountryId(makeLong(nullable(line[p++])));
        msg.setOperatorId(makeLong(nullable(line[p++])));
        msg.setAgeRange(makeIntRange(nullable(line[p++])));
        String v = nullable(line[p++]);
        msg.setGender(v == null ? null : Gender.valueOf(v));
        msg.setGeotargetId(makeLong(nullable(line[p++])));
        msg.setIntegrationTypeId(makeLong(nullable(line[p++])));
        try {
            msg.setTestMode(Integer.valueOf(line[p++]) == 1);
        } catch (NumberFormatException e) {
            // Try the old format, which was the Boolean string
            --p;
            msg.setTestMode(Boolean.valueOf(line[p++]));
        }
        msg.setIpAddress(line[p++]);
        v = nullable(line[p++]);
        msg.setUnfilledReason(v == null ? null : UnfilledReason.valueOf(v));
        msg.setUserAgentHeader(nullable(line[p++]));
        msg.setTrackingIdentifier(nullable(line[p++]));
        msg.setImpressionExternalID(nullable(line[p++]));
        msg.setRtbSettlementPrice(makeBigDecimal(nullable(line[p++])));
        msg.setPostalCodeId(makeLong(nullable(line[p++])));
        msg.setActionValue(makeInt(nullable(line[p++])));
        // Added as of SC-134
        msg.setRtbBidPrice(makeBigDecimal(nullable(line[p++])));
        msg.setHost(nullable(line[p++]));
        msg.setUserTimeId(makeInt(nullable(line[p++])));
        msg.setStrategy(nullable(line[p++]));
        msg.setDateOfBirth(makeDateOfBirth(nullable(line[p++])));
        msg.setLatitude(makeDouble(nullable(line[p++])));
        msg.setLongitude(makeDouble(nullable(line[p++])));
        msg.setLocationSource(nullable(line[p++]));
        // Added as of MAD-1048
        msg.setCampaignHistoryDataFeeId(makeLong(line[p++]));
        return msg;
    }

    private static String csv(Number value) {
        return value == null ? "" : value.toString();
    }

    private static <T extends Enum> String csv(T value) {
        return value == null ? "" : value.name();
    }

    private static String csvDateOfBirth(Date dateOfBirth) {
        return dateOfBirth == null ? "" : DATE_OF_BIRTH_FORMATTER.format(dateOfBirth);
    }

    /** Converts the empty string to null */
    private static String nullable(String value) {
        return "".equals(value) ? null : value;
    }
    
    private static Integer makeInt(String value) {
        return value == null ? null : Integer.valueOf(value);
    }

    private static Long makeLong(String value) {
        return value == null ? null : Long.valueOf(value);
    }

    private static BigDecimal makeBigDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private static Range<Integer> makeIntRange(String value) {
        if (value == null) return null;
        int pos = value.indexOf('-');
        if (pos > 0) {
            return new Range<Integer>(Integer.valueOf(value.substring(0, pos)), Integer.valueOf(value.substring(pos + 1)));
        } else {
            return new Range<Integer>(Integer.valueOf(value));
        }
    }

    private static Double makeDouble(String value) {
        return value == null ? null : Double.valueOf(value);
    }

    private static Date makeDateOfBirth(String value) throws java.text.ParseException {
        if (value == null) {
            return null;
        } else {
            return new SimpleDateFormat(DATE_OF_BIRTH_FORMAT).parse(value);
        }
    }

    private void readObjectDataPreviousVersion(Kryo kryo, ByteBuffer buffer) {
     // First read in the bitmask with all the "is this field set?" type info
        int bitmask = getInt(buffer);

        testMode = isSet(bitmask, BITMASK_TEST_MODE);

        if (isSet(bitmask, BITMASK_HOST)) {
            host = getString(buffer);
        }
        eventTime = getDate(buffer);
        adAction = getEnum(buffer, AdAction.class);
        if (isSet(bitmask, BITMASK_CREATIVE_ID)) {
            creativeId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_CAMPAIGN_ID)) {
            campaignId = getLong(buffer);
        }
        adSpaceId = getLong(buffer);
        publicationId = getLong(buffer);
        if (isSet(bitmask, BITMASK_MODEL_ID)) {
            modelId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_COUNTRY_ID)) {
            countryId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_OPERATOR_ID)) {
            operatorId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_AGE_RANGE)) {
            ageRange = getIntRange(buffer);
        }
        if (isSet(bitmask, BITMASK_GENDER)) {
            gender = getEnum(buffer, Gender.class);
        }
        if (isSet(bitmask, BITMASK_DEVICE_IDENTIFIERS)) {
            deviceIdentifiers = getMap(kryo, buffer, LinkedHashMap.class, Long.class, String.class);
        }
        if (isSet(bitmask, BITMASK_GEOTARGET_ID)) {
            geotargetId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_INTEGRATION_TYPE_ID)) {
            integrationTypeId = getLong(buffer);
        }
        ipAddress = getString(buffer);
        if (isSet(bitmask, BITMASK_UNFILLED_REASON)) {
            unfilledReason = getEnum(buffer, UnfilledReason.class);
        }
        if (isSet(bitmask, BITMASK_USER_AGENT_HEADER)) {
            userAgentHeader = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_TRACKING_IDENTIFIER)) {
            trackingIdentifier = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_RTB_SETTLEMENT_PRICE)) {
            rtbSettlementPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_POSTAL_CODE_ID)) {
            postalCodeId = getLong(buffer);
        }
        if (isSet(bitmask, BITMASK_ACTION_VALUE)) {
            actionValue = getInt(buffer);
        }

        // As of SC-134
        if (isSet(bitmask, BITMASK_IMPRESSION_EXTERNAL_ID)) {
            impressionExternalID = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_RTB_BID_PRICE)) {
            rtbBidPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_USER_TIME_ID)) {
            userTimeId = getInt(buffer);
        }
        if (isSet(bitmask, BITMASK_STRATEGY)) {
            strategy = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_DATE_OF_BIRTH)) {
            dateOfBirth = getDate(buffer);
        }
        if (isSet(bitmask, BITMASK_LATITUDE_LONGITUDE)) {
            latitude = getDouble(buffer);
            longitude = getDouble(buffer);
        }
        if (isSet(bitmask, BITMASK_LOCATION_SOURCE)) {
            locationSource = getString(buffer);
        }
    }
}
