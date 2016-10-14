package com.adfonic.adserver.rtb.nativ;

import java.util.List;

public class ByydResponse extends ByydBase {

    private String id;

    private ByydRequest byydRequest;

    private String bidid;

    private ByydBid bid;

    private String bidCurrencyIso4217;

    private List<String> impTrackList;

    ByydResponse() {
        // marshalling
    }

    public ByydResponse(ByydRequest byydRequest, ByydBid bid) {
        this.byydRequest = byydRequest;
        this.id = byydRequest.getId();
        this.bid = bid;
        this.bidid = bid.getAdid(); // impression external id // FastUUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public ByydRequest getByydRequest() {
        return byydRequest;
    }

    public String getBidid() {
        return bidid;
    }

    /*
        public void setBidid(String bidid) {
            this.bidid = bidid;
        }
    */
    public ByydBid getBid() {
        return bid;
    }

    public String getBidCurrencyIso4217() {
        return bidCurrencyIso4217;
    }

    public void setBidCurrencyIso4217(String bidCurrencyIso4217) {
        this.bidCurrencyIso4217 = bidCurrencyIso4217;
    }

    public void setImpTrackUrls(List<String> impTrackList) {
        this.impTrackList = impTrackList;
    }

    public List<String> getImpTrackUrls() {
        return impTrackList;
    }

}
