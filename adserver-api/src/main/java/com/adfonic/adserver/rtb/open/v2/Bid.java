package com.adfonic.adserver.rtb.open.v2;

import java.util.ArrayList;
import java.util.List;

public class Bid extends com.adfonic.adserver.rtb.open.v1.Bid {

    private String id;

    private static int instcnt;

    private List<String> adomain;

    private List<String> cat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCat() {
        return cat;
    }

    public void setCat(List<String> cat) {
        this.cat = cat;
    }

    public void v2() {
        id = String.valueOf(instcnt++);// doesn't matter give them something - coz it needs to be unique within multiple bids of an impression - we give one

        String adomain = (String) super.getAdomain();
        if (adomain != null) {
            this.adomain = new ArrayList<String>();
            this.adomain.add(adomain);
        }
    }

    @Override
    public Object getAdomain() {
        return adomain == null ? super.getAdomain() : adomain;
    }

}
