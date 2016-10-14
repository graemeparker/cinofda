package com.adfonic.adserver.rtb.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.open.v1.App;
import com.adfonic.adserver.rtb.open.v1.Restrictions;
import com.adfonic.adserver.rtb.open.v1.RtbPublication;
import com.adfonic.adserver.rtb.open.v1.SeatBid;
import com.adfonic.adserver.rtb.open.v1.Site;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.geo.SimpleCoordinates;
import com.google.common.collect.ImmutableMap;

public class OpenRTBv1QuickNdirty implements BaseMapper<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> {

    private static final transient Logger LOG = Logger.getLogger(OpenRTBv1QuickNdirty.class.getName());

    public static final OpenRTBv1QuickNdirty instance = new OpenRTBv1QuickNdirty();

    public static OpenRTBv1QuickNdirty getInstance() {
        return instance;
    }

    protected OpenRTBv1QuickNdirty() {
        //only subclasses instances
    }

    protected static final Map<APIFramework, ContentForm> SUPPORTED_API_MAP = ImmutableMap.of(APIFramework.MRAID, ContentForm.MRAID_1_0);

    // These are quick hotfixed stuff.
    // Warning: when changing values for these, remember, the variables names reflect the version order
    // The variable names are in the format OLDEST_N_.*
    //  The code above is dependent on the order of the version for reducing costly version comparisons
    private static final String OLDEST_1_INMOBI_SUPPORTED_MOPUB_SDK_VER = "1.9";// inmobi/sprout - AI-202
    private static final String OLDEST_2_CELTRAV3_SUPPORTED_MOPUB_SDK_VER = "1.10";

    // just 2 of them. so preloading maps for some speed
    private static final Set<String> VENDORS_CELTRAV3_FIN = new HashSet<>();
    static {
        VENDORS_CELTRAV3_FIN.add("CeltraV3");
    }
    private static final Set<String> VENDORS_CELTRAV3_INMOBI_FIN = new HashSet<>();
    static {
        VENDORS_CELTRAV3_INMOBI_FIN.add("CeltraV3");
        VENDORS_CELTRAV3_INMOBI_FIN.add("InMobi");
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v1.BidRequest rtbRequest, RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = new ByydRequest(publisherExternalId, rtbRequest.getId());
        extractPubRtbIdetc(publisherExternalId, rtbRequest, listener, byydRequest);

        byydRequest.setTmax(rtbRequest.getTmax());
        byydRequest.setDevice(getNativeDevice(rtbRequest.getDevice()));
        byydRequest.setUser(getNativeUser(rtbRequest.getUser()));
        Restrictions restriction = rtbRequest.getRestrictions();
        if (restriction != null) {
            byydRequest.setBlockedCategoryIabIds(restriction.getBcat());
            byydRequest.setBlockedAdvertiserDomains(restriction.getBadv());
        }

        // Do NOT iterate imp list and go for first element directly
        // 1. Exchanges allways send only one imp (although this is up to them and they can change it)
        // 2. Targeting and rendering code cannot handle multiple bids anyway (only one Impression is stored, etc etc)
        List<com.adfonic.adserver.rtb.open.v1.Imp> rtbImpList = rtbRequest.getImp();
        if (rtbImpList != null && rtbImpList.size() != 0) {
            com.adfonic.adserver.rtb.open.v1.Imp rtbImp = rtbImpList.get(0);
            ByydImp byydImp = mapImp(rtbImp, byydRequest);
            byydImp.setBidfloor(rtbRequest.getPf());
            byydRequest.setImp(byydImp);
        } else {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request.imp");
        }

        return byydRequest;
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse nativeResponse, ByydRequest byydRequest) {
        com.adfonic.adserver.rtb.open.v1.BidResponse bidResponse = new com.adfonic.adserver.rtb.open.v1.BidResponse();
        List<SeatBid> seatbidList = new ArrayList<SeatBid>(1); // initialCapacity for ++performance
        seatbidList.add(new SeatBid());
        bidResponse.setSeatbid(seatbidList);

        return mapRtbResponse(nativeResponse, bidResponse);
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, com.adfonic.adserver.rtb.open.v1.BidResponse rtbResponse) {
        List<com.adfonic.adserver.rtb.open.v1.Bid> bidList = Arrays.asList(buildBid(byydResponse.getBid()));
        ((SeatBid) rtbResponse.getSeatbid().get(0)).setBid(bidList);
        rtbResponse.setId(byydResponse.getId());
        rtbResponse.setBidid(byydResponse.getBidid());
        String bidCurrency = byydResponse.getBidCurrencyIso4217();
        if (bidCurrency != null && !bidCurrency.equals(Constant.USD)) {
            rtbResponse.setCur(bidCurrency);
        }

        return rtbResponse;
    }

    protected com.adfonic.adserver.rtb.open.v1.Bid buildBid(ByydBid byydBid) {
        // Use v2 Bid so as to reuse v1 mapper for v2 mapping. The id field will be null and hence not serialized
        // TODO - bifurcate code when necessary
        return mapBid(byydBid, new com.adfonic.adserver.rtb.open.v2.Bid());
    }

    protected com.adfonic.adserver.rtb.open.v1.Bid mapBid(ByydBid byydBid, com.adfonic.adserver.rtb.open.v1.Bid rtbBid) {
        rtbBid.setAdid(byydBid.getAdid());
        rtbBid.setAdm(byydBid.getAdm());
        rtbBid.setAdomain(byydBid.getAdomain());
        rtbBid.setAttr(byydBid.getAttr());
        rtbBid.setCid(byydBid.getCid());
        rtbBid.setCrid(byydBid.getCrid());
        rtbBid.setDestination(byydBid.getDestination());
        rtbBid.setImpid(byydBid.getImpid());
        rtbBid.setIurl(byydBid.getIurl());
        rtbBid.setNurl(byydBid.getNurl());
        rtbBid.setPrice(byydBid.getPrice());

        return rtbBid;
    }

    protected ByydImp mapImp(com.adfonic.adserver.rtb.open.v1.Imp rtbImp, ByydRequest byydRequest) {
        ByydImp to = new ByydImp(rtbImp.getImpid());
        to.setBattr(rtbImp.getBattr());
        to.setBtype(rtbImp.getBtype());
        to.setH(rtbImp.getH());
        to.setW(rtbImp.getW());

        APIFramework api = rtbImp.getApi();
        ContentForm contentForm;
        if (api != null && (contentForm = SUPPORTED_API_MAP.get(api)) != null) {
            Set<ContentForm> contentFormList = new HashSet<ContentForm>(ByydImp.CF_MOBILE_WEB);
            contentFormList.add(contentForm);

            // for mopub richmedia is only applicable if api is present
            String displaymanagerver = rtbImp.getDisplaymanagerver();
            if (displaymanagerver == null) {
                to.setBlockedExtendedCreativeTypes(VENDORS_CELTRAV3_INMOBI_FIN);
            } else if (compareVersions(displaymanagerver, OLDEST_2_CELTRAV3_SUPPORTED_MOPUB_SDK_VER) < 0) {
                if (compareVersions(displaymanagerver, OLDEST_1_INMOBI_SUPPORTED_MOPUB_SDK_VER) < 0) {
                    to.setBlockedExtendedCreativeTypes(VENDORS_CELTRAV3_INMOBI_FIN);
                } else {
                    to.setBlockedExtendedCreativeTypes(VENDORS_CELTRAV3_FIN);
                }
            }
        }

        return to;
    }

    private static int compareVersions(String ver, String baseVer) {
        String[] verSeq = ver.split("\\.");
        String[] baseVerSeq = baseVer.split("\\.");
        int vSl = verSeq.length, bVSl = baseVerSeq.length, n = vSl < bVSl ? vSl : bVSl, i = -1;
        while (++i < n) {
            if (!verSeq[i].equals(baseVerSeq[i])) {
                return Integer.parseInt(verSeq[i]) - Integer.parseInt(baseVerSeq[i]);
            }
        }
        return vSl - bVSl;
    }

    private void extractPubRtbIdetc(String publisherExternalId, com.adfonic.adserver.rtb.open.v1.BidRequest rtbRequest, RtbBidEventListener listener, ByydRequest byydRequest)
            throws NoBidException {
        // Look for either "site" or "app" (only one will be supplied per the OpenRTB spec)
        RtbPublication siteOrApp;
        String siteOrAppId;
        String page = null;
        if ((siteOrApp = rtbRequest.getSite()) != null) {
            Site site = (Site) siteOrApp;
            siteOrAppId = site.getSid();
            // Only "site" has a "page" URL...see if it was supplied
            page = site.getPage();
        } else if ((siteOrApp = rtbRequest.getApp()) != null) {
            siteOrAppId = ((App) siteOrApp).getAid();
            byydRequest.setMedium(Medium.APPLICATION);
        } else {
            if (listener != null) {
                listener.bidRequestRejected(publisherExternalId, rtbRequest.getId(), "request app/site");
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request app/site");
        }

        // Fall back on the site or app name if the id wasn't specified (unlikely)
        if (StringUtils.isEmpty(siteOrAppId)) {
            siteOrAppId = siteOrApp.getName();
            if (StringUtils.isEmpty(siteOrAppId)) {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_PUBL_ID, "No id or name");
            }
        }

        // Determine the id of the RTB publication
        String pid = getRtbApplicationId(siteOrApp, byydRequest, listener);

        // Fall back on the optional "domain"
        byydRequest.setPublicationUrlString(page != null ? page : siteOrApp.getDomain());
        byydRequest.setIabIds(siteOrApp.getCat());
        byydRequest.setPub(siteOrApp.getPub());
        byydRequest.setPublicationName(siteOrApp.getName());

        // Try to look up the Publication. Originally we had one Publication per pid,
        // and one AdSpace per sid/aid, but we've changed that around now so that we
        // have one Publication per pid+sid/aid combo...and there just happens to be
        // one and only one AdSpace hanging off it. Construct the Publication.rtbId
        // value from the pid + sid/aid.
        byydRequest.setPublicationRtbId(pid + "-" + siteOrAppId);
    }

    /**
     * get RtbApplicationId
     * 
     * @param siteOrApp
     * @param publisherExternalID
     * @return
     * @throws NoBidException
     */
    private String getRtbApplicationId(RtbPublication siteOrApp, ByydRequest byydRequest, RtbBidEventListener listener) throws NoBidException {
        String pid = siteOrApp.getPid();
        if (StringUtils.isEmpty(pid)) {
            // Fall back on the pub name
            pid = siteOrApp.getPub();
            if (StringUtils.isEmpty(pid)) {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "app/site pid or pub");
            }
        }
        return pid;
    }

    private ByydDevice getNativeDevice(com.adfonic.adserver.rtb.open.v1.Device device) {
        // SC-319
        if (device == null) {
            return null;
        }

        ByydDevice nativeDevice = new ByydDevice();
        nativeDevice.setIp(device.getIp());
        nativeDevice.setUserAgent(device.getUa());
        nativeDevice.setOs(device.getOs());

        if (StringUtils.isNotEmpty(device.getLoc())) {
            try {
                nativeDevice.setCoordinates(new SimpleCoordinates(device.getLoc()));
            } catch (SimpleCoordinates.InvalidCoordinatesException e) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for \"device.loc\": " + e.getMessage());
                }
            }
        }

        copyDeviceIdentifiers(device, nativeDevice);

        // NOTE: fallback is still handled by RtbLogicImpl

        return nativeDevice;
    }

    protected void copyDeviceIdentifiers(com.adfonic.adserver.rtb.open.v1.Device device, ByydDevice nativeDevice) {
        nativeDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_DPID, device.getDpid());
        nativeDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ODIN_1, device.getNex_dmac());
        if (device != null && device.getAdid() != null)
            nativeDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, device.getAdid());

        // TODO: revisit this, since according to AI-78 we're supposed to use
        // nex_ifaclr for frequency capping even if nex_ifatrk = 0.  I'm just
        // replicating what Anish had in RtbLogicImpl here for now.
        if (device.getNex_ifatrk()) {
            nativeDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IFA, device.getNex_ifaclr());
        }
    }

    private ByydUser getNativeUser(com.adfonic.adserver.rtb.open.v1.User user) {
        // SC-319
        if (user == null) {
            return null;
        }

        ByydUser nativeUser = new ByydUser();
        nativeUser.setUid(user.getUid());
        if (user.getYob() != null) {
            // NOTE: no need to add "1231" to the end (see DateOfBirthDeriver)
            nativeUser.setDateOfBirth(String.valueOf(user.getYob()));
        }
        nativeUser.setGender(user.getGender());
        nativeUser.setPostalCode(user.getZip());
        nativeUser.setCountryCode(user.getCountry());
        nativeUser.setState(user.getNex_state());
        nativeUser.setDma(user.getNex_dma());
        return nativeUser;
    }

}
