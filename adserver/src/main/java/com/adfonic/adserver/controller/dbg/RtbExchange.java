package com.adfonic.adserver.controller.dbg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.adserver.controller.rtb.ExchangeBidAdapter;
import com.adfonic.adserver.controller.rtb.MobFoxRtbController;
import com.adfonic.adserver.controller.rtb.MopubV2BidController;
import com.adfonic.adserver.controller.rtb.NexageV2Controller;
import com.adfonic.adserver.controller.rtb.OmaxRtbController;
import com.adfonic.adserver.controller.rtb.OpenRTBv2Controller;
import com.adfonic.adserver.controller.rtb.OpenRtbV1Controller;
import com.adfonic.adserver.controller.rtb.OpenXController;
import com.adfonic.adserver.controller.rtb.PubmaticRTBv2Controller;
import com.adfonic.adserver.controller.rtb.RtbEndpoint;
import com.adfonic.adserver.controller.rtb.RubiconRTBv2Controller;
import com.adfonic.adserver.controller.rtb.SmaatoBidController;
import com.adfonic.adserver.controller.rtb.YieldlabController;

/**
 * 
 * @author mvanek
 * 
 * Hardcoded here because adserver has no access into ToolsDB and values never change
 *
 */
public enum RtbExchange {

    // OpenRtbV2
    Mopub(17593, "62e77526-8f80-4224-a07e-7d851f87048e", RtbEndpoint.MopubV2, MopubV2BidController.ADAPTER), //
    Rubicon(28001, "98970e34-eb0a-4221-bb94-c10715b93d3f", RtbEndpoint.RubiconV2, RubiconRTBv2Controller.ADAPTER), //
    Nexage(7481, "924146eb-ae41-41b1-bacb-4e445faa99a3", RtbEndpoint.NexageV2, NexageV2Controller.ADAPTER), //
    Omax(34620, "a89c857a-bedb-4780-ac79-a6b72d4c069b", RtbEndpoint.Omax, OmaxRtbController.ADAPTER), //
    MobFox(34623, "e1b6a4d2-486c-4b9c-b905-6762244bbcdf", RtbEndpoint.MobFox, MobFoxRtbController.ADAPTER), //

    Pubmatic(28635, "1c2ba3d1-d8b0-42c0-88f7-09e5b8b97da4", RtbEndpoint.PubmaticV2, PubmaticRTBv2Controller.ADAPTER), //
    Smaato(24440, "09d0d0da-1762-43d3-925b-1335fa895f6b", RtbEndpoint.SmaatoV2, SmaatoBidController.ADAPTER), //
    Flurry(29418, "5e5f53af-c500-42a4-aec3-7a5ebfde64fc", RtbEndpoint.ORTBv2, OpenRTBv2Controller.ADAPTER), //

    Appnexus(34354, "f587516d-235f-41ca-aa84-954cebb7d549", RtbEndpoint.AppNexusV2, null), //
    Millennial(34381, "6a91c79e-d662-40e3-9589-d37da13760b8", RtbEndpoint.AppNexusV2, null), //
    Orange(34437, "110e68ba-9a47-4d66-9d7f-2e9d93cc4a5f", RtbEndpoint.AppNexusV2, null), //

    // OpenRtbV1
    Switch(17487, "c350591b-40ae-4e94-b2f9-6a8ae2d1c86b", RtbEndpoint.ORTBv1, OpenRtbV1Controller.ADAPTER), //
    Adiquity(17270, "b5489eb7-8300-439e-8c97-c69ff2eabc1a", RtbEndpoint.ORTBv1, OpenRtbV1Controller.ADAPTER), //
    Mobclix(23223, "bcf6be2e-f7d0-4059-b985-d1fb6c744865", RtbEndpoint.ORTBv1, OpenRtbV1Controller.ADAPTER), //

    //Protobuf
    AdX(26442, "ca6e5a4c-1d67-490c-95e2-4877cea57bb9", RtbEndpoint.DcAdX, null), //
    OpenX(27319, "c5373546-5d54-41c0-9707-0fe49fdf5863", RtbEndpoint.OpenX, OpenXController.ADAPTER), //
    Samsung(30109, "1b31e97a-f475-42de-ad9c-10dc97f0cf0a", RtbEndpoint.OpenX, OpenXController.ADAPTER), //

    //HTTP GET request
    YieldLab(32457, "3d36b3c5-b513-4bf2-bc91-f39ec6495e7b", RtbEndpoint.YieldLab, YieldlabController.ADAPTER), //

    ByydTest(34512, "ec272f51-d2ca-49ff-ab3b-5b968771d96d", RtbEndpoint.ByydTest, MopubV2BidController.ADAPTER), //
    Unknown(-1, "12345678-1234-1234-1234-123456789012", RtbEndpoint.ORTBv2, OpenRTBv2Controller.ADAPTER); //

    private static final Logger logger = LoggerFactory.getLogger(RtbExchange.class);

    private static final Map<Long, RtbExchange> byId = Collections.unmodifiableMap(initializeById());
    private static final Map<String, RtbExchange> byExteralId = Collections.unmodifiableMap(initializeByExternalId());

    private final String publisherExternalId;

    private final Long publisherId;

    private final RtbEndpoint endpoint;

    protected ExchangeBidAdapter<?, ?> adapter;

    private RtbExchange(long publisherId, String publisherExternalId, RtbEndpoint endpoint, ExchangeBidAdapter<?, ?> adapter) {
        this.publisherId = publisherId;
        if (publisherExternalId == null || publisherExternalId.isEmpty()) {
            throw new IllegalArgumentException("Null or empty publisherExternalId: " + publisherExternalId);
        }
        this.publisherExternalId = publisherExternalId;
        Objects.requireNonNull(endpoint);
        this.endpoint = endpoint;

        this.adapter = adapter;
    }

    private static Map<String, RtbExchange> initializeByExternalId() {
        HashMap<String, RtbExchange> map = new HashMap<String, RtbExchange>();
        for (RtbExchange item : RtbExchange.values()) {
            map.put(item.getPublisherExternalId(), item);
        }
        return map;
    }

    private static Map<Long, RtbExchange> initializeById() {
        HashMap<Long, RtbExchange> map = new HashMap<Long, RtbExchange>();
        for (RtbExchange item : RtbExchange.values()) {
            map.put(item.getPublisherId(), item);
        }
        return map;
    }

    public long getPublisherId() {
        return this.publisherId;
    }

    public String getPublisherExternalId() {
        return publisherExternalId;
    }

    public RtbEndpoint getEndpoint() {
        return endpoint;
    }

    public ExchangeBidAdapter<?, ?> getAdapter() {
        return adapter;
    }

    /**
     * Some exchanges has parametereized adapters and must be creatd externaly
     * As we cannot create them during enum construction, we need  an set here
     */
    public void setAdapter(ExchangeBidAdapter<?, ?> adapter) {
        if (this.adapter != null) {
            logger.error("Do not do that!", new IllegalArgumentException("Adapter already set for " + name() + " to " + this.adapter));
        }
        this.adapter = adapter;
    }

    public static RtbExchange lookup(String publisherExtId) {
        return byExteralId.get(publisherExtId);
    }

    public static RtbExchange getByPublisherId(Long publisherId) {
        return byId.get(publisherId);
    }

}
