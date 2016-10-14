package com.adfonic.adserver.rtb.open.v2;

/**
 * OpenRTB-API-Specification
 * 3.2.7 Object: App
 *
 */
public class App extends SiteOrApp {

    /**
     * Application bundle or package name (e.g., com.foo.mygame);
     * intended to be a unique ID across exchanges.
     */
    private String bundle;

    /**
     * App store URL for an installed app; for QAG 1.5 compliance
     */
    private String storeurl;

    // Unmaped: sectioncat, pagecat, ver, privacypolicy, paid, content, keywords

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getStoreurl() {
        return storeurl;
    }

    public void setStoreurl(String storeurl) {
        this.storeurl = storeurl;
    }

}
