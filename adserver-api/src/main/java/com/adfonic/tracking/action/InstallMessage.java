package com.adfonic.tracking.action;

import com.adfonic.tracking.TrackingMessage;

public class InstallMessage implements TrackingMessage {

    private static final long serialVersionUID = 1L;

    private String appId;

    private String udId;


    public InstallMessage(String appid, String udid) {
        this.appId = appid;
        this.udId = udid;
    }


    @Override
    public String getTrackerPath() {
        return "/is/" + appId + "/" + udId;
    }


}
