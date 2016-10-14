package com.adfonic.adserver.rtb.smaato;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.impl.DidEater;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.itlookup.FallbackDirectITDeriver;
import com.adfonic.adserver.rtb.mapper.OpenRTBv2ByHandMapper;
import com.adfonic.adserver.rtb.nativ.AdType;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.Device;
import com.adfonic.adserver.rtb.smaato.SmaatoImp.SmaatoImpExtension;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;

/**
 * http://dspportal.smaato.com/documentation
 *
 */
public class SmaatoBidMapper extends OpenRTBv2ByHandMapper {

    private static final SmaatoBidMapper instance = new SmaatoBidMapper();

    public static SmaatoBidMapper instance() {
        return instance;
    }

    private SmaatoBidMapper() {
        //no outside made instances
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExternalId, rtbRequest, listener);
        /**
         * Historicaly Smaato used app.id for their adspace id value and we used this value inside RTB_ID. 
         * Later they start to send imp.tagid with same value and yet later they changed app.id to send Smaato's application id, which is different from adspace id. 
         * To keep our publication mapping via RTB_ID same, we are using imp.tagid
         */
        String tagid = rtbRequest.getImp().get(0).getTagid();
        byydRequest.setPublicationRtbId((byydRequest.getMedium() == Medium.APPLICATION ? "RTB2AID" : "RTB2SID") + "-" + tagid); // Arrrrgh. Again possibly conflictiog RTB_ID...
        return byydRequest;
    }

    @Override
    protected ByydImp mapRtbImp(com.adfonic.adserver.rtb.open.v2.Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = super.mapRtbImp(rtbImp, byydRequest);
        SmaatoImpExtension impExt = ((SmaatoImp) rtbImp).getExt();
        if (impExt != null) {
            byydImp.setStrictBannerSize(impExt.isStrictbannersize());
        }

        Set<AdType> blockedTypes = byydImp.getBtype();
        if (blockedTypes != null && blockedTypes.contains(AdType.JAVASCRIPT_AD)) {
            byydImp.setBlockExtendedCreatives(true);
        } else {
            String displaymanager = rtbImp.getDisplaymanager();
            String displaymanagerver = rtbImp.getDisplaymanagerver();
            if (displaymanager != null && displaymanagerver != null) {
                String platformName = getPlatformNameFromDevice(byydRequest.getDevice());
                // .imp.setIntegrationTypeDeriver(new
                // DirectITDeriver(displaymanager + "/" + displaymanagerver));
                byydImp.setIntegrationTypeDeriver(new FallbackDirectITDeriver(displaymanager, displaymanagerver, platformName != null ? platformName : "unknown_P9"));
            }
        }
        byydImp.bypassCFRestrictions(true);
        return byydImp;
    }

    @Override
    protected ByydDevice mapRtbDevice(Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {
        ByydDevice byydDevice = super.mapRtbDevice(rtbDevice, rtbRequest, byydRequest);
        SmaatoBidRequest smaatoReq = (SmaatoBidRequest) rtbRequest;
        if (smaatoReq.getExt() != null && smaatoReq.getExt().getUdi() != null) {
            SmaatoUdi udi = smaatoReq.getExt().getUdi();
            String idfa = udi.getIdfa();
            String adid;
            if (udi.trackIdfa() && StringUtils.length(idfa) == DidEater.RAW_HEX_LENGTH) {
                // common iOS
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IFA, idfa);
            } else if (udi.trackAdid() && StringUtils.length(adid = udi.getGoogleadid()) == DidEater.RAW_HEX_LENGTH) {
                // common android
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, adid);
            }
        }
        return byydDevice;
    }
}
