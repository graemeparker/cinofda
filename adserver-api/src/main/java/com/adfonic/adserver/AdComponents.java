package com.adfonic.adserver;

import java.util.Map;

import com.adfonic.domain.DestinationType;

public interface AdComponents {

    /** @return the Format.systemName that is being used for this ad */
    String getFormat();

    void setFormat(String format);

    DestinationType getDestinationType();

    void setDestinationType(DestinationType destinationType);

    String getDestinationUrl();

    void setDestinationUrl(String destinationUrl);

    Map<String, Map<String, String>> getComponents();
}
