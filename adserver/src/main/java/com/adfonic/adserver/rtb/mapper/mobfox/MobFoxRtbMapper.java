package com.adfonic.adserver.rtb.mapper.mobfox;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.rtb.MobFoxRtbController;
import com.adfonic.adserver.impl.DidEater;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.mapper.OpenRTBv2ByHandMapper;
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxBidRequest.MobFoxBidRequestExt;
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxBidRequest.MobFoxUdi;
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxImp.MobFoxImpExt;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.Device;
import com.adfonic.adserver.rtb.open.v2.Imp;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.DeviceIdentifierType;

/**
 * MobFox
 * http://docs.mobfox.com/docs/integrate-an-a-dsp 
 * http://www.mobfox.com/dsp-resource-center/ 
 * 
 * OpenRtb 2.1 Exchange with extensions @link {@link MobFoxRtbController}
 */
public class MobFoxRtbMapper extends OpenRTBv2ByHandMapper {

    private static final String RTB_ID_PUBLISHER = "MobFox";

    private static final MobFoxRtbMapper instance = new MobFoxRtbMapper();

    public static MobFoxRtbMapper instance() {
        return instance;
    }

    private MobFoxRtbMapper() {
        // No outsider instances
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends Imp> rtbRequest, RtbBidEventListener listener)
            throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExternalId, rtbRequest, listener);
        setExchangeBasedRtbId(RTB_ID_PUBLISHER, rtbRequest, byydRequest);
        return byydRequest;
    }

    /**
     * http://www.mobfox.com/dsp-resource-center/
     */
    @Override
    protected ByydDevice mapRtbDevice(Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {

        ByydDevice byydDevice = super.mapRtbDevice(rtbDevice, rtbRequest, byydRequest);

        MobFoxBidRequestExt extension = ((MobFoxBidRequest) rtbRequest).getExt();
        MobFoxUdi udi;
        if (extension != null && (udi = extension.getUdi()) != null) {
            String idfa = udi.getIdfa();
            if (StringUtils.isNotEmpty(idfa) && idfa.length() == DidEater.RAW_HEX_LENGTH) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IFA, idfa);
            }
            String gaid = udi.getGaid();
            if (StringUtils.isNotEmpty(gaid) && gaid.length() == DidEater.RAW_HEX_LENGTH) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, gaid);
            }
            // Some publications are sending ADID inside androidid
            String androidId = udi.getAndroidid();
            if (StringUtils.isNotEmpty(androidId) && androidId.length() == DidEater.ANDROID_ID_HEX_LENGTH) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ANDROID, androidId);
            }
        }

        return byydDevice;
    }

    @Override
    @Deprecated
    protected ByydImp mapRtbImp(Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = super.mapRtbImp(rtbImp, byydRequest);

        MobFoxImpExt impExt = ((MobFoxImp) rtbImp).getExt();
        if (impExt != null) {
            byydImp.setStrictBannerSize(Constant.ONE.equals(impExt.getStrictbannersize()));
        }

        // XXX Temporarily here to disable complex ad formats by default 
        if (rtbImp.getVideo() != null && !AdServerFeatureFlag.MOBFOX_VIDEO.isEnabled()) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AsCounter.MobFoxVideo);
        }
        if (rtbImp.getNative() != null && !AdServerFeatureFlag.MOBFOX_NATIVE.isEnabled()) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AsCounter.MobFoxNative);
        }
        return byydImp;
    }

}
