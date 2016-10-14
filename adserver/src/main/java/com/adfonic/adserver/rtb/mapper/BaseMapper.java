package com.adfonic.adserver.rtb.mapper;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.BaseRequest;
import com.adfonic.adserver.rtb.nativ.BaseResponse;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

public interface BaseMapper<K extends BaseRequest, V extends BaseResponse> {

    ByydRequest mapRtbRequest(String publisherExternalId, K request, RtbBidEventListener listener) throws NoBidException;

    V mapRtbResponse(ByydResponse byydResponse, ByydRequest byydRequest);

    V mapRtbResponse(ByydResponse byydResponse, V bidResponse);

}
