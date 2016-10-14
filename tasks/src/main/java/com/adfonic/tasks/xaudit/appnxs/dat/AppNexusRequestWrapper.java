package com.adfonic.tasks.xaudit.appnxs.dat;

public class AppNexusRequestWrapper {

    private AppNexusCreativeRecord creative;

    public AppNexusRequestWrapper() {
        //default
    }

    public AppNexusRequestWrapper(AppNexusCreativeRecord creative) {
        this.creative = creative;
    }

    public AppNexusCreativeRecord getCreative() {
        return creative;
    }

    public void setCreative(AppNexusCreativeRecord creative) {
        this.creative = creative;
    }

}
