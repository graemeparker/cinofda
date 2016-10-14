package com.adfonic.adserver;

import java.io.IOException;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.DestinationType;

/**
 * Manager that deals with image data for destination type icons
 */
public interface IconManager {
    /**
     * Get base64-encoded image data for a given destination type icon
     * 
     * @param destinationType
     *            the given DestinationType (i.e. URL, CALL, etc.)
     * @param colorScheme
     *            the desired color scheme
     * @param width
     *            the desired width (i.e. 300 or 320)
     * @param height
     *            the desired height (i.e. 50)
     * @return the respective base64-encoded image data
     * @throws IOException 
     */
    String getBase64EncodedImageData(DestinationType destinationType, ColorScheme colorScheme, int width, int height) throws IOException;
}
