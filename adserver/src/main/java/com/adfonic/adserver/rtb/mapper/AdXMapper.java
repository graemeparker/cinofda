package com.adfonic.adserver.rtb.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.AdSlot;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Video;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Video.CompanionSlot;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Video.SkippableBidRequestType;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Video.VideoFormat;
import com.adfonic.adserver.rtb.adx.AdX.BidResponse;
import com.adfonic.adserver.rtb.adx.DestinationUrlType;
import com.adfonic.adserver.rtb.dec.AdXEncUtil;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.byyd.adx.AdxCreativeAttribute;
import com.byyd.adx.AdxOpenRtbMapper;
import com.byyd.adx.AdxRestrictedCategory;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

@Component
public class AdXMapper extends ProtoBufMapper {

    private static final transient Logger LOG = Logger.getLogger(AdXMapper.class.getName());

    private static final Set<DestinationType> appDestinations = ImmutableSet.of(DestinationType.IPHONE_APP_STORE);

    private static final Set<BidType> installTrackableBidTypes = ImmutableSet.of(BidType.CPI);

    private static final List<String> MP4_VIDEO_LIST = Collections.unmodifiableList(Arrays.asList("video/mp4"));

    private final AdXEncUtil encoder;

    private final DomainCacheManager domainCacheManager;

    private final AdxOpenRtbMapper adxIabMapper = AdxOpenRtbMapper.instance();

    // MAD-2984 - AdX Alcohol restricted category support 
    // With alcohol AdX introduced ad-restricted-categories.txt https://developers.google.com/ad-exchange/rtb/downloads
    // List contains only Alcohol (so far) and bid request needs to be processed very differently from those in ad-sensitive-categories.txt
    // read from request: allowed_restricted_category, write into response: restricted_category

    public static AdX.BidRequest protoText2Request(String protoText) throws ParseException {
        return protoText2Builder(protoText).build();
    }

    public static AdX.BidRequest.Builder protoText2Builder(String protoText) throws ParseException {
        AdX.BidRequest.Builder builder = AdX.BidRequest.newBuilder();
        TextFormat.merge(protoText, builder);
        return builder;
    }

    public static byte[] protoText2Bytes(String protoText) throws IOException {
        AdX.BidRequest bidRequest = protoText2Request(protoText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bidRequest.writeTo(baos);
        return baos.toByteArray();
    }

    public static String request2ProtoText(AdX.BidRequest request) {
        return TextFormat.printToUnicodeString(request);
    }

    public static String escapeBytes(byte[] bytes) throws Exception {
        Method mEscapeBytes = TextFormat.class.getDeclaredMethod("escapeBytes", byte[].class);
        mEscapeBytes.setAccessible(true);
        return (String) mEscapeBytes.invoke(null, bytes);
    }

    public static String escapeBytes(ByteString bytes) throws Exception {
        Method mEscapeBytes = TextFormat.class.getDeclaredMethod("escapeBytes", ByteString.class);
        mEscapeBytes.setAccessible(true);
        return (String) mEscapeBytes.invoke(null, bytes);
    }

    public static ByteString unescapeBytes(String protoText) throws Exception {
        Method mUnescapeBytes = TextFormat.class.getDeclaredMethod("unescapeBytes", CharSequence.class);
        mUnescapeBytes.setAccessible(true);
        return (ByteString) mUnescapeBytes.invoke(null, protoText);
    }

    public static String ipToProtoText(String ipAddress) throws Exception {
        InetAddress iaddr = InetAddress.getByName(ipAddress);
        return escapeBytes(ByteString.copyFrom(iaddr.getAddress()));
    }

    public static String ipFromProtoText(String protoText) throws Exception {
        ByteString byteString = AdXMapper.unescapeBytes(protoText);
        // AdX trucates last byte of IPv4 so ByteString is actually ony 3 bytes long
        byte[] bytes = new byte[4];
        byteString.copyTo(bytes, 0);
        if (bytes[3] == 0) {
            bytes[3] = 1; // Put 1 in place of missig byte
        }
        return InetAddress.getByAddress(bytes).getHostAddress();
    }

    @Autowired
    public AdXMapper(AdXEncUtil encoder, DomainCacheManager domainCacheManager) {
        Objects.requireNonNull(encoder);
        this.encoder = encoder;
        Objects.requireNonNull(domainCacheManager);
        this.domainCacheManager = domainCacheManager;
    }

    /**
     * https://developers.google.com/ad-exchange/rtb/request-guide
     */
    public ByydRequest mapRequest(String publisherExtId, AdX.BidRequest adxRequest, RtbBidEventListener listener) throws NoBidException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Request  >>\n" + TextFormat.printToString(adxRequest));
        }
        String rtbRequestId = BaseEncoding.base16().encode(adxRequest.getId().toByteArray());
        ByydRequest byydRequest = new ByydRequest(publisherExtId, rtbRequestId);

        if (adxRequest.getIsPing()) {
            throw new NoBidException(byydRequest, NoBidReason.TEST_REQUEST, NoBidReason.TEST_REQUEST);
        }

        AdX.BidRequest.Mobile mobile = null;
        // use hasMobile because isInitialized only flags uninitialized mandatory fields
        if (!adxRequest.hasMobile() || isNotInitialized(mobile = adxRequest.getMobile())) {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "request.Mobile", Level.WARNING);
        }

        AdX.BidRequest.Device device = adxRequest.getDevice();
        if (!adxRequest.hasDevice() || device == null) {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "request.Device", Level.WARNING);
        }

        String userAgent = adxRequest.getUserAgent();
        if (StringUtils.isEmpty(userAgent)) {
            abort(byydRequest, AdSrvCounter.MISS_UA, Level.FINE); // less than 1% but still noisy in the log
        }

        if (!adxRequest.hasIp()) {
            abort(byydRequest, AdSrvCounter.MISS_IP, Level.FINE); // about 20% of AdX requets
        }
        // For now IPv4 network address is fine
        String ipAddress = ipv4BytesToString(adxRequest.getIp(), byydRequest);
        if (StringUtils.isEmpty(ipAddress)) {
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "request.ip", Level.WARNING);
        }

        byydRequest.setTestMode(adxRequest.getIsTest());

        mapPublication(adxRequest, byydRequest, mobile);

        ByydDevice byydDevice = mapDevice(device, mobile);
        byydDevice.setUserAgent(userAgent);
        byydDevice.setIp(ipAddress);
        byydRequest.setDevice(byydDevice);

        if (adxRequest.hasGoogleUserId()) {
            String googleUserId = adxRequest.getGoogleUserId();
            ByydUser user = new ByydUser();
            user.setUid(googleUserId);
            byydRequest.setUser(user);
            byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_GOUID, adxRequest.getGoogleUserId());
        }

        boolean interstitial = mobile.getIsInterstitialRequest();

        // I've never seen AdX request with more than 1 AdSlot

        Set<String> bcat = new HashSet<String>();
        List<AdX.BidRequest.AdSlot> adxSlots = adxRequest.getAdslotList();
        AdX.BidRequest.AdSlot adxSlot = null;
        int adxSlotCount = adxSlots.size();
        if (adxSlotCount == 0) {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "request.AdSlot", Level.WARNING); // throws NoBidException
        } else if (adxSlotCount == 1) {
            adxSlot = adxSlots.get(0);
        } else {
            LOG.log(Level.INFO, "AdX request with multiple AdSlots: " + adxSlotCount + ", RtbId:" + byydRequest.getPublicationRtbId());
            adxSlot = adxSlots.get(0);
        }

        ByydImp byydImp = mapRequestAdSlot(adxSlot, byydRequest, bcat, interstitial);

        /**
         * Mopub's video skipability rules are static and duration based
         * - 1-15s are referred to as “non-skippable videos”
         * - 16-30s are referred to as “skippable videos”
         *  
         * AdX  video skipability works differently
         * https://developers.google.com/ad-exchange/rtb/adx-video-guide
         * 
         * To return skippable VAST response, it must contain specific fields (but AdX can be configured to add them automaticaly)  
         * https://support.google.com/adxbuyer/answer/2691733
         */
        Video video = adxRequest.getVideo();
        if (adxRequest.hasVideo() && video != null) {

            // TODO AdX video is unfinished 

            /*
             * AdX sends us lots of MOBILE_INTERSTITIAL video bid requests
             * 
             * AdX mobile interstitial which has video support, where the video inventory is embedded in the interstitial inventory.
             * Skip element of any creative should become active after 5 seconds of video start time. 
             */
            if (video.getInventoryType() == AdX.BidRequest.Video.InventoryType.MOBILE_INTERSTITIAL) {

                // We can bid with classic image/mraid creatives that can fit into this bid interstitial ad slot.

            } else {

                // Kick out requests that would probably only come with non-mobile traffic...
                List<VideoFormat> allowedVideoFormats = video.getAllowedVideoFormatsList();
                if (allowedVideoFormats.isEmpty() || !allowedVideoFormats.contains(VideoFormat.VIDEO_HTML5)) {
                    abort(byydRequest, AdSrvCounter.BAD_FIELD, "VideoFormat not allowed", Level.WARNING);
                }

                if (!video.getIsClickable()) {
                    abort(byydRequest, AdSrvCounter.BAD_FIELD, "Video not clickable", Level.WARNING);
                }

                // Oddly all interstitial video request are REQUIRE_SKIPPABLE from 1st millisecond

                SkippableBidRequestType skippability = video.getVideoAdSkippable();
                if (skippability == SkippableBidRequestType.REQUIRE_SKIPPABLE) {
                    abort(byydRequest, AdSrvCounter.FORMAT_INVALID, "Skippable video required", Level.FINE);
                }

                if (video.hasMaxAdDuration()) {
                    // max_ad_duration: 1 indicates that the request is only for skippable video creatives, so the buyer needs to refer to skippable_max_ad_duration instead.
                    int maxAdDuration = video.getMaxAdDuration();
                    if (maxAdDuration == 1) {
                        abort(byydRequest, AdSrvCounter.FORMAT_INVALID, "Skippable video required", Level.FINE);
                    }
                    byydImp.setMaxduration(video.getMaxAdDuration() / 1000);
                }
                if (video.hasMinAdDuration()) {
                    byydImp.setMinduration(video.getMinAdDuration() / 1000);
                } else {
                    byydImp.setMinduration(Constant.ZERO);
                }
                byydImp.setAdObject(AdObject.VIDEO);
                byydImp.setMimeTypeWhiteList(MP4_VIDEO_LIST);

                int videoadStartDelay = video.getVideoadStartDelay(); // 0 = pre-roll, x = mid-roll, -1 = post-roll

                int skippableMaxAdDuration = video.getSkippableMaxAdDuration(); // 

                List<CompanionSlot> companionSlotList = video.getCompanionSlotList();
            }
        }

        byydRequest.setImp(byydImp);

        boolean alcoholNotAllowed;
        List<ByydDeal> byydDeals = mapDirectDeals(adxSlot.getMatchingAdDataList());
        if (byydDeals != null && !byydDeals.isEmpty()) {
            // Deals are private by default on AdX (OpenRTB sends attribute)
            ByydMarketPlace byydMarketPlace = new ByydMarketPlace(byydDeals, true);
            byydRequest.setMarketPlace(byydMarketPlace);
            alcoholNotAllowed = !adxSlot.getAllowedRestrictedCategoryForDealsList().contains(AdxRestrictedCategory.Alcohol.getAdxId());
        } else {
            alcoholNotAllowed = !adxSlot.getAllowedRestrictedCategoryList().contains(AdxRestrictedCategory.Alcohol.getAdxId());
        }

        // Alcohol logic is opposit to OpenRTB bcat - If RestrictedCategory is not explicitly allowed -> it is blocked
        if (alcoholNotAllowed) {
            bcat.addAll(AdxRestrictedCategory.Alcohol.getIabCategories());
        }

        bcat.remove(null);
        if (!bcat.isEmpty()) {
            byydRequest.setBlockedCategoryIabIds(new ArrayList<String>(bcat));
        }

        byydRequest.doIncludeDestination(true);
        byydRequest.setUseOnlyRealDestination(true);

        // Plugins cannot be made conformant to the border guideline with the current asset tweaking. So may be no for now
        byydRequest.blockPlugins();

        return byydRequest;
    }

    private void mapPublication(AdX.BidRequest adxRequest, ByydRequest byydRequest, AdX.BidRequest.Mobile mobile) throws NoBidException {
        String rtbId;
        String url = adxRequest.getUrl(); // mobile website / appstore link
        String pubName;
        String anonymousId = null;
        boolean isApp = mobile.getIsApp();
        if (isApp) {
            byydRequest.setMedium(Medium.APPLICATION);
            String appId = mobile.getAppId(); // application bundle (com.devuni.flashlight / 840919914)
            if (StringUtils.isNotEmpty(appId)) {
                rtbId = "APPLNID-" + appId;
                byydRequest.setBundleName(appId);
                pubName = appId;
            } else if (adxRequest.hasAnonymousId() && StringUtils.isNotEmpty(anonymousId = adxRequest.getAnonymousId())) {
                rtbId = "ANONAID-" + anonymousId;
                pubName = "Anonymous App - " + anonymousId;
            } else {
                // No way to build RtbId - happens +/- every second
                abort(byydRequest, AdSrvCounter.MISS_PUBL_ID, "AdX App without AppId or AnonymousId", Level.FINE);
                return;
            }
        } else {
            byydRequest.setMedium(Medium.SITE);
            if (StringUtils.isNotEmpty(url)) {
                String sellerNetworkCnPfx = null;
                StringBuilder rtbIdBldr = new StringBuilder();
                if (adxRequest.hasSellerNetworkId()) {
                    // Only non anonymous sites can have SellerNetworkId
                    int sellerNetworkId = adxRequest.getSellerNetworkId();
                    byydRequest.setSellerNetworkId(sellerNetworkId);
                    String sellerNetworkName = adxIabMapper.getSellerNetwork(sellerNetworkId);
                    if (sellerNetworkName != null) {
                        sellerNetworkCnPfx = "SITEURL-SN" + sellerNetworkName;
                    }
                    rtbIdBldr.append("SITEURL-SELNID").append(sellerNetworkId).append("_");
                }

                try {
                    pubName = new URI(url).getHost();
                    rtbId = rtbIdBldr.append(pubName).toString();
                    if (sellerNetworkCnPfx != null) {
                        byydRequest.setFallbackPublicationRtbId(sellerNetworkCnPfx + "_" + pubName);
                    }
                } catch (URISyntaxException e) {
                    abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unparseable request url" + url, Level.INFO);
                    return;
                }
            } else if (adxRequest.hasAnonymousId() && StringUtils.isEmpty(anonymousId = adxRequest.getAnonymousId())) {
                rtbId = "ANONSID-" + anonymousId;
                pubName = "Anonymous Site - " + anonymousId;
            } else {
                abort(byydRequest, AdSrvCounter.MISS_PUBL_ID, "AdX Site without url or AnonymousId", Level.FINE);
                return;
            }
        }

        if (url.length() > MAX_URL_LENGTH) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Truncating url with length=" + url.length() + " at " + MAX_URL_LENGTH);
            }
            url = url.substring(0, MAX_URL_LENGTH);
        }
        byydRequest.setPublicationUrlString(url);
        byydRequest.setPublicationName(pubName);
        byydRequest.setPublicationRtbId(rtbId);
    }

    /**
     * Private Auction FAQ
     * https://support.google.com/adxbuyer/answer/3081011?hl=en
     */
    private List<ByydDeal> mapDirectDeals(List<AdX.BidRequest.AdSlot.MatchingAdData> matchingAdDataList) {
        List<ByydDeal> dealList = new ArrayList<>();
        for (AdX.BidRequest.AdSlot.MatchingAdData matchingAdData : matchingAdDataList) {
            // If it ever happen that we have multiple adGroups, then then we will need -> long adgroupId = matchingAdData.getAdgroupId();
            if (matchingAdData.getDirectDealCount() > 0) {
                for (AdX.BidRequest.AdSlot.MatchingAdData.DirectDeal directDeal : matchingAdData.getDirectDealList()) {
                    ByydDeal byydDeal = new ByydDeal(String.valueOf(directDeal.getDirectDealId()));
                    byydDeal.setBidFloor(fromMicros(directDeal.getFixedCpmMicros()).doubleValue());
                    /*
                    This need grooming to figure out this
                    DealType dealType = directDeal.getDealType();
                    AuctionType byydType;
                    if (dealType == DealType.PREFERRED_DEAL) {
                        // Inventory sold through a Preferred Deal goes to the single participating buyer, as long as the buyer bids at or above the negotiated fixed price. 
                        // If the buyer’s bid is above the fixed price, the buyer still pays only the fixed price.
                        byydType = AuctionType.NOT_AN_AUCTION;
                    } else if (dealType == DealType.PRIVATE_AUCTION) {
                        // The inventory clears to the highest net bidder who bids above the impression minimum
                        byydType = AuctionType.FIRST_PRICE_AUCTION;
                    } else {
                        byydType = AuctionType.SECOND_PRICE_ACTION; // Honestly, I don't have a clue here...
                    }
                    byydDeal.setAuctionType(byydType);
                    */
                    dealList.add(byydDeal);
                }
            }
        }
        return dealList;
    }

    /**
     * Read https://support.google.com/adxbuyer/answer/3221407
     */
    private ByydDevice mapDevice(AdX.BidRequest.Device device, AdX.BidRequest.Mobile mobile) {
        ByydDevice byydDevice = new ByydDevice();

        String rawDeviceId = null;
        if (mobile.hasAdvertisingId()) {
            // AdX sends plaintext if bid is over https
            ByteString advertisingIdBytes = mobile.getAdvertisingId();
            ByteBuffer bb = ByteBuffer.wrap(advertisingIdBytes.toByteArray());
            UUID uuid = new UUID(bb.getLong(), bb.getLong());
            rawDeviceId = uuid.toString();

        } else if (mobile.hasEncryptedAdvertisingId()) {
            // https://developers.google.com/ad-exchange/rtb/response-guide/decrypt-advertising-id
            rawDeviceId = encoder.decodeDeviceId(mobile.getEncryptedAdvertisingId().toByteArray());

        } else if (mobile.hasEncryptedHashedIdfa()) {
            // https://support.google.com/adxbuyer/answer/3221407
            byte[] ifaMd5bytes = encoder.decrypt(mobile.getEncryptedHashedIdfa().toByteArray(), 16);
            byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5, Hex.encodeHexString(ifaMd5bytes));
        }

        if (rawDeviceId != null) {
            String deviceIdentifierType = null;
            if (device.hasPlatform()) {
                String adxPlatform = device.getPlatform();
                if ("android".equals(adxPlatform)) {
                    deviceIdentifierType = DeviceIdentifierType.SYSTEM_NAME_ADID;
                } else if ("iphone".equals(adxPlatform) || "ipad".equals(adxPlatform)) {
                    deviceIdentifierType = DeviceIdentifierType.SYSTEM_NAME_IFA;
                } else {
                    LOG.warning("AdX unrecognized mobile platform: " + adxPlatform);
                }
            }
            if (deviceIdentifierType != null) {

                if (deviceIdentifierType.equals(DeviceIdentifierType.SYSTEM_NAME_IFA)) {
                    rawDeviceId = rawDeviceId.toUpperCase();
                } else {
                    rawDeviceId = rawDeviceId.toLowerCase();
                }
                byydDevice.setDeviceIdentifier(deviceIdentifierType, rawDeviceId);
            }
        }

        return byydDevice;
    }

    /**
     * Publishers use the BidRequest to pass restrictions on what ads they will allow.
     * https://developers.google.com/ad-exchange/rtb/response-guide#publisher-restrictions
     * 
     * Valid excludable attributes
     * https://storage.googleapis.com/adx-rtb-dictionaries/publisher-excludable-creative-attributes.txt
     */
    private ByydImp mapRequestAdSlot(AdX.BidRequest.AdSlot adSlot, ByydRequest byydRequest, Set<String> bcats, boolean interstitial) throws NoBidException {
        List<Integer> adxExcludedAttrs = adSlot.getExcludedAttributeList();

        //Ads are disallowed to use the html_snippet or snippet_template field in BidResponse.Ad. Most likely this is VAST video request
        if (adxExcludedAttrs.contains(AdxCreativeAttribute.Html.getAdxId())) {
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "HTML attribute excluded", Level.FINE);
        }

        if (!adSlot.getNativeAdTemplateList().isEmpty()) {
            // We do not support native ads on AdX. They also have empty adSlot widht/height
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unsupported native ad on AdX", Level.FINE);
        }

        ByydImp byydImp = new ByydImp(String.valueOf(adSlot.getId()));
        byydImp.setInterstitial(interstitial);

        mapWidthAndHeight(adSlot, byydImp, byydRequest);

        /**
         * When multiple AdGroups is defined and active in https://www.google.com/adx/Main.html#PRETARGETING
         * then bid request can contain multiple MatchingAdData elements.
         * 
         * We have no use for that, but AdX then requires that bid response must contain billing_id
         * so simply use first one and same apply for response
         */
        if (adSlot.getMatchingAdDataCount() > 0) {
            long minCpmMicros = adSlot.getMatchingAdData(0).getMinimumCpmMicros();
            byydImp.setBidfloor(fromMicros(minCpmMicros));
            byydImp.setBidfloorcur(Constant.USD);
        }

        for (int pCatId : adSlot.getExcludedProductCategoryList()) {
            Set<String> iabCategories = adxIabMapper.getIabProductCategories(pCatId);
            if (iabCategories == null) {
                abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unknown AdX product category" + pCatId, Level.INFO);
            }
            bcats.addAll(iabCategories);
        }

        for (int sCatId : adSlot.getExcludedSensitiveCategoryList()) {
            Set<String> iabCategories = adxIabMapper.getIabSensitiveCategories(sCatId);
            if (iabCategories == null) {
                abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unknown AdX sensitive category" + sCatId, Level.INFO);
            }
            bcats.addAll(iabCategories);
        }

        if (adxExcludedAttrs.contains(DestinationUrlType.CLICK_TO_APP.creativeAttributeId())) {
            byydImp.setbDestTypes(appDestinations);
            byydImp.setbBidTypes(installTrackableBidTypes);
        }

        // Ads are disallowed to require the MRAID API to render.
        if (adxExcludedAttrs.contains(AdxCreativeAttribute.Mraid_1_0.getAdxId())) {
            byydImp.setContentFormWhiteList(ByydImp.CF_MOBILE_WEB);
        } else {
            byydImp.setContentFormWhiteList(ByydImp.CF_MRAID_MOBWEB);
        }

        // AdX very dislikes http/https creative duality, causing our bids to be filter outs and general confusion
        // Anyway, more than 90% of traffic is ssl required (excluded_attribute: 48) and AdX plan is to have 100% SSL
        // So just make sure here, that secured version of creative (https trackers/beacons/pixels) is allways rendered and returned (even when not required in bid request) 
        byydImp.setSslRequired(true);

        /*
        This was bad idea - AdX excluded attributes does not match well to OpenRtb attributes 
        Set<Integer> battr = new HashSet<Integer>();
        if (excludedAttrs.contains(AdxCreativeAttribute.VastVideo.getAdxId())) {
            battr.add(CreativeAttribute.IN_BANNER_VIDEO_AD_USER_INITIATED.ordinal());
        }

        if (excludedAttrs.contains(AdxCreativeAttribute.ExpandingAnyDiagonal.getAdxId())) {
            battr.add(CreativeAttribute.EXPANDABLE_USER_INITIATED_CLICK.ordinal());
        }

        if (excludedAttrs.contains(AdxCreativeAttribute.RolloverToExpand.getAdxId())) {
            battr.add(CreativeAttribute.EXPANDABLE_USER_INITIATED_ROLLOVER.ordinal());
        }
        byydImp.setBattr(battr);
        */

        return byydImp;
    }

    /**
     * Requests where widths 320 & 300 and heights 50 & 50 are known to exist
     * Rule, I just made, is that we will pick dimension with biggest width (regardles height) 
     */
    private void mapWidthAndHeight(AdSlot adSlot, ByydImp byydImp, ByydRequest byydRequest) throws NoBidException {

        int w;
        int h;
        int widthCount = adSlot.getWidthCount();
        if (widthCount == 1) {
            // usual path - easy
            w = adSlot.getWidth(0);
            h = adSlot.getHeight(0);
        } else if (widthCount > 1) {

            List<Integer> widthList = adSlot.getWidthList();
            List<Integer> heightList = adSlot.getHeightList();
            Integer maxWidth = widthList.get(0);
            Integer maxHeight = heightList.get(0);
            for (int i = 1; i < widthList.size(); ++i) {
                Integer width = widthList.get(i);
                if (width.intValue() > maxWidth.intValue()) {
                    maxWidth = width;
                    maxHeight = heightList.get(i); //height just follows width
                }
            }
            w = maxWidth;
            h = maxHeight;

        } else {
            abort(byydRequest, AdSrvCounter.MISS_FIELD, "AdX adslot with no width", Level.WARNING);
            return; //not really needed as abort allways throws expcetion
        }

        // Interstitial request have dimensions of device screen instead of standard ad format sizes.
        // https://developers.google.com/ad-exchange/rtb/interstitial-ads
        if (byydImp.isInterstitial()) {
            // Solved here as this is AdX exclusive feature. Systematic FORMAT handling is already compromised by numerous hardconings and exceptions already
            // If we will add AdX Video support, this code would need be extended
            FormatDto directFormat = domainCacheManager.getCache().getFormatBySystemName("image" + w + "x" + h);
            // Some screens maps directly to some corresponding Format (320x480, 768x1024) 
            if (directFormat == null) {
                // But lots of screens doesn't (360x640, 480x805, 1280x752), so find Format bigger than 50% of screen width x 40% screen height
                if (w > 320 && w < 640 && h > 480 && h < 960) {
                    w = 320;
                    h = 480; // image320x480
                } else if (w > 480 && w < 960 && h > 320 && h < 640) {
                    w = 480;
                    h = 320; // image480x320
                } else if (w > 768 && w < 1536 && h > 1024 && h < 2048) {
                    w = 768;
                    h = 1024; // image768x1024
                } else if (w > 1024 && w < 2048 && h > 768 && h < 1536) {
                    w = 1024;
                    h = 768; // image1024x768
                }
            }
        }

        byydImp.setW(w);
        byydImp.setH(h);
    }

    /**
     *
     * https://developers.google.com/ad-exchange/rtb/response-guide
     * 
     * Valid declarable attributes
     * https://storage.googleapis.com/adx-rtb-dictionaries/buyer-declarable-creative-attributes.txt
     */
    public AdX.BidResponse.Builder mapResponse(ByydResponse byydResponse, RtbExecutionContext<AdX.BidRequest, BidResponse> context) throws NoBidException {

        ByydBid byydBid = byydResponse.getBid();
        ByydImp byydImp = byydBid.getImp();
        AdX.BidResponse.Builder responseBldr = AdX.BidResponse.newBuilder();
        AdX.BidResponse.Ad.AdSlot.Builder adSlotBldr = AdX.BidResponse.Ad.AdSlot.newBuilder();

        // If it ever happen that we have multiple adGroups, then then we will need to set adSlotBldr.setAdgroupId(...);

        adSlotBldr.setId(Integer.parseInt(byydBid.getImpid()));
        adSlotBldr.setMaxCpmMicros(intoMicros(byydBid.getPrice()));

        // As we don't really know how min_cpm_micros is used so let's not send it 
        // adSlotBldr.setMinCpmMicros(intoMicros(byydBid.getImp().getBidfloor()));

        if (byydBid.getDealId() != null) {
            adSlotBldr.setDealId(Long.parseLong(byydBid.getDealId()));
        }

        // We simply use first billing_id when more is sent
        adSlotBldr.setBillingId(context.getRtbRequest().getAdslot(0).getMatchingAdData(0).getBillingId(0));

        AdX.BidResponse.Ad.Builder adBldr = AdX.BidResponse.Ad.newBuilder();
        if (byydBid.getAdomain() != null) {
            adBldr.addAdvertiserName(byydBid.getAdomain());
        }
        adBldr.addAdslot(adSlotBldr);

        CreativeDto creative = byydBid.getCreative();
        adBldr.setBuyerCreativeId(creative.getExternalID());

        adBldr.addClickThroughUrl(byydBid.getDestination());

        adBldr.setHtmlSnippet(byydBid.getAdm());

        // Declare sensitive categories
        String categoryIab = byydBid.getIabId();
        Set<Integer> adxCategories = adxIabMapper.getAdxSensitiveCategories(categoryIab);
        if (adxCategories != null) {
            for (Integer adxCategory : adxCategories) {
                adBldr.addCategory(adxCategory);
            }
        }

        // Alcohol is restricted (not sensitive) category
        Collection<AdxRestrictedCategory> adxRestricted = AdxRestrictedCategory.getByIabId(categoryIab);
        for (AdxRestrictedCategory item : adxRestricted) {
            adBldr.addRestrictedCategory(item.getAdxId());
        }

        // Interstitials must have dimensions declared - https://developers.google.com/ad-exchange/rtb/interstitial-ads
        // BidResponses must include ad size for multi-size requests - https://developers.google.com/ad-exchange/rtb/relnotes?hl=en#07-2013-release
        // But AdX filters out more than that so put dimensions into response every time...

        adBldr.setWidth(byydImp.getW());
        adBldr.setHeight(byydImp.getH());

        // Documentation does not requires that, but AdX allways autocorrect us by adding RichMediaCapabilityNonFlash (50) attribute
        // Just do not declare it for video bids (currently we do not serve video on AdX, but I'm future proof you know...)
        if (byydImp.getAdObject() != AdObject.VIDEO) {
            adBldr.addAttribute(AdxCreativeAttribute.RichMediaCapabilityNonFlash.getAdxId());
        }

        ContentForm usedContentForm = byydBid.getContentForm();
        if (usedContentForm == ContentForm.MRAID_1_0) {
            // AdX uses MRAID as kind of super-attribute. For example expansion attributes must NOT be declared when MRAID is 
            adBldr.addAttribute(AdxCreativeAttribute.Mraid_1_0.getAdxId());
        } else {
            // Non MRAID - declared attributes that can be translated OpenRTB -> AdX 
            Set<Integer> creativeAttributes = creative.getCreativeAttributes();
            for (Integer oRtbId : creativeAttributes) {
                // This is problably still not right as when declaring some expandibility flag, then vendor must be declared as well... 
                AdxCreativeAttribute adXattr = AdxCreativeAttribute.getByOrtbId(oRtbId);
                if (adXattr != null && adXattr.isDeclarable()) {
                    adBldr.addAttribute(adXattr.getAdxId());
                } else {
                    //print warning at maybe...
                }
            }
        }

        if (creative.isSslCompliant()) {
            adBldr.addAttribute(AdxCreativeAttribute.RichMediaCapabilitySSL.getAdxId());
        }
        responseBldr.addAd(adBldr);

        return responseBldr;
    }

}
