package com.adfonic.asset.controller;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.domain.Asset;
import com.adfonic.util.ImageUtils;
import com.adfonic.util.ImageUtils.BorderMode;
import com.byyd.middleware.creative.service.AssetManager;

@Controller
public class AssetController {
    private static final transient Logger LOG = Logger.getLogger(AssetController.class.getName());

    // http://www.faqs.org/rfcs/rfc2616.html
    private static final FastDateFormat EXPIRES_FORMAT = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss z", TimeZone.getTimeZone("GMT"));

    // For solid rectangular single-color borders, use 75% opacity, semi-translucent
    static final float DEFAULT_ALPHA_SOLID = 0.75f;

    // When we're doing our own average, don't apply any translucency
    static final float DEFAULT_ALPHA_AVERAGE = 1.00f;

    private static final int FONT_SIZE_12 = 12;

    static final Map<BorderMode, Float> DEFAULT_ALPHA_BY_BORDER_MODE = new HashMap<>();
    static {
        DEFAULT_ALPHA_BY_BORDER_MODE.put(BorderMode.SOLID, DEFAULT_ALPHA_SOLID);
        DEFAULT_ALPHA_BY_BORDER_MODE.put(BorderMode.AVERAGE, DEFAULT_ALPHA_AVERAGE);
    }

    static final String AUTO = "auto"; // clients can pass ?b=auto

    private final AssetManager assetManager;
    private final ImageUtils imageUtils;
    private final String autoBorderAverageColor;

    private Map<BoxSize, byte[]> bgContentByBoxSize;

    private Map<BoxSize, Font> fontByBoxSize;

    public enum BoxSize {
        XL, XXL
    }

    @Autowired
    public AssetController(AssetManager assetManager, ImageUtils imageUtils, @Value("${AssetController.autoBorderAverageColor:0000FF}") String autoBorderAverageColor,
            ServletContext servletContext) {
        this.assetManager = assetManager;
        this.imageUtils = imageUtils;
        this.autoBorderAverageColor = autoBorderAverageColor;

        fontByBoxSize = new EnumMap<>(BoxSize.class);
        fontByBoxSize.put(BoxSize.XL, new Font("Tahoma", Font.PLAIN, FONT_SIZE_12));
        fontByBoxSize.put(BoxSize.XXL, new Font("Tahoma", Font.PLAIN, FONT_SIZE_12));
        initBoxBGImages(servletContext);
    }

    @RequestMapping("/as/{assetExternalID}")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String assetExternalID,
            @RequestParam(value = "b", required = false) String borderColorRgbHex, @RequestParam(value = "a", required = false) Float alpha,
            @RequestParam(value = "w", required = false, defaultValue = "1") int borderWidth, @RequestParam(value = "size", required = false) BoxSize bgBoxSize)
            throws java.io.IOException {

        Asset asset = assetManager.getAssetByExternalId(assetExternalID);

        if (asset == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Asset not found: " + assetExternalID);
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // The client should cache the asset "forever" -- i.e. when we have
        // a CDN in front of our asset service, it shouldn't need to reload
        // assets...well...ever.

        // From the CloudFront developer guide:
        //
        // "For responses to CloudFront, set the Cache-Control max-age directive.
        // The value indicates the time, in seconds, that CloudFront caches an object
        // Objects are cached for a minimum of 3600 seconds (1 hour). If the value is
        // less than 3600 seconds, it is rounded up to 1 hour. Note that although the
        // Expires header field can be set to control object caching, the best practice
        // for CloudFront is to set the Cache-Control field instead of the Expires field."

        response.setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);

        // This is a fallback...expire one year from today.
        response.setHeader("Expires", EXPIRES_FORMAT.format(DateUtils.addYears(new Date(), 1)));

        byte[] data = asset.getData();

        String contentType = null;
        if (bgBoxSize != null && "text/plain".equals(asset.getContentType().getMIMEType())) {
            data = imageUtils.overlayTextOnBGImage(new String(asset.getData(), "utf-8"), fontByBoxSize.get(bgBoxSize), new ByteArrayInputStream(bgContentByBoxSize.get(bgBoxSize)),
                    "png").toByteArray();
            contentType = "image/png";
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Probable iurl fetch: asset[" + assetExternalID + "] for size:" + bgBoxSize);
            }
        }

        // SC-153 - add a border if the caller specified a border color
        if (StringUtils.isNotBlank(borderColorRgbHex)) {
            BorderMode borderMode;
            String useBorderColor;
            if (AUTO.equals(borderColorRgbHex)) {
                borderMode = BorderMode.AVERAGE;
                useBorderColor = autoBorderAverageColor;
            } else {
                borderMode = BorderMode.SOLID;
                useBorderColor = borderColorRgbHex;
            }

            float useAlpha = alpha != null ? alpha : DEFAULT_ALPHA_BY_BORDER_MODE.get(borderMode);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Adding border to Asset id=" + asset.getId() + " using borderMode=" + borderMode + ", color=" + useBorderColor + ", alpha=" + useAlpha + ", width="
                        + borderWidth);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                imageUtils.addBorderToImage(new ByteArrayInputStream(data), baos, asset.getContentType().isAnimated(), borderMode, useBorderColor, useAlpha, borderWidth);
                data = baos.toByteArray();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to add border to Asset id=" + asset.getId(), e);
            }
        }

        response.setContentType(contentType == null ? asset.getContentType().getMIMEType() : contentType);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Asset served: " + assetExternalID + " Mime: " + asset.getContentType().getMIMEType() + " Length: " + data.length);
        }
    }

    private void initBoxBGImages(ServletContext servletContext) {
        bgContentByBoxSize = new EnumMap<>(BoxSize.class);
        try {
            bgContentByBoxSize.put(BoxSize.XL, IOUtils.toByteArray(servletContext.getResourceAsStream("/300x50.png")));
            bgContentByBoxSize.put(BoxSize.XXL, IOUtils.toByteArray(servletContext.getResourceAsStream("/320x50.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
