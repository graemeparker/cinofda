package com.adfonic.tasks.xaudit.appnxs.dat;

public class AppNexusAuthResponseWrapper {

    private AppNexusAuthResponse response;

    public AppNexusAuthResponse getResponse() {
        return response;
    }

    public void setResponse(AppNexusAuthResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "Wrapper { response=" + response + "}";
    }

}
