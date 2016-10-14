package com.adfonic.adserver.rtb.open.v2;

/**
 * OpenRTB-API-Specification
 * 3.2.8 Object: Publisher
 *
 */
public class Publisher {

    /**
     * Exchange-specific publisher ID
     */
    private String id;

    /**
     * Publisher name (may be aliased at the publisher’s request).
     */
    private String name;

    // Unmaped: cat, domain, ext

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
