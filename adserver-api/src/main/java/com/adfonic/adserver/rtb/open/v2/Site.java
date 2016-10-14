package com.adfonic.adserver.rtb.open.v2;

/**
 * OpenRTB-API-Specification
 * 3.2.6 Object: Site
 *
 */
public class Site extends SiteOrApp {

    /**
     * URL of the page where the impression will be shown.
     */
    private String page;

    // Unmaped: sectioncat, pagecat, ref, search, mobile, privacypolicy, content, keywords

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

}
