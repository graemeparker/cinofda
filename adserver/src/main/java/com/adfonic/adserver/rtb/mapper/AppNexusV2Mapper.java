package com.adfonic.adserver.rtb.mapper;

import static com.adfonic.adserver.cst.AppNexusShared.ADSPACE_ID_MACRO;
import static com.adfonic.adserver.cst.AppNexusShared.END_POINT_MACRO;
import static com.adfonic.adserver.cst.AppNexusShared.IMPRESSION_ID_MACRO;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v2.App;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.Device;
import com.adfonic.adserver.rtb.open.v2.Publisher;
import com.adfonic.adserver.rtb.open.v2.Site;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid.AppNexusExtWrap;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid.CustomMacro;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest.AppNexusUdi;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest.RequestExt;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusImp;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusSeatBid;

@Component
@Qualifier("appnexusv2Mapper")
@SuppressWarnings("rawtypes")
public class AppNexusV2Mapper extends OpenRTBv2ByHandMapper {

    private final String seatId;

    private final String endPointMacro;

    @Autowired
    public AppNexusV2Mapper(@Value("${Rtb.seat.id.appnxs}") String seatId, @Value("${Rtb.appnxs.end.point.macro}") String endPointMacro) {
        super(new AppNexusV1Mapper(endPointMacro));
        this.endPointMacro = endPointMacro;
        this.seatId = seatId;
    }

    /**
     * https://wiki.appnexus.com/display/adnexusdocumentation/Bid+Request
     */
    @Override
    public ByydRequest mapRtbRequest(String publisherExtId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExtId, rtbRequest, listener);

        RequestExt appNxsExt = ((AppNexusBidRequest) rtbRequest).getExt();
        if (appNxsExt == null || appNxsExt.getAppnexus() == null || appNxsExt.getAppnexus().getSeller_member_id() == null) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_SELL_ID, "seller_member_id");
        }

        byydRequest.setAssociateReference(appNxsExt.getAppnexus().getSeller_member_id().toString());

        App app = rtbRequest.getApp();
        String publicationRtbId;
        if (app != null) {
            // For AppNexus use app.bundle rather than app.id in RtbId
            if (app.getBundle() != null) {
                byydRequest.setPublicationUrlString("bundle: " + app.getBundle());
                Publisher publisher = app.getPublisher();
                if (publisher != null) {
                    publicationRtbId = publisher.getId() + "-" + app.getBundle();
                } else {
                    publicationRtbId = "APNXAPP-" + app.getBundle();
                }
            } else {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_PUBL_ID, "app.bundle");
            }
        } else {
            Site site = rtbRequest.getSite();
            Publisher publisher = site.getPublisher();
            if (publisher != null) {
                publicationRtbId = publisher.getId() + "-" + site.getId();
            } else {
                publicationRtbId = "APNXSITE-" + site.getId();
            }
        }
        byydRequest.setPublicationRtbId(publicationRtbId);

        return byydRequest;
    }

    /**
     * https://wiki.appnexus.com/pages/viewpage.action?pageId=58656730
     */
    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, ByydRequest byydRequest) {

        AppNexusBidResponse rtbResponse = new AppNexusBidResponse();
        AppNexusSeatBid appNexusSeatBid = rtbResponse.getSeatbid().get(0);
        appNexusSeatBid.setSeat(seatId); // Mandatory - This is AppNexus's member_id.
        // Standard response mapping... 
        super.mapRtbResponse(byydResponse, rtbResponse);

        AppNexusBid rtbBid = appNexusSeatBid.getBid().get(0);
        AppNexusExtWrap appnexusExt = rtbBid.getExt().getAppnexus();
        // We use Creative external id in Appnexus creative approval service
        appnexusExt.setCrcode(byydResponse.getBid().getCreative().getExternalID());
        List<CustomMacro> customMacros = appnexusExt.getCustom_macros();
        customMacros.add(new CustomMacro(ADSPACE_ID_MACRO, byydRequest.getAdSpace().getExternalID()));
        customMacros.add(new CustomMacro(END_POINT_MACRO, endPointMacro));

        for (CustomMacro macro : customMacros) {
            if (IMPRESSION_ID_MACRO.equalsIgnoreCase(macro.getName())) {
                rtbBid.setImpid(macro.getValue());
            }
        }

        return rtbResponse;
    }

    @Override
    protected ByydImp mapRtbImp(com.adfonic.adserver.rtb.open.v2.Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = super.mapRtbImp(rtbImp, byydRequest);
        // AppNexus sends nonstandard imp.reserve_price attribute instead of imp.bidfloor
        // MAD-2132: AppNexus Bid Floors
        BigDecimal reservedPrice = ((AppNexusImp) rtbImp).getReserve_price();
        if (reservedPrice != null) {
            byydImp.setBidfloor(reservedPrice);
        }
        return byydImp;
    }

    @Override
    protected ByydDevice mapRtbDevice(Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {
        ByydDevice byydDevice = super.mapRtbDevice(rtbDevice, rtbRequest, byydRequest);
        AppNexusBidRequest appNexusRequest = (AppNexusBidRequest) rtbRequest;
        if (appNexusRequest.getExt() != null && appNexusRequest.getExt().getUdi() != null) {
            AppNexusUdi udi = appNexusRequest.getExt().getUdi();
            OpenRTBv2ByHandMapper.setIfaOrAdid(byydDevice, rtbDevice.getOs(), udi.getIdfa());
        }
        return byydDevice;
    }
}
