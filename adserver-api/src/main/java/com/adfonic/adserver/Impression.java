package com.adfonic.adserver;

import static com.adfonic.adserver.KryoUtils.getBigDecimal;
import static com.adfonic.adserver.KryoUtils.getByteArray;
import static com.adfonic.adserver.KryoUtils.getDate;
import static com.adfonic.adserver.KryoUtils.getDouble;
import static com.adfonic.adserver.KryoUtils.getEnum;
import static com.adfonic.adserver.KryoUtils.getInt;
import static com.adfonic.adserver.KryoUtils.getIntRange;
import static com.adfonic.adserver.KryoUtils.getLong;
import static com.adfonic.adserver.KryoUtils.getMap;
import static com.adfonic.adserver.KryoUtils.getString;
import static com.adfonic.adserver.KryoUtils.putBigDecimal;
import static com.adfonic.adserver.KryoUtils.putByteArray;
import static com.adfonic.adserver.KryoUtils.putDate;
import static com.adfonic.adserver.KryoUtils.putDouble;
import static com.adfonic.adserver.KryoUtils.putEnum;
import static com.adfonic.adserver.KryoUtils.putInt;
import static com.adfonic.adserver.KryoUtils.putIntRange;
import static com.adfonic.adserver.KryoUtils.putLong;
import static com.adfonic.adserver.KryoUtils.putMap;
import static com.adfonic.adserver.KryoUtils.putString;
import static com.adfonic.util.BitMaskUtils.isSet;
import static com.adfonic.util.BitMaskUtils.set;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adfonic.domain.Gender;
import com.adfonic.util.FastUUID;
import com.adfonic.util.Range;
import com.adfonic.util.TimeZoneUtils;
import com.adfonic.util.UrlCompressor;
import com.esotericsoftware.kryo.CustomSerialization;
import com.esotericsoftware.kryo.Kryo;

public class Impression implements CustomSerialization {
    private static final transient Logger LOG = Logger.getLogger(Impression.class.getName());

    // In order to optimize the serialized size of the Impression object, we
    // make use of a bitmask at the very beginning of the serialized form.
    // The bitmask holds flags that are used to preclude the need for "is this
    // set or is this null" bytes later on.  By packing this info into the
    // little bitmask up front we save about 24 bytes, 20%+ smaller than before.
    // These constants are indexes into the bitmask.
    // NOTE: these are package instead of private so unit tests can use them.
    static final int BITMASK_EXTERNAL_ID = 0;
    static final int BITMASK_TEST_MODE = 1;
    static final int BITMASK_TRACKING_IDENTIFIER = 2;
    static final int BITMASK_MODEL_ID = 3;
    static final int BITMASK_COUNTRY_ID = 4;
    static final int BITMASK_OPERATOR_ID = 5;
    static final int BITMASK_AGE_RANGE = 6;
    static final int BITMASK_GENDER = 7;
    static final int BITMASK_DEVICE_IDENTIFIERS = 8;
    static final int BITMASK_GEOTARGET_ID = 9;
    static final int BITMASK_INTEGRATION_TYPE_ID = 10;
    static final int BITMASK_RTB_SETTLEMENT_PRICE = 11;
    static final int BITMASK_PD_DESTINATION_URL = 12;
    static final int BITMASK_POSTAL_CODE_ID = 13;

    // New fields as of SC-134
    static final int BITMASK_RTB_BID_PRICE = 14;
    static final int BITMASK_HOST = 15;
    static final int BITMASK_USER_TIME_ZONE_ID = 16;
    static final int BITMASK_STRATEGY = 17;
    static final int BITMASK_DATE_OF_BIRTH = 18;
    static final int BITMASK_LATITUDE_LONGITUDE = 19; // one bit for both fields
    static final int BITMASK_LOCATION_SOURCE = 20;

    //New field MAD-1048
    static final int BITMASK_CAMPAIGN_DATA_FEE = 21;

    static final int BITMASK_SSL_REQUIRED = 22;
    static final int BITMASK_PRICE_BOOST = 23;
    static final int BITMASK_VIDEO_PROTOCOL = 24;

    private String externalID;
    private Date creationTime;
    private boolean testMode;
    private String trackingIdentifier;
    private long adSpaceId;
    private long creativeId;
    private Long modelId;
    private Long countryId;
    private Long operatorId;
    private Range<Integer> ageRange;
    private Gender gender;
    private Long geotargetId;
    private Long integrationTypeId;
    private Long postalCodeId;
    private Map<Long, String> deviceIdentifiers;

    // Fields representing the ProxiedDestination
    private String pdDestinationUrl;

    // Fields for RTB
    private BigDecimal rtbSettlementPrice;

    // New fields as of SC-134
    private BigDecimal rtbBidPrice;
    private String host;
    private String userTimeZoneId;
    private String strategy;
    private Date dateOfBirth;
    private Double latitude;
    private Double longitude;
    private String locationSource; // String makes this as fwd compatible as possible

    //MAD-1048 data fee
    private Long campaignDataFeeId;
    private BigDecimal priceBoost = BigDecimal.ZERO;

    private boolean sslRequired;

    private Integer videoProtocol;

    public Impression() {
        // AF-1656 - use FastUUID (non-secure Random) instead of UUID to avoid
        // synchronization blockage on SecureRandom, i.e.
        //
        // "catalina-exec-255" daemon prio=10 tid=0x00007fdb5005e000 nid=0x699c waiting for monitor entry [0x00007fdac3d3b000]
        //    java.lang.Thread.State: BLOCKED (on object monitor)
        //     at java.security.SecureRandom.nextBytes(SecureRandom.java:433)
        //     - waiting to lock <0x00000005ed68d7e0> (a java.security.SecureRandom)
        //     at java.util.UUID.randomUUID(UUID.java:162)
        //     at com.adfonic.adserver.Impression.<init>(Impression.java:76)
        //
        externalID = FastUUID.randomUUID().toString();
        creationTime = new Date();
        testMode = false;
    }

    /** This copy constructor is really only used by Click */
    protected Impression(Impression other) {
        this.externalID = other.getExternalID();
        this.creationTime = other.getCreationTime();
        this.testMode = other.isTestMode();
        this.trackingIdentifier = other.getTrackingIdentifier();
        this.adSpaceId = other.getAdSpaceId();
        this.creativeId = other.getCreativeId();
        this.modelId = other.getModelId();
        this.countryId = other.getCountryId();
        this.operatorId = other.getOperatorId();
        this.ageRange = other.getAgeRange();
        this.gender = other.getGender();
        this.geotargetId = other.getGeotargetId();
        this.integrationTypeId = other.getIntegrationTypeId();
        this.rtbSettlementPrice = other.rtbSettlementPrice;
        this.pdDestinationUrl = other.getPdDestinationUrl();
        this.postalCodeId = other.getPostalCodeId();
        if (MapUtils.isNotEmpty(other.getDeviceIdentifiers())) {
            this.deviceIdentifiers = new LinkedHashMap<Long, String>(other.getDeviceIdentifiers());
        }
        // Added as of SC-134
        this.host = other.getHost();
        this.userTimeZoneId = other.getUserTimeZoneId();
        this.rtbBidPrice = other.getRtbBidPrice();
        this.strategy = other.getStrategy();
        this.dateOfBirth = other.getDateOfBirth();
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.locationSource = other.getLocationSource();

        this.campaignDataFeeId = other.getCampaignDataFeeId(); // MAD-1048
        this.sslRequired = other.getSslRequired();
        this.priceBoost = other.priceBoost;
        this.videoProtocol = other.videoProtocol;
    }

    @Override
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

        //System.out.println("*********** reading bitmask: " + bitmask + " (" + Integer.toBinaryString(bitmask) + ")");
        testMode = isSet(bitmask, BITMASK_TEST_MODE);

        if (isSet(bitmask, BITMASK_EXTERNAL_ID)) {
            externalID = getString(buffer);
        } else {
            externalID = null; // override the one generated by the constructor
        }
        creationTime = getDate(buffer);
        if (isSet(bitmask, BITMASK_TRACKING_IDENTIFIER)) {
            trackingIdentifier = getString(buffer);
        }
        adSpaceId = getLong(buffer);
        creativeId = getLong(buffer);
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
        if (isSet(bitmask, BITMASK_RTB_SETTLEMENT_PRICE)) {
            rtbSettlementPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_PD_DESTINATION_URL)) {
            pdDestinationUrl = UrlCompressor.uncompress(getByteArray(buffer));
        }
        if (isSet(bitmask, BITMASK_POSTAL_CODE_ID)) {
            postalCodeId = getLong(buffer);
        }

        // Added as of SC-134
        if (isSet(bitmask, BITMASK_RTB_BID_PRICE)) {
            rtbBidPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_HOST)) {
            host = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_USER_TIME_ZONE_ID)) {
            userTimeZoneId = getString(buffer);
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
        if (isSet(bitmask, BITMASK_CAMPAIGN_DATA_FEE)) {
            campaignDataFeeId = getLong(buffer);
        }

        sslRequired = isSet(bitmask, BITMASK_SSL_REQUIRED);

        if (isSet(bitmask, BITMASK_PRICE_BOOST)) {
            priceBoost = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_VIDEO_PROTOCOL)) {
            videoProtocol = getInt(buffer);
        }
    }

    @Override
    public void writeObjectData(Kryo kryo, ByteBuffer buffer) {
        // Generate a bitmask with all the "is this field set?" type info
        int bitmask = 0;
        bitmask = set(bitmask, BITMASK_EXTERNAL_ID, externalID != null);
        bitmask = set(bitmask, BITMASK_TEST_MODE, testMode);
        bitmask = set(bitmask, BITMASK_TRACKING_IDENTIFIER, StringUtils.isNotEmpty(trackingIdentifier));
        bitmask = set(bitmask, BITMASK_MODEL_ID, modelId != null);
        bitmask = set(bitmask, BITMASK_COUNTRY_ID, countryId != null);
        bitmask = set(bitmask, BITMASK_OPERATOR_ID, operatorId != null);
        bitmask = set(bitmask, BITMASK_AGE_RANGE, ageRange != null);
        bitmask = set(bitmask, BITMASK_GENDER, gender != null);
        bitmask = set(bitmask, BITMASK_DEVICE_IDENTIFIERS, MapUtils.isNotEmpty(deviceIdentifiers));
        bitmask = set(bitmask, BITMASK_GEOTARGET_ID, geotargetId != null);
        bitmask = set(bitmask, BITMASK_INTEGRATION_TYPE_ID, integrationTypeId != null);
        bitmask = set(bitmask, BITMASK_RTB_SETTLEMENT_PRICE, rtbSettlementPrice != null);
        bitmask = set(bitmask, BITMASK_PD_DESTINATION_URL, pdDestinationUrl != null);
        bitmask = set(bitmask, BITMASK_POSTAL_CODE_ID, postalCodeId != null);

        // Added as of SC-134
        bitmask = set(bitmask, BITMASK_RTB_BID_PRICE, rtbBidPrice != null);
        bitmask = set(bitmask, BITMASK_HOST, host != null);
        bitmask = set(bitmask, BITMASK_USER_TIME_ZONE_ID, userTimeZoneId != null);
        bitmask = set(bitmask, BITMASK_STRATEGY, strategy != null);
        bitmask = set(bitmask, BITMASK_DATE_OF_BIRTH, dateOfBirth != null);
        bitmask = set(bitmask, BITMASK_LATITUDE_LONGITUDE, latitude != null && longitude != null);
        bitmask = set(bitmask, BITMASK_LOCATION_SOURCE, locationSource != null);

        bitmask = set(bitmask, BITMASK_CAMPAIGN_DATA_FEE, campaignDataFeeId != null); // MAD-1048
        bitmask = set(bitmask, BITMASK_SSL_REQUIRED, sslRequired);
        bitmask = set(bitmask, BITMASK_PRICE_BOOST, priceBoost != null);
        bitmask = set(bitmask, BITMASK_VIDEO_PROTOCOL, videoProtocol != null);

        //System.out.println("*********** writing bitmask: " + bitmask + " (" + Integer.toBinaryString(bitmask) + ")");

        // Write the bitmask first
        putInt(buffer, bitmask);

        // Now as we write the fields, only write the non-null ones
        if (externalID != null) {
            putString(buffer, externalID);
        }
        putDate(buffer, creationTime);

        if (StringUtils.isNotEmpty(trackingIdentifier)) {
            putString(buffer, trackingIdentifier);
        }

        putLong(buffer, adSpaceId);
        putLong(buffer, creativeId);
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
        if (rtbSettlementPrice != null) {
            putBigDecimal(buffer, rtbSettlementPrice);
        }
        if (pdDestinationUrl != null) {
            // Use the URL compressor to store this compressed
            putByteArray(buffer, UrlCompressor.compress(pdDestinationUrl));
        }
        if (postalCodeId != null) {
            putLong(buffer, postalCodeId);
        }

        // Added as of SC-134
        if (rtbBidPrice != null) {
            putBigDecimal(buffer, rtbBidPrice);
        }
        if (host != null) {
            putString(buffer, host);
        }
        if (userTimeZoneId != null) {
            putString(buffer, userTimeZoneId);
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
        if (campaignDataFeeId != null) {
            putLong(buffer, campaignDataFeeId);
        }
        if (priceBoost != null) {
            putBigDecimal(buffer, priceBoost);
        }
        if (videoProtocol != null) {
            putLong(buffer, videoProtocol);
        }
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public String getTrackingIdentifier() {
        return trackingIdentifier;
    }

    public void setTrackingIdentifier(String trackingIdentifier) {
        this.trackingIdentifier = trackingIdentifier;
    }

    public long getAdSpaceId() {
        return adSpaceId;
    }

    public void setAdSpaceId(long adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public long getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(long creativeId) {
        this.creativeId = creativeId;
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

    public BigDecimal getRtbSettlementPrice() {
        return rtbSettlementPrice;
    }

    public void setRtbSettlementPrice(BigDecimal rtbSettlementPrice) {
        this.rtbSettlementPrice = rtbSettlementPrice;
    }

    public String getPdDestinationUrl() {
        return pdDestinationUrl;
    }

    public void setPdDestinationUrl(String pdDestinationUrl) {
        this.pdDestinationUrl = pdDestinationUrl;
    }

    public boolean isProxiedDestination() {
        return pdDestinationUrl != null;
    }

    public Long getPostalCodeId() {
        return postalCodeId;
    }

    public void setPostalCodeId(Long postalCodeId) {
        this.postalCodeId = postalCodeId;
    }

    public Map<Long, String> getDeviceIdentifiers() {
        // AF-1192 - always return a non-null Map for the caller, i.e. the
        // Ad-X click redirect URL code, which doesn't want to have to null
        // check this puppy.  We can comply.
        return deviceIdentifiers == null ? Collections.EMPTY_MAP : deviceIdentifiers;
    }

    public void setDeviceIdentifiers(Map<Long, String> deviceIdentifiers) {
        if (MapUtils.isNotEmpty(deviceIdentifiers)) {
            this.deviceIdentifiers = new LinkedHashMap<Long, String>(deviceIdentifiers);
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserTimeZoneId() {
        return userTimeZoneId;
    }

    public TimeZone getUserTimeZone() {
        return TimeZoneUtils.getTimeZoneNonBlocking(userTimeZoneId);
    }

    public void setUserTimeZoneId(String userTimeZoneId) {
        this.userTimeZoneId = userTimeZoneId;
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

    public Long getCampaignDataFeeId() {
        return campaignDataFeeId;
    }

    public void setCampaignDataFeeId(Long campaignDataFeeId) {
        this.campaignDataFeeId = campaignDataFeeId;
    }

    public boolean getSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(boolean sslRequired) {
        this.sslRequired = sslRequired;
    }

    public BigDecimal getPriceBoost() {
        return priceBoost;
    }

    public void setPriceBoost(BigDecimal priceBoost) {
        this.priceBoost = priceBoost;
    }

    public Integer getVideoProtocol() {
        return videoProtocol;
    }

    public void setVideoProtocol(Integer videoProtocol) {
        this.videoProtocol = videoProtocol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof Impression) {
            Impression other = (Impression) obj;
            return StringUtils.equals(this.externalID, other.externalID) && ObjectUtils.equals(this.creationTime, other.creationTime) && this.testMode == other.testMode
                    && StringUtils.equals(this.trackingIdentifier, other.trackingIdentifier) && this.adSpaceId == other.adSpaceId && this.creativeId == other.creativeId
                    && ObjectUtils.equals(this.modelId, other.modelId) && ObjectUtils.equals(this.countryId, other.countryId)
                    && ObjectUtils.equals(this.operatorId, other.operatorId) && ObjectUtils.equals(this.ageRange, other.ageRange) && ObjectUtils.equals(this.gender, other.gender)
                    && ObjectUtils.equals(this.geotargetId, other.geotargetId)
                    && ObjectUtils.equals(this.integrationTypeId, other.integrationTypeId)
                    && ObjectUtils.equals(this.rtbSettlementPrice, other.rtbSettlementPrice)
                    && ObjectUtils.equals(this.priceBoost, other.priceBoost)
                    && ObjectUtils.equals(this.pdDestinationUrl, other.pdDestinationUrl)
                    && ObjectUtils.equals(this.postalCodeId, other.postalCodeId)
                    && ObjectUtils.equals(this.deviceIdentifiers, other.deviceIdentifiers)
                    // Added as of SC-134
                    && ObjectUtils.equals(this.rtbBidPrice, other.rtbBidPrice) && ObjectUtils.equals(this.host, other.host)
                    && ObjectUtils.equals(this.userTimeZoneId, other.userTimeZoneId) && ObjectUtils.equals(this.strategy, other.strategy)
                    && ObjectUtils.equals(this.dateOfBirth, other.dateOfBirth) && ObjectUtils.equals(this.latitude, other.latitude)
                    && ObjectUtils.equals(this.longitude, other.longitude) && ObjectUtils.equals(this.locationSource, other.locationSource)
                    //Added as of MAD-1048
                    && ObjectUtils.equals(this.campaignDataFeeId, other.campaignDataFeeId) && this.sslRequired == other.sslRequired
                    && ObjectUtils.equals(this.videoProtocol, other.videoProtocol);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        buildToString(builder);
        return builder.toString();
    }

    protected ToStringBuilder buildToString(ToStringBuilder builder) {
        return builder.append("externalID", externalID).append("creationTime", creationTime).append("testMode", testMode).append("trackingIdentifier", trackingIdentifier)
                .append("adSpaceId", adSpaceId).append("creativeId", creativeId).append("modelId", modelId).append("countryId", countryId).append("operatorId", operatorId)
                .append("ageRange", ageRange).append("gender", gender).append("geotargetId", geotargetId).append("integrationTypeId", integrationTypeId)
                .append("rtbSettlementPrice", rtbSettlementPrice).append("priceBoost", priceBoost).append("pdDestinationUrl", pdDestinationUrl)
                .append("postalCodeId", postalCodeId).append("deviceIdentifiers", deviceIdentifiers)
                // Added as of SC-134
                .append("rtbBidPrice", rtbBidPrice).append("host", host).append("userTimeZoneId", userTimeZoneId).append("strategy", strategy).append("dateOfBirth", dateOfBirth)
                .append("latitude", latitude).append("longitude", longitude).append("locationSource", locationSource)
                //Added as of MAD-1048
                .append("dataFee", campaignDataFeeId).append("sslRequired", sslRequired).append("videoProtocol", videoProtocol);
    }

    /**
     * This is our way of coping with serialized format changes.  We just
     * always keep the "last version of serialization" up to date in this
     * method, and that gives us a fallback when the current version fails.
     */
    private void readObjectDataPreviousVersion(Kryo kryo, ByteBuffer buffer) {
        // First read in the bitmask with all the "is this field set?" type info
        int bitmask = getInt(buffer);

        //System.out.println("*********** reading bitmask: " + bitmask + " (" + Integer.toBinaryString(bitmask) + ")");
        testMode = isSet(bitmask, BITMASK_TEST_MODE);

        if (isSet(bitmask, BITMASK_EXTERNAL_ID)) {
            externalID = getString(buffer);
        } else {
            externalID = null; // override the one generated by the constructor
        }
        creationTime = getDate(buffer);
        if (isSet(bitmask, BITMASK_TRACKING_IDENTIFIER)) {
            trackingIdentifier = getString(buffer);
        }
        adSpaceId = getLong(buffer);
        creativeId = getLong(buffer);
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
        if (isSet(bitmask, BITMASK_RTB_SETTLEMENT_PRICE)) {
            rtbSettlementPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_PD_DESTINATION_URL)) {
            pdDestinationUrl = UrlCompressor.uncompress(getByteArray(buffer));
        }
        if (isSet(bitmask, BITMASK_POSTAL_CODE_ID)) {
            postalCodeId = getLong(buffer);
        }

        // Added as of SC-134
        if (isSet(bitmask, BITMASK_RTB_BID_PRICE)) {
            rtbBidPrice = getBigDecimal(buffer);
        }
        if (isSet(bitmask, BITMASK_HOST)) {
            host = getString(buffer);
        }
        if (isSet(bitmask, BITMASK_USER_TIME_ZONE_ID)) {
            userTimeZoneId = getString(buffer);
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
