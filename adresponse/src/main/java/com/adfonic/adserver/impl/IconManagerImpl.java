package com.adfonic.adserver.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.IconManager;
import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.DestinationType;
import com.adfonic.util.Base64;

@Component
public class IconManagerImpl implements IconManager {

    private static final transient Logger LOG = Logger.getLogger(IconManagerImpl.class.getName());

    static final String RESOURCE_PATH = "/images/text-ad-icons/";
    static final String EXTENSION = ".gif";

    ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

    @PostConstruct
    public void preLoadIconImages() {
        // Pre-load all of the images
        LOG.info("Pre-loading all icon images");
        for (DestinationType destinationType : DestinationType.values()) {
            for (ColorScheme colorScheme : ColorScheme.values()) {
                try {
                    getBase64EncodedImageData(destinationType, colorScheme, 300, 50);
                    getBase64EncodedImageData(destinationType, colorScheme, 320, 50);
                } catch (Exception e) {
                    LOG.warning("Failed to pre-load icon image data for " + destinationType + " + " + colorScheme);
                }
            }
        }
    }

    static String getFileBaseName(DestinationType destinationType, ColorScheme colorScheme, int width, int height) {
        return destinationType.name() + "-" + colorScheme.name() + "-" + width + "x" + height;
    }

    static String getResourcePath(String fileBaseName) {
        return RESOURCE_PATH + fileBaseName + EXTENSION;
    }

    /** @throws IOException 
     * @{inheritDoc} */
    @Override
    public String getBase64EncodedImageData(DestinationType destinationType, ColorScheme colorScheme, int width, int height) throws IOException {
        String fileBaseName = getFileBaseName(destinationType, colorScheme, width, height);

        String base64Encoded = cache.get(fileBaseName);
        if (base64Encoded != null) {
            return base64Encoded;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading " + fileBaseName);
        }
        String path = getResourcePath(fileBaseName);
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Classpath resource not found: " + path);
        }
        byte[] rawImageData = IOUtils.toByteArray(stream);
        base64Encoded = Base64.encode(rawImageData);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Caching " + fileBaseName);
        }
        cache.put(fileBaseName, base64Encoded);
        return base64Encoded;
    }
}
