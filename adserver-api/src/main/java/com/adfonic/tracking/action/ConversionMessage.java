package com.adfonic.tracking.action;

import com.adfonic.tracking.TrackingMessage;

public class ConversionMessage implements TrackingMessage {

    private static final long serialVersionUID = 1L;

    private String clickExternalID;


    public ConversionMessage(String clickExternalID) {
        this.clickExternalID = clickExternalID;
    }


    @Override
    public String getTrackerPath() {
        return "/cs/" + clickExternalID;
    }


}
