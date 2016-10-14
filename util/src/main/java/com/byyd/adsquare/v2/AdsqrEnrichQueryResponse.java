package com.byyd.adsquare.v2;

import java.util.List;

public class AdsqrEnrichQueryResponse {

    private List<Integer> audiences;

    AdsqrEnrichQueryResponse() {
        // json
    }

    public AdsqrEnrichQueryResponse(List<Integer> audiences) {
        this.audiences = audiences;
    }

    public List<Integer> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<Integer> audiences) {
        this.audiences = audiences;
    }

    @Override
    public String toString() {
        return "AdsqrEnrichQueryResponse {audiences=" + audiences + "}";
    }

}
