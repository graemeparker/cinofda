package com.adfonic.domain;

/**
 * A number of media types are available and equate roughly to a particular
 * representation of an ad: Banner (HTML), Text (HTML), HTML, HTML+JS.
 * Where the first two are "advertising like we know it" types, and the
 * further two are potentially rich media or tag types outside of the
 * image/text + banner type. The explicit recognition of these types
 * allows publishers to demand certain transformations at serve time,
 * most likely to do with mediation.
 */
public enum MediaType {
    BANNER_HTML(false),
    TEXT_HTML(false),
    HTML_JS(true),
    HTML(true),
    VAST_XML_2_0(false),
    STAND_JS(true);

    private final boolean markupRequired;
    
    private MediaType(boolean markupRequired) {
        this.markupRequired = markupRequired;
    }

    public boolean isMarkupRequired() {
        return markupRequired;
    }
}