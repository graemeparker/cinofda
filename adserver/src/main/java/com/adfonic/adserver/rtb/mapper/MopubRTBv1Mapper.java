package com.adfonic.adserver.rtb.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.adfonic.adserver.rtb.itlookup.OverridingCustomRangeITDeriver;
import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.ContentForm;

public class MopubRTBv1Mapper extends OpenRTBv1QuickNdirty {

    @Override
    protected com.adfonic.adserver.rtb.open.v1.Bid buildBid(ByydBid from) {
        com.adfonic.adserver.rtb.open.v1.Bid to = super.buildBid(from);
        String txtIurl = from.getTxtIUrl();
        if (txtIurl != null && to.getIurl() == null) {
            to.setIurl(txtIurl);
        }
        return to;
    }

    @Override
    protected ByydImp mapImp(com.adfonic.adserver.rtb.open.v1.Imp from, ByydRequest byydRequest) {
        ByydImp to = new ByydImp(from.getImpid());
        to.setBattr(from.getBattr());
        to.setBtype(from.getBtype());
        to.setH(from.getH());
        to.setW(from.getW());

        copyDeals(from, byydRequest);

        APIFramework api = from.getApi();
        ContentForm contentForm;
        if (api != null && (contentForm = SUPPORTED_API_MAP.get(api)) != null) {
            Set<ContentForm> contentFormList = new HashSet<ContentForm>(ByydImp.CF_MOBILE_WEB);
            contentFormList.add(contentForm);

            // for mopub richmedia is only applicable if api is present
            String displaymanagerver = from.getDisplaymanagerver();
            if (displaymanagerver != null) {
                String platformName = OpenRTBv2ByHandMapper.getPlatformNameFromDevice(byydRequest.getDevice());
                if (platformName != null) {
                    to.setIntegrationTypeDeriver(new OverridingCustomRangeITDeriver(displaymanagerver, platformName));
                }
            }
        }

        return to;
    }

    private void copyDeals(com.adfonic.adserver.rtb.open.v1.Imp rtbImp, ByydRequest byydRequest) {
        if (rtbImp.getWseat() == null || rtbImp.getWseat().isEmpty()) {
            return;
        }

        List<ByydDeal> byydDeals = new ArrayList<>();
        for (String dealId : rtbImp.getWseat()) { //seat is deal? really?
            ByydDeal deal = new ByydDeal(dealId);
            byydDeals.add(deal);
        }

        ByydMarketPlace mp = new ByydMarketPlace(byydDeals, true);
        byydRequest.setMarketPlace(mp);
    }

}
