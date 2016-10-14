package com.adfonic.adserver.rtb.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.AdType;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.adserver.rtb.openx.OpenX.AdId;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.geo.SimpleCoordinates;
import com.byyd.adx.AdxOpenRtbMapper;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

/**
 * 
 * http://docs.openx.com/ad_exchange_adv/#ox_rtb_api.html
 *
 */
@SuppressWarnings("unchecked")
public class OpenXMapper extends ProtoBufMapper {

    private static final transient Logger LOG = Logger.getLogger(OpenXMapper.class.getName());

    private static final OpenXMapper instance = new OpenXMapper();

    public static OpenXMapper instance() {
        return instance;
    }

    enum CreativeType {
        _0_UNUSED, IMAGE, FLASH, TEXT, VIDEO, DHTML, //
        _6_UNUSED, AUDIO_USER_INITIATED, _8_UNUSED, _9_UNUSED, //
        VIDEO_AUTO_PLAY, AUDIO_AUTO_PLAY, ROTATING_CREATIVES;

        public static CreativeType getByOrdinal(int ordinal) {
            CreativeType[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return null;
            } else {
                return values[ordinal];
            }
        }
    }

    private static final Set<String> LANG_OTHER = Collections.singleton("other");

    private static final Map<APIFramework, ContentForm> API_FRAMEWORK_CONTENT_FORM_MAP = ImmutableMap.of(//
            APIFramework.MRAID, ContentForm.MRAID_1_0,// 
            APIFramework.ORMMA, ContentForm.ORMMA_LEVEL1);

    // Category mappings both ways
    private final Map<Integer, Set<String>> iabIdsByOpenXCategoryId;
    private final Map<String, Set<Integer>> openXCategoryIdsByIabId;

    // Content mappings both ways
    private final Map<Integer, Set<String>> iabIdsByOpenXContentId;
    private final Map<String, Set<Integer>> openXContentIdsByIabId;

    public static OpenX.BidRequest protoText2Request(String protoText) throws ParseException {
        return protoText2Builder(protoText).build();
    }

    public static OpenX.BidRequest.Builder protoText2Builder(String protoText) throws ParseException {
        OpenX.BidRequest.Builder builder = OpenX.BidRequest.newBuilder();
        TextFormat.merge(protoText, builder);
        return builder;
    }

    public static String request2ProtoText(OpenX.BidRequest request) {
        return TextFormat.printToUnicodeString(request);
    }

    public static byte[] protoText2Bytes(String protoText) throws IOException {
        OpenX.BidRequest bidRequest = protoText2Request(protoText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bidRequest.writeTo(baos);
        return baos.toByteArray();
    }

    private OpenXMapper() {
        this.iabIdsByOpenXCategoryId = AdxOpenRtbMapper.convertToIntegerToStringSetMap(AdxOpenRtbMapper.loadProperties("OpenX/openx-category-to-iabid.properties"));
        this.openXCategoryIdsByIabId = AdxOpenRtbMapper.convertToStringToIntegerSetMap(AdxOpenRtbMapper.loadProperties("OpenX/adfonic-iabid-to-openx-category.properties"));
        this.openXContentIdsByIabId = AdxOpenRtbMapper.convertToStringToIntegerSetMap(AdxOpenRtbMapper.loadProperties("OpenX/adfonic-iabid-to-openx-content.properties"));

        // Build the reverse mapping
        this.iabIdsByOpenXContentId = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : this.openXContentIdsByIabId.entrySet()) {
            for (Integer openXContentId : entry.getValue()) {
                Set<String> iabIds = iabIdsByOpenXContentId.get(openXContentId);
                if (iabIds == null) {
                    iabIds = new HashSet<>();
                    iabIdsByOpenXContentId.put(openXContentId, iabIds);
                }
                iabIds.add(entry.getKey());
            }
        }
    }

    public ByydRequest mapRequest(String publisherExternalId, OpenX.BidRequest openxRequest) throws NoBidException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Request  >>\n" + TextFormat.printToString(openxRequest));
        }

        String requestId = openxRequest.getAuctionId();
        ByydRequest byydRequest = new ByydRequest(publisherExternalId, requestId);
        if (requestId == null || requestId.length() == 0) {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "auctionId", Level.WARNING);
        }

        String userAgent = digUserAgent(openxRequest);
        if (StringUtils.isEmpty(userAgent)) {
            abort(byydRequest, AdSrvCounter.MISS_UA, Level.WARNING);
        }

        String ip = digIpAddress(openxRequest, byydRequest);
        if (StringUtils.isEmpty(ip)) {
            abort(byydRequest, AdSrvCounter.MISS_IP, Level.INFO);
        }

        List<OpenX.AdId> adIds = openxRequest.getMatchingAdIdsList();
        if (adIds == null || adIds.isEmpty()) {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "matching_ad_ids", Level.INFO);
        }

        mapBidRequest(openxRequest, byydRequest);

        ByydDevice byydDevice = new ByydDevice();
        byydDevice.setUserAgent(userAgent);
        byydDevice.setIp(ip);

        byydRequest.setDevice(byydDevice);

        ByydUser user = mapUser(openxRequest);
        byydRequest.setUser(user);

        Set<ContentForm> contentForms = null;

        if (openxRequest.hasDevice()) {
            //OpenX device is not mandatory
            OpenX.Device device = openxRequest.getDevice();

            if (isInitialized(device)) {

                mapDevice(byydDevice, device, openxRequest.getTpKeyValList());

                if (device.hasGeo()) {
                    OpenX.Geo geo = device.getGeo();
                    if (isInitialized(geo)) {
                        if (geo.hasLat() && geo.hasLon()) {
                            byydDevice.setCoordinates(new SimpleCoordinates(geo.getLat(), geo.getLon()));
                        }
                        if (StringUtils.isNotBlank(geo.getCountry())) {
                            // OpenX provides country codes lowercase, we need to uppercase
                            user.setCountryCode(geo.getCountry().toUpperCase());
                        }
                        if (StringUtils.isNotBlank(geo.getZip())) {
                            user.setPostalCode(geo.getZip());
                        }
                        if (StringUtils.isNotBlank(geo.getState())) {
                            // Our AbstractGeoDeriver can deal with name or abbrev (SC-284)
                            user.setState(geo.getState());
                        }
                        if (geo.hasDma()) {
                            user.setDma(String.valueOf(geo.getDma()));
                        }
                    }
                }
                contentForms = mapRichMediaApi(device);
            }
        }

        // The "pub_blocked_type" list contains a mix of metaphors.  Some apply
        // to AdType, some apply to extended creatives, and some apply to
        // destination types.  Process each blocked type as applicable.
        Set<AdType> blockedAdTypes = null;
        boolean blockExtendedCreatives = false;
        Set<DestinationType> blockedDestinationTypes = null;
        if (openxRequest.getPubBlockedTypeCount() > 0) {
            for (Integer ordinal : openxRequest.getPubBlockedTypeList()) {
                CreativeType creativeType = CreativeType.getByOrdinal(ordinal);
                if (creativeType != null) {
                    switch (creativeType) {
                    case IMAGE:
                        blockedAdTypes = addToSet(blockedAdTypes, AdType.XHTML_BANNER_AD);
                        break;
                    case TEXT:
                        blockedAdTypes = addToSet(blockedAdTypes, AdType.XHTML_TEXT_AD);
                        break;
                    case DHTML:
                        blockExtendedCreatives = true;
                        break;
                    case VIDEO:
                        blockedDestinationTypes = addToSet(blockedDestinationTypes, DestinationType.VIDEO);
                        break;
                    case AUDIO_USER_INITIATED:
                        blockedDestinationTypes = addToSet(blockedDestinationTypes, DestinationType.AUDIO);
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        // Do not iterate adIds. We can bid on only one imp anyway...
        AdId adId = adIds.get(0);
        ByydImp byydImp = new ByydImp(String.valueOf(0));
        byydImp.setBtypeDefault(blockedAdTypes);
        byydImp.setbDestTypes(blockedDestinationTypes);
        byydImp.setBlockExtendedCreatives(blockExtendedCreatives);
        if (contentForms != null) {
            contentForms.addAll(ByydImp.CF_MOBILE_WEB);
            byydImp.setContentFormWhiteList(contentForms);
        }

        mapImp(byydImp, adId, openxRequest);
        byydRequest.setImp(byydImp);

        // Required in order to populate "click_through_urls" on the response side
        byydRequest.doIncludeDestination(true);

        return byydRequest;
    }

    void mapBidRequest(OpenX.BidRequest opnexRequest, ByydRequest byydRequest) throws NoBidException {

        byydRequest.setTestMode(opnexRequest.getIsTest());
        byydRequest.setPublicationRtbId("PID-" + opnexRequest.getPubWebsiteId());

        if (opnexRequest.getIsApplication()) {
            byydRequest.setMedium(Medium.APPLICATION);
        } else {
            byydRequest.setMedium(Medium.SITE);
        }

        //Apps - url is store url
        String url = opnexRequest.getUrl();
        if (StringUtils.isNotEmpty(url)) {
            url = url.trim();
            if (url.length() > MAX_URL_LENGTH) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Truncating url with length=" + url.length() + " at " + MAX_URL_LENGTH);
                }
                url = url.substring(0, MAX_URL_LENGTH);
            }
            byydRequest.setPublicationUrlString(url);
            try {
                byydRequest.setPublicationName(new URI(url).getHost());
            } catch (URISyntaxException e) {
                abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unparseable OpenX url " + url, Level.FINE); // sometimes space character in the middle of url... 
            }
        }

        if (opnexRequest.getUserLangCount() > 0) {
            byydRequest.setAcceptedLanguageIsoCodes(ListUtils.removeAll(opnexRequest.getUserLangList(), LANG_OTHER));
        }

        if (opnexRequest.getPubBlockedAdLanguagesCount() > 0) {
            byydRequest.setBlockedLanguageIsoCodes(ListUtils.removeAll(opnexRequest.getPubBlockedAdLanguagesList(), LANG_OTHER));
        }

        mapBlockedData(opnexRequest, byydRequest);
    }

    private void mapBlockedData(OpenX.BidRequest opnexRequest, ByydRequest byydRequest) {
        List<String> blockedIabIds = null;

        if (opnexRequest.getPubBlockedCatCount() > 0) {
            // Convert OpenX category ids to Category.iabId values
            blockedIabIds = new ArrayList<String>();
            for (Integer openXCategoryId : opnexRequest.getPubBlockedCatList()) {
                Set<String> iabIds = iabIdsByOpenXCategoryId.get(openXCategoryId);
                if (CollectionUtils.isNotEmpty(iabIds)) {
                    blockedIabIds.addAll(iabIds);
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("OpenX category has no IAB id mappings: " + openXCategoryId);
                }
            }
        }

        // Make sure Publication.statedCategories get set
        Set<String> statedIabIds = null;
        if (opnexRequest.hasOxCatTier1()) {
            Set<String> tier1IabIds = iabIdsByOpenXCategoryId.get(opnexRequest.getOxCatTier1());
            if (CollectionUtils.isNotEmpty(tier1IabIds)) {
                statedIabIds = addToSet(statedIabIds, tier1IabIds);
            }
        }
        if (opnexRequest.hasOxCatTier2()) {
            Set<String> tier2IabIds = iabIdsByOpenXCategoryId.get(opnexRequest.getOxCatTier2());
            if (CollectionUtils.isNotEmpty(tier2IabIds)) {
                statedIabIds = addToSet(statedIabIds, tier2IabIds);
            }
        }
        if (CollectionUtils.isNotEmpty(statedIabIds)) {
            byydRequest.setIabIds(new ArrayList<String>(statedIabIds));
        }

        if (opnexRequest.getPubBlockedContentCount() > 0) {
            // Convert OpenX content ids to Category.iabId values
            if (blockedIabIds == null) {
                blockedIabIds = new ArrayList<>();
            }
            for (Integer openXContentId : opnexRequest.getPubBlockedContentList()) {
                Set<String> iabIds = iabIdsByOpenXContentId.get(openXContentId);
                if (CollectionUtils.isNotEmpty(iabIds)) {
                    blockedIabIds.addAll(iabIds);
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("OpenX content has no IAB id mappings: " + openXContentId);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(blockedIabIds)) {
            byydRequest.setBlockedCategoryIabIds(blockedIabIds);
        }

        if (opnexRequest.getPubBlockedUrlCount() > 0) {
            byydRequest.setBlockedAdvertiserDomains(opnexRequest.getPubBlockedUrlList());
        }
    }

    static void mapImp(ByydImp byydImp, OpenX.AdId adId, OpenX.BidRequest opnexRequest) {

        if (adId.hasAdWidth()) {
            byydImp.setW(adId.getAdWidth());
        } else {
            byydImp.setW(opnexRequest.getAdWidth());
        }

        if (adId.hasAdHeight()) {
            byydImp.setH(adId.getAdHeight());
        } else {
            byydImp.setH(opnexRequest.getAdHeight());
        }

        byydImp.setSslRequired(opnexRequest.getSslEnabled());

        // Per Tiemen, OpenX doesn't signal this explicitly but it needs to be enforced
        byydImp.setStrictBannerSize(true);
    }

    static void mapDevice(ByydDevice byydDevice, OpenX.Device device, List<OpenX.ThirdPartyKeyValue> tpKeyVal) {

        String carrier = device.getCarrier();
        if (StringUtils.isNotEmpty(carrier)) {
            // OpenX gives us "MCC-MNC" and we need "MCCMNC"
            byydDevice.setMccMnc(StringUtils.replace(carrier, "-", ""));
        }

        String connectiontype = device.getConnectiontype();
        if (StringUtils.isNotEmpty(connectiontype)) {
            byydDevice.setNetworkType(connectiontype);
        }

        if (device.hasIdforad()) {
            String idforad = device.getIdforad();
            String os = device.getOs();
            if ("Android".equals(os)) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, idforad);
            } else if ("iOS".equals(os)) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IFA, idforad);
            } else {
                LOG.warning("Unrecognized OpenX device os " + os);
            }
        } else if (device.hasDidsha1()) {
            byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_DPID, device.getDidsha1());
        } else if (device.hasAndroididSha1()) {
            byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ANDROID, device.getAndroididSha1());
        } else if (device.hasAndroididMd5()) {
            byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ANDROID, device.getAndroididMd5());
        }
    }

    static ByydUser mapUser(OpenX.BidRequest request) {
        ByydUser user = new ByydUser();
        String userCookieId = request.getUserCookieId();
        if (StringUtils.isNotEmpty(userCookieId)) {
            user.setUid(userCookieId);
        }

        // Process geo attributes from the BidRequest first
        if (StringUtils.isNotEmpty(request.getUserGeoCountry())) {
            // OpenX provides country codes lowercase, we need to uppercase
            user.setCountryCode(request.getUserGeoCountry().toUpperCase());
        }

        if (StringUtils.isNotEmpty(request.getUserGeoState())) {
            // Our AbstractGeoDeriver can deal with name or abbrev (SC-284)
            user.setState(request.getUserGeoState());
        }

        if (request.hasUserGeoDma()) {
            user.setDma(String.valueOf(request.getUserGeoDma()));
        }

        return user;
    }

    public OpenX.BidResponse mapResponse(ByydResponse byydResponse, OpenX.BidRequest rtbRequest) {
        OpenX.BidResponse.Builder bidResponse = OpenX.BidResponse.newBuilder().setApiVersion(rtbRequest.getApiVersion()).setAuctionId(rtbRequest.getAuctionId());

        ByydBid byydBid = byydResponse.getBid();
        // The "impid" is the index into request.adIds
        int adIdIndex = Integer.parseInt(byydBid.getImpid());

        long priceInMicros = intoMicros(byydBid.getPrice());
        OpenX.Bid.Builder bidBuilder = OpenX.Bid.newBuilder().setMatchingAdId(rtbRequest.getMatchingAdIds(adIdIndex)).setCpmBidMicros(priceInMicros).setAdCode(byydBid.getAdm())
                .addClickThroughUrls(byydBid.getDestination()).setCrid(byydBid.getCrid());

        if (byydBid.getIabId() != null) {
            Set<Integer> openXCategoryIds = openXCategoryIdsByIabId.get(byydBid.getIabId());
            if (CollectionUtils.isNotEmpty(openXCategoryIds)) {
                for (Integer openXCategoryId : openXCategoryIds) {
                    bidBuilder.addAdOxCats(openXCategoryId);
                }
            }
        }

        bidResponse.addBids(bidBuilder);

        return bidResponse.build();
    }

    String digUserAgent(OpenX.BidRequest rtbRequest) throws NoBidException {
        if (rtbRequest.hasDevice()) {
            OpenX.Device rtbDevice = rtbRequest.getDevice();
            if (rtbDevice.hasUa()) {
                return rtbDevice.getUa();
            }
        }
        if (rtbRequest.hasUserAgent()) {
            return rtbRequest.getUserAgent();
        }
        return null;
    }

    String digIpAddress(OpenX.BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {
        if (rtbRequest.hasDevice()) {
            OpenX.Device device = rtbRequest.getDevice();
            if (device.hasIp()) {
                return device.getIp();
            }
        }
        if (rtbRequest.hasUserIpAddress()) {
            return ipv4BytesToString(rtbRequest.getUserIpAddress(), byydRequest);
        }
        return null;
    }

    static Set<ContentForm> mapRichMediaApi(OpenX.Device rtbDevice) {
        APIFramework[] apiFrameworkValues = APIFramework.values();
        Set<ContentForm> contentForms = new HashSet<ContentForm>();
        for (Integer api : rtbDevice.getApiList()) {
            if (api < 1 || api >= apiFrameworkValues.length) {
                LOG.info("Unrecognized OpenX framework api : " + api);
            } else {
                ContentForm contentForm = API_FRAMEWORK_CONTENT_FORM_MAP.get(apiFrameworkValues[api]);
                if (contentForm != null) {
                    contentForms.add(contentForm);
                }
            }
        }
        return contentForms;
    }

    static <T> Set<T> addToSet(Set<T> set, T value) {
        if (set == null) {
            set = new LinkedHashSet<T>();
        }
        set.add(value);
        return set;
    }

    static <T> Set<T> addToSet(Set<T> set, Collection<T> values) {
        if (set == null) {
            set = new LinkedHashSet<T>();
        }
        set.addAll(values);
        return set;
    }
}
