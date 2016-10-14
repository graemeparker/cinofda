package com.adfonic.adserver.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.controller.dbg.DbgUiUtil;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.rtb.OpenRtbV1Controller;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

/**
 * 
 * @author mvanek
 *
 */
@Controller
public class CreativeController {

    public enum Render {
        markup, source, bid;
    }

    @Autowired
    private DomainCacheManager domainCacheManager;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Autowired
    private AdMarkupRenderer adMarkupRenderer;

    @Autowired
    private TargetingContextFactory targetingContextFactory;

    /*
     * 4695 - text ad
     * 4696/4658 - simple banner
     * 4697 - 300x250
     * 4699 - VAST
     * 4698/4587 - 3rd party tag
     * 
     * This is merely for testing...
     */
    @RequestMapping("/creative/{crid}")
    public void render(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String crid, //
            @RequestParam(required = false, name = "asid") String asid // Optional - AdSpace id
    ) throws Exception {

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");

        AdserverDomainCache adCache = adserverCacheManager.getCache();
        Long id = DbgUiUtil.tryToLong(crid);
        CreativeDto creative;
        if (id != null) {
            creative = adCache.getCreativeById(id);
        } else {
            creative = adCache.getCreativeByExternalID(crid);
        }
        if (creative == null) {
            httpResponse.sendError(404, "Creative " + crid + " not found");
            return;
        }

        Render render = getParameter(httpRequest, Render.markup);
        RtbExchange exchange = getParameter(httpRequest, "exchange", RtbExchange.Mopub);
        List<Feature> features = getParameterList(httpRequest, "feature", Feature.BEACON, true);
        List<ContentForm> contentForms = null;
        if (httpRequest.getParameter("contentForm") != null) {
            contentForms = getParameterList(httpRequest, "contentForm", ContentForm.MRAID_1_0, false);
        }

        TargetingContext context = targetingContextFactory.createTargetingContext(httpRequest, false); // Do not use http request headers

        context.setSslRequired(creative.isSslCompliant());
        //context.setFlagTrue(TargetingContext.CREATIVE_AUDIT);

        Impression impression = new Impression();
        impression.setExternalID(Constant.XAUDIT_IMPRESSION_EXTERNAL_ID);
        impression.setDeviceIdentifiers(Collections.emptyMap());

        DomainCache domainCache = domainCacheManager.getCache();

        FormatDto format = domainCache.getFormatById(creative.getFormatId());

        DisplayTypeDto displayType = null; // TODO Add parameter for selecting DisplayType
        for (DisplayTypeDto item : format.getDisplayTypes()) {
            if (creative.hasAssets(item.getId())) { // Find DisplayType that creative has Asset for
                displayType = item;
                break;
            }
        }
        if (displayType == null) {
            throw new IllegalStateException("Cannot figure out Creative Asset for Format " + format);
        }

        IntegrationTypeDto integrationType = new IntegrationTypeDto();
        integrationType.setId(0l);
        integrationType.setSystemName("Artificial");
        context.setAttribute(TargetingContext.INTEGRATION_TYPE, integrationType);

        for (Feature feature : features) {
            integrationType.getSupportedFeatures().add(feature);
        }

        if (contentForms != null) {
            context.setAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET, new HashSet<ContentForm>(contentForms));
        }

        Map<ContentForm, String> extenedTemplates = creative.getExtendedCreativeTemplates();
        // For extended creative we need IntegrationType
        if (extenedTemplates != null && !extenedTemplates.isEmpty()) {
            ExtendedCreativeTypeDto xType = domainCache.getExtendedCreativeTypeById(creative.getExtendedCreativeTypeId());

            for (ContentForm contentForm : extenedTemplates.keySet()) {
                integrationType.addSupportedContentForm(xType.getMediaType(), contentForm);
            }

            // Not specified via parameter -> Allow all ContentForms creative has Template for
            if (null == context.getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET)) {
                context.setAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET, extenedTemplates.keySet());
            } else {
                if (!extenedTemplates.keySet().containsAll(contentForms)) {
                    throw new IllegalArgumentException("Required ContentForm " + contentForms + " mismatch with Creative " + extenedTemplates.keySet());
                }
            }

        }

        AdSpaceDto adSpace;
        if (StringUtils.isNotBlank(asid)) {
            adSpace = selectAdSpace(asid, creative, exchange, adCache);
        } else {
            Map<String, AdSpaceDto> adSpacesMap = adCache.getPublisherRtbAdSpacesMap(exchange.getPublisherId());
            if (adSpacesMap == null) {
                throw new IllegalArgumentException("No " + exchange + " AdSpace in cache");
            }
            adSpace = adSpacesMap.values().iterator().next();
            adSpace = anonymizeAdSpaceDto(adSpace);
        }

        context.setAdSpace(adSpace);

        RtbConfigDto rtbConfig = adSpace.getPublication().getPublisher().getRtbConfig();

        String[] rendered = adMarkupRenderer.createMarkup(context, impression, creative, format, adSpace, displayType, httpRequest, null, rtbConfig);
        if (render == Render.markup) {
            writeAdMarkup(httpRequest, httpResponse, rendered);
        } else if (render == Render.source) {
            writeAdSource(httpRequest, httpResponse, rendered);
        } else if (render == Render.bid) {
            throw new IllegalStateException("Not implemented yet");
        }

    }

    private void writeAdSource(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String[] rendered) throws IOException {
        String escapedMarkup = StringEscapeUtils.escapeHtml(rendered[0]);
        httpResponse.setContentType("text/html; charset=utf-8");
        PrintWriter writer = httpResponse.getWriter();
        writer.println("<pre>");
        writer.println(escapedMarkup);
        writer.println("</pre>");
    }

    private void writeAdMarkup(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String[] rendered) throws IOException {
        String contentType = rendered[1] + "; charset=utf-8";
        httpResponse.setContentType(contentType);

        if (contentType.startsWith(Constant.APPL_XML)) { // CORS headers for VAST HTML5 player
            OpenRtbV1Controller.addCorsHeaders(httpRequest, httpResponse);
        }

        httpResponse.getWriter().write(rendered[0]);
    }

    private AdSpaceDto selectAdSpace(String asid, CreativeDto creative, RtbExchange exchange, AdserverDomainCache adCache) {
        if (StringUtils.isNotBlank(asid)) {
            Long id = DbgUiUtil.tryToLong(asid);
            AdSpaceDto adspace;
            if (id != null) {
                adspace = adCache.getAdSpaceById(id);
            } else {
                adspace = adCache.getAdSpaceByExternalID(asid);
            }
            if (adspace == null) {
                throw new IllegalArgumentException("AdSpace " + asid + " not found");
            }
            return adspace;
        } else {
            // Do we really want to find ?
            Map<String, AdSpaceDto> adSpacesMap = adCache.getPublisherRtbAdSpacesMap(exchange.getPublisherId());
            for (AdSpaceDto item : adSpacesMap.values()) {
                if (item.getFormatIds().contains(creative.getFormatId())) {
                    return item;
                }
            }
            throw new IllegalStateException("No adspace found for " + creative);
        }
    }

    private AdSpaceDto anonymizeAdSpaceDto(AdSpaceDto adSpace) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(adSpace);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            adSpace = (AdSpaceDto) ois.readObject();
        }
        adSpace.setExternalID(Constant.XAUDIT_ADSPACE_EXTERNAL_ID);
        adSpace.getPublication().setExternalID(Constant.XAUDIT_PUBLICATION_EXTERNAL_ID);
        return adSpace;
    }

    private <T extends Enum<T>> T getParameter(HttpServletRequest httpRequest, T defaultValue) {
        return getParameter(httpRequest, defaultValue.getClass().getSimpleName().toLowerCase(), defaultValue);
    }

    private <T extends Enum<T>> T getParameter(HttpServletRequest httpRequest, String paramName, T defaultValue) {
        Class<T> clazz = (Class<T>) defaultValue.getClass();
        String value = httpRequest.getParameter(paramName);
        if (StringUtils.isNotBlank(value)) {
            return Enum.valueOf(clazz, value);
        } else {
            return defaultValue;
        }
    }

    private <T extends Enum<T>> List<T> getParameterList(HttpServletRequest httpRequest, String name, T defaultValue, boolean allwaysAddDefault) {
        String[] values = httpRequest.getParameterValues(name);
        if (values == null) {
            return Arrays.asList(defaultValue);
        } else if (values.length == 1) {
            values = values[0].split(",");
        }
        Class<T> clazz = (Class<T>) defaultValue.getClass();
        List<T> retval = new ArrayList<T>(values.length + 1);
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                retval.add(Enum.valueOf(clazz, value));
            }
        }
        if (allwaysAddDefault && !retval.contains(defaultValue)) {
            retval.add(defaultValue);
        }
        return retval;
    }
}
