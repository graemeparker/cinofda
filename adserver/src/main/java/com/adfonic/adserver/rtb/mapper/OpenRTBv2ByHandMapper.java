package com.adfonic.adserver.rtb.mapper;

import static com.adfonic.adserver.Constant.ONE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.impl.DidEater;
import com.adfonic.adserver.impl.ExtendedCapabilitiesUtils;
import com.adfonic.adserver.impl.OrtbNativeAdWorker;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v1.SeatBid;
import com.adfonic.adserver.rtb.open.v2.App;
import com.adfonic.adserver.rtb.open.v2.Banner;
import com.adfonic.adserver.rtb.open.v2.Bid;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.Device;
import com.adfonic.adserver.rtb.open.v2.Geo;
import com.adfonic.adserver.rtb.open.v2.Imp;
import com.adfonic.adserver.rtb.open.v2.PmpV2;
import com.adfonic.adserver.rtb.open.v2.PmpV2.DealV2;
import com.adfonic.adserver.rtb.open.v2.RtbNative;
import com.adfonic.adserver.rtb.open.v2.Site;
import com.adfonic.adserver.rtb.open.v2.SiteOrApp;
import com.adfonic.adserver.rtb.open.v2.User;
import com.adfonic.adserver.rtb.open.v2.VideoV2;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.ortb.nativead.NativeAdRequest.NativeAdRequestWrapper;
import com.google.common.collect.ImmutableSet;

public class OpenRTBv2ByHandMapper implements
        BaseMapper<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, com.adfonic.adserver.rtb.open.v1.BidResponse> {

    private static final transient Logger LOG = Logger.getLogger(OpenRTBv2ByHandMapper.class.getName());

    private static final Map<APIFramework, ContentForm> SUPPORTED_API_MAP = Collections.unmodifiableMap(new HashMap<APIFramework, ContentForm>() {
        {
            put(APIFramework.MRAID, ContentForm.MRAID_1_0);
            put(APIFramework.MRAID_2, ContentForm.MRAID_1_0); // MRAID is backward compatible
        }
    });

    protected final OpenRTBv1QuickNdirty v1mapper;

    private static final OpenRTBv2ByHandMapper instance = new OpenRTBv2ByHandMapper();

    public static OpenRTBv2ByHandMapper instance() {
        return instance;
    }

    protected OpenRTBv2ByHandMapper() {
        this(OpenRTBv1QuickNdirty.getInstance());
    }

    protected OpenRTBv2ByHandMapper(OpenRTBv1QuickNdirty v1mapper) {
        this.v1mapper = v1mapper;
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, ByydRequest byydRequest) {
        BidResponse rtbResponse = v2(v1mapper.mapRtbResponse(byydResponse, byydRequest), byydResponse);
        ByydBid byydBid = byydResponse.getBid();
        // DealID is optional
        SeatBid rtbSeatBid = (SeatBid) rtbResponse.getSeatbid().get(0);
        if (byydBid.getDealId() != null) {
            Bid rtbBid = (Bid) rtbSeatBid.getBid().get(0);
            rtbBid.setDealid(byydBid.getDealId());
        }
        // SeatID is optional
        rtbSeatBid.setSeat(byydBid.getSeat());
        return rtbResponse;
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, com.adfonic.adserver.rtb.open.v1.BidResponse bidResponse) {
        BidResponse v1response = v1mapper.mapRtbResponse(byydResponse, bidResponse);
        return v2(v1response, byydResponse);
    }

    /**
     * OMG, I would like know state of this method author's mental sanity. Maybe not.
     */
    protected com.adfonic.adserver.rtb.open.v1.BidResponse v2(com.adfonic.adserver.rtb.open.v1.BidResponse<SeatBid<com.adfonic.adserver.rtb.open.v1.Bid>> v1Response,
            ByydResponse byydResponse) {
        Bid rtbBid = (Bid) v1Response.getSeatbid().get(0).getBid().get(0);
        rtbBid.v2();
        rtbBid.setCat(Arrays.asList(byydResponse.getBid().getIabId()));
        // MAD-3168 - Allowing seat id on rtb v2
        v1Response.getSeatbid().get(0).setSeat(byydResponse.getBid().getSeat());
        return v1Response;
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExtId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = mapPublication(publisherExtId, rtbRequest, listener);

        Device rtbDevice = rtbRequest.getDevice();
        if (rtbDevice != null) {
            byydRequest.setDevice(mapRtbDevice(rtbRequest.getDevice(), rtbRequest, byydRequest));
        } else {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request.device");
        }

        byydRequest.setTmax(rtbRequest.getTmax());
        byydRequest.setBlockedAdvertiserDomains(rtbRequest.getBadv());
        byydRequest.setBlockedCategoryIabIds(rtbRequest.getBcat());
        byydRequest.setCurrencies(rtbRequest.getCur());
        byydRequest.setTestMode(Constant.ONE.equals(rtbRequest.getTest()));

        // User is optional
        User rtbUser = rtbRequest.getUser();
        if (rtbUser != null) {
            byydRequest.setUser(mapRtbUser(rtbUser));
        }

        byydRequest.setTrackingDisabled(isTrackingDisabled(rtbRequest));

        // Do NOT iterate imp list and go for first element directly
        // 1. Exchanges allways send only one imp (although this is up to them and they can change it)
        // 2. Targeting and rendering code cannot handle multiple bids anyway (only one Impression is stored, etc etc)
        List<com.adfonic.adserver.rtb.open.v2.Imp> rtbImpList = (List<com.adfonic.adserver.rtb.open.v2.Imp>) rtbRequest.getImp();
        if (rtbImpList != null && rtbImpList.size() != 0) {
            com.adfonic.adserver.rtb.open.v2.Imp rtbImp = rtbImpList.get(0);
            ByydImp byydImp = mapRtbImp(rtbImp, byydRequest);
            byydRequest.setImp(byydImp);
        } else {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request.imp");
        }

        return byydRequest;
    }

    /**
     * Simply if any of those flags is 1 => tracking is disabled
     */
    protected boolean isTrackingDisabled(BidRequest rtbRequest) {
        if (ONE.equals(rtbRequest.getDevice().getDnt()) || ONE.equals(rtbRequest.getDevice().getLmt())) {
            return true;
        } else if (rtbRequest.getRegs() != null && ONE.equals(rtbRequest.getRegs().getCoppa())) {
            return true;
        } else {
            return false;
        }
    }

    private ByydRequest mapPublication(String publisherExtId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = new ByydRequest(publisherExtId, rtbRequest.getId());

        SiteOrApp siteOrApp;
        String siteOrAppId;
        String publicationUrl;
        Medium medium;
        if ((siteOrApp = rtbRequest.getSite()) != null) {
            medium = Medium.SITE;
            siteOrAppId = siteOrApp.getId();
            publicationUrl = ((Site) siteOrApp).getPage();
            if (publicationUrl == null) {
                // Fall back on the optional "domain"
                publicationUrl = siteOrApp.getDomain();
            }
        } else if ((siteOrApp = rtbRequest.getApp()) != null) {
            medium = Medium.APPLICATION;
            publicationUrl = siteOrApp.getDomain();
            siteOrAppId = siteOrApp.getId();
            // MAX-137 FT: AppNexus Bundle ID
            if (StringUtils.isBlank(siteOrAppId)) {
                siteOrAppId = ((App) siteOrApp).getBundle();
            }
            byydRequest.setBundleName(((App) siteOrApp).getBundle());
        } else {
            if (listener != null) {
                listener.bidRequestRejected(publisherExtId, rtbRequest.getId(), "Neither site nor app supplied");
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request app/site");
        }

        String siteOrAppName = siteOrApp.getName();
        // Next fallback is to site/app name
        if (StringUtils.isEmpty(siteOrAppId)) {
            siteOrAppId = siteOrAppName; // Last chance is app/site name
            if (StringUtils.isEmpty(siteOrAppId)) {
                if (listener != null) {
                    listener.bidRequestRejected(publisherExtId, rtbRequest.getId(), "site or app id/name not supplied");
                }
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_PUBL_ID, "app/site id/bundle/name");
            }
        }

        byydRequest.setMedium(medium);
        // Having single static prefix is NOT clever. If 2 exchanges will happen to have same app/site id - collision is born
        // Anyway this is usually overriden in Exchange specific mapper subclass
        byydRequest.setPublicationRtbId((medium == Medium.APPLICATION ? "RTB2AID" : "RTB2SID") + "-" + siteOrAppId);

        byydRequest.setPublicationUrlString(publicationUrl);
        byydRequest.setIabIds(siteOrApp.getCat());
        byydRequest.setPublicationName(siteOrAppName);

        return byydRequest;
    }

    /**
     * Legacy RTB_ID construction algorithm in OpenRTBv2ByHandMapper is quite flawed. 
     * Unfortunately lots of publications is already created using it and as we do not want to lose them 
     * by changing algorithm, we need to override it in exchange specific mapper.
     */
    public static void setExchangeBasedRtbId(String exchangePrefix, BidRequest<? extends Imp> rtbRequest, ByydRequest byydRequest) throws NoBidException {
        App app;
        Site site;
        String publicationId;
        StringBuilder sbRtbId = new StringBuilder(exchangePrefix);
        if ((app = rtbRequest.getApp()) != null) {
            sbRtbId.append("-a-");
            publicationId = app.getId();
            if (publicationId == null) {
                // Mobile app can fallback to bundle
                publicationId = app.getBundle();

            }
        } else if ((site = rtbRequest.getSite()) != null) {
            sbRtbId.append("-s-");
            publicationId = site.getId();
            // Mobile website has nothing to fallback to. Sad so sad.
        } else {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request app/site");
        }

        if (StringUtils.isBlank(publicationId)) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_PUBL_ID);
        }

        byydRequest.setPublicationRtbId(sbRtbId.append(publicationId).toString());
    }

    /**
     */
    protected ByydImp mapRtbImp(com.adfonic.adserver.rtb.open.v2.Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = new ByydImp(rtbImp.getId());
        byydImp.setBidfloor(rtbImp.getBidfloor());
        byydImp.setBidfloorcur(rtbImp.getBidfloorcur());
        byydImp.setSslRequired(rtbImp.isSslRequired());
        byydImp.setInterstitial(rtbImp.isInterstitial());

        Banner rtbBanner;
        VideoV2 rtbVideo;
        RtbNative rtbNative;
        Set<APIFramework> apis = null;
        if ((rtbVideo = rtbImp.getVideo()) != null) {
            apis = rtbVideo.getApi(); // Mopub does NOT send api
            byydImp.bypassCFRestrictions(true); // disable setting TargetingContext.CONTENT_FORM_RESTRICTION_SET
            mapVideoImp(rtbVideo, byydImp, byydRequest);
        } else if ((rtbBanner = rtbImp.getBanner()) != null) { //take when no video
            apis = rtbBanner.getApi();
            mapBannerImp(byydImp, rtbBanner);
        } else if ((rtbNative = rtbImp.getNative()) != null) {
            mapNativeImp(rtbNative, byydImp, byydRequest);
        } else {
            LOG.warning("Missing imp.banner or imp.video or imp.native");
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "imp.banner or imp.video or imp.native");
        }

        if (apis != null) {
            apis = ExtendedCapabilitiesUtils.intersectionOf(apis, SUPPORTED_API_MAP.keySet());
            Set<ContentForm> contentFormList = new HashSet<ContentForm>(ByydImp.CF_MOBILE_WEB);
            for (APIFramework api : apis) {
                ContentForm contentForm = SUPPORTED_API_MAP.get(api);
                if (contentForm != null) {
                    contentFormList.add(contentForm);
                }
            }
        }

        PmpV2 rtbPmp = rtbImp.getPmp();
        if (rtbPmp != null) {
            ByydMarketPlace byydPmp = mapRtbPmp(rtbPmp);
            if (byydPmp != null) {
                byydRequest.setMarketPlace(byydPmp);
            }
        }

        return byydImp;
    }

    protected void mapBannerImp(ByydImp byydImp, Banner rtbBanner) {
        byydImp.setAdObject(AdObject.BANNER);
        byydImp.setBattr(rtbBanner.getBattr());
        byydImp.setBtype(rtbBanner.getBtype());
        byydImp.setH(rtbBanner.getH());
        byydImp.setW(rtbBanner.getW());
        byydImp.setMimeTypeWhiteList(rtbBanner.getMimes());
    }

    protected void mapVideoImp(VideoV2 rtbVideo, ByydImp byydImp, ByydRequest byydRequest) throws NoBidException {
        byydImp.setAdObject(AdObject.VIDEO);
        byydImp.setBattr(rtbVideo.getBattr());
        byydImp.setH(rtbVideo.getH());
        byydImp.setW(rtbVideo.getW());
        byydImp.setMimeTypeWhiteList(rtbVideo.getMimes());
        byydImp.setMinduration(rtbVideo.getMinduration());
        byydImp.setMaxduration(rtbVideo.getMaxduration());
        Set<Integer> protocols = rtbVideo.getProtocols();
        if (protocols != null && protocols.size() != 0) {
            byydImp.setVideoProtocols(protocols);
        } else {
            Integer protocol = rtbVideo.getProtocol();
            if (protocol != null) {
                byydImp.setVideoProtocols(ImmutableSet.of(protocol));
            }
        }

        byydImp.setSkipafter(ONE);

    }

    /**
     * Extension point as different handling is expected for different exchanges 
     */
    protected void mapNativeImp(RtbNative rtbNative, ByydImp byydImp, ByydRequest byydRequest) throws NoBidException {
        byydImp.setAdObject(AdObject.NATIVE);
        byydImp.setBattr(rtbNative.getBattr());
        // Dimensions equal to 0 is legacy way to smuggle native ad disguised as banner  
        byydImp.setH(0);
        byydImp.setW(0);
        String nativeRequest = rtbNative.getRequest();
        if (StringUtils.isNotBlank(nativeRequest)) {
            try {
                NativeAdRequestWrapper wrapper = OrtbNativeAdWorker.instance().readRequest(nativeRequest);
                byydImp.setNativeAdRequest(wrapper.getNative());
            } catch (IOException iox) {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.BAD_FIELD, "Native Ad Details: " + iox);
            }
        }
    }

    protected ByydMarketPlace mapRtbPmp(PmpV2 rtbPmp) {
        List<DealV2> rtbDeals;
        if ((rtbDeals = rtbPmp.getDeals()) != null && !rtbDeals.isEmpty()) {
            List<ByydDeal> byydDeals = new ArrayList<>(rtbDeals.size());
            for (DealV2 rtbDeal : rtbDeals) {
                ByydDeal byydDeal = mapRtbDeal(rtbDeal);
                if (byydDeal != null) {
                    byydDeals.add(byydDeal);
                }
            }
            if (!byydDeals.isEmpty()) {
                return new ByydMarketPlace(byydDeals, rtbPmp.isPrivate());
            }
        }
        return null;
    }

    protected ByydDeal mapRtbDeal(DealV2 deal) {
        String dealId = deal.getId();
        if (StringUtils.isBlank(dealId)) {
            LOG.warning("Ignoring Pmp Deal with empty Id");
            return null;
        }
        ByydDeal byydDeal = new ByydDeal(dealId, deal.getWseat());
        BigDecimal bidFloor = deal.getBidfloor();
        if (bidFloor != null) {
            byydDeal.setBidFloor(bidFloor.doubleValue());
        }
        return byydDeal;
    }

    protected ByydDevice mapRtbDevice(com.adfonic.adserver.rtb.open.v2.Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {
        if (StringUtils.isEmpty(rtbDevice.getIp())) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_IP);
        }
        if (StringUtils.isEmpty(rtbDevice.getUa())) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_UA);
        }

        ByydDevice byydDevice = new ByydDevice();
        byydDevice.setIp(rtbDevice.getIp());
        byydDevice.setUserAgent(rtbDevice.getUa());

        String os = rtbDevice.getOs();
        byydDevice.setOs(os);

        String rawDid = setIfaOrAdid(byydDevice, os, rtbDevice.getIfa());
        String dpidsha1 = rtbDevice.getDpidsha1();
        if (dpidsha1 != null) {
            // Some Rubicon apps send ADID inside dpidsha1 field
            if (dpidsha1.length() == DidEater.RAW_HEX_LENGTH && dpidsha1.charAt(8) == '-') {
                setIfaOrAdid(byydDevice, os, dpidsha1);
            } else if (rawDid == null && dpidsha1.length() == DidEater.SHA1_HEX_LENGTH) {
                // Use dpidsha1 only when ADID/IDFA does not exist
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_DPID, dpidsha1);
            }
        }

        Geo geo = rtbDevice.getGeo();
        if (geo != null) {
            BigDecimal lat = geo.getLat(), lon = geo.getLon();
            if (lat != null && lon != null) {
                byydDevice.setCoordinates(new SimpleCoordinates(lat.doubleValue(), lon.doubleValue()));
            }
        }

        return byydDevice;
    }

    protected ByydUser mapRtbUser(com.adfonic.adserver.rtb.open.v2.User rtbUser) {
        ByydUser nativeUser = new ByydUser();
        nativeUser.setUid(rtbUser.getId());
        nativeUser.setGender(rtbUser.getGender());
        if (rtbUser.getYob() != null) {
            nativeUser.setDateOfBirth(String.valueOf(rtbUser.getYob()));
        }
        Geo geo = rtbUser.getGeo();
        if (geo != null) {
            nativeUser.setCountryCode(geo.getCountry());
            nativeUser.setPostalCode(geo.getZip());
        }

        return nativeUser;
    }

    /**
     * Exchanges usually send ADID or IFA in same field and we need to distinguish between using "os" field
     * 
     * @return null or DeviceIdentifierType 
     */
    public static String setIfaOrAdid(ByydDevice byydDevice, String os, String deviceIdValue) {
        String identifierType = null;
        if (StringUtils.length(deviceIdValue) == DidEater.RAW_HEX_LENGTH && os != null && !os.isEmpty()) {
            if ("iOS".equalsIgnoreCase(os) || "iPhone".equalsIgnoreCase(os) || "iPad".equalsIgnoreCase(os)) {
                identifierType = DeviceIdentifierType.SYSTEM_NAME_IFA;
            } else if ("Android".equalsIgnoreCase(os)) {
                identifierType = DeviceIdentifierType.SYSTEM_NAME_ADID;
            }

            if (identifierType != null) {
                byydDevice.setDeviceIdentifier(identifierType, deviceIdValue);
            }
        }

        return identifierType;
    }

    protected static String getPlatformNameFromDevice(ByydDevice device) {
        String os;
        if (device == null || (os = device.getOs()) == null) {
            return null;
        }
        return StringUtils.replace(os, " ", "_");
    }
}
