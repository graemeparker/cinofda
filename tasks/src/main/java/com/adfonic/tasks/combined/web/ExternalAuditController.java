package com.adfonic.tasks.combined.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;
import com.adfonic.tasks.xaudit.adx.AdXCreativeApiManager;
import com.adfonic.tasks.xaudit.appnxs.AppNexusApiClient;
import com.adfonic.tasks.xaudit.appnxs.AppNexusAuditService;
import com.adfonic.tasks.xaudit.appnxs.AppNexusCreativeSystem;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.util.ConfUtils;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(ExternalAuditController.URL_CONTEXT)
public class ExternalAuditController {

    public static final String HTML_OPEN = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head><body>";
    public static final String HTML_CLOSE = "</body></html>";

    public static final String URL_CONTEXT = "/xaudit";

    @Value("${adx.publisherid}")
    private long ADX_PUBLISHER_ID;
    public static long APPNEXUS_PUBLISHER_ID = 34354l;

    static enum XauditAction {
        Retry, Query, Render, Submit;
    }

    @Autowired
    PublisherManager publisherManager;

    @Autowired
    CreativeManager creativeManager;

    @Autowired
    private AdXAuditService adxService;

    @Autowired
    private AdXCreativeApiManager adxClient;

    @Autowired
    private AppNexusCreativeSystem apnxSystem;

    @Autowired
    private AppNexusApiClient apnxClient;

    private AppNexusAuditService apnxService;

    private final ObjectMapper jackson = new ObjectMapper();

    @Autowired
    @Qualifier(ConfUtils.TOOLS_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void formView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(HTML_OPEN);
        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("Creative: <input name='creativeSpec' size='40' />");
        writer.println("<input type='radio' name='publisherId' value='" + ADX_PUBLISHER_ID + "' />AdX");
        writer.println("<input type='radio' name='publisherId' value='" + APPNEXUS_PUBLISHER_ID + "' />AppNexus");
        writer.println("<br/>");
        writer.println("<input type='submit' name='action' value='" + XauditAction.Retry + "' title='Retry audit check as designed' />");
        writer.println("<input type='submit' name='action' value='" + XauditAction.Render + "' title='Print how would submission into Exchange's Audit API would look like' />");
        writer.println("<input type='submit' name='action' value='" + XauditAction.Query + "' title='Query Exchange's Audit API' />");
        writer.println("<input type='submit' name='action' value='" + XauditAction.Submit + "' title='Submit directly into Exchange's Audit API' />");
        writer.println(HTML_CLOSE);
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void formSubmit(HttpServletResponse httpResponse,//
            @RequestParam String creativeSpec, @RequestParam Long publisherId,//
            @RequestParam String action) throws IOException {

        long creativeId = getCreativeId(creativeSpec);

        XauditAction xAction = XauditAction.valueOf(action);
        if (xAction == XauditAction.Retry) {
            //XXX deleteAuditRecord(publisherId, creativeId);
            if (publisherId == adxService.getPublisherId()) {
                adxService.onNewCreative(creativeId);
            } else {
                apnxService.onCreate(creativeId, publisherId);
            }
        } else {
            doActionWith(creativeId, publisherId, httpResponse, xAction);
        }

    }

    private void doActionWith(long creativeId, long publisherId, HttpServletResponse httpResponse, XauditAction xAction) throws IOException {
        Publisher publisher = publisherManager.getPublisherById(publisherId, AdXAuditService.PUBLISHER_FETCH_STRATEGY);
        if (publisher == null) {
            throw new IllegalStateException("Publisher not found: " + publisherId);
        }
        Creative byydCreative = creativeManager.getCreativeById(creativeId, AdXAuditService.CREATIVE_FETCH_STRATEGY);
        if (byydCreative == null) {
            throw new IllegalStateException("Creative not found: " + creativeId);
        }
        PublisherAuditedCreative auditRecord = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(publisher, byydCreative);

        if (publisher.getId() == ADX_PUBLISHER_ID) {
            com.google.api.services.adexchangebuyer.model.Creative adxCreative;
            if (xAction == XauditAction.Render) {
                adxCreative = adxService.buildAdxCreative(byydCreative, publisher);
            } else if (xAction == XauditAction.Query) {
                adxCreative = adxClient.getAdxCreative(byydCreative.getExternalID());
            } else if (xAction == XauditAction.Submit) {
                adxCreative = adxService.buildAdxCreative(byydCreative, publisher);
                adxClient.submitAdxCreative(byydCreative.getId(), adxCreative);
            } else {
                throw new IllegalArgumentException("Unsupported " + xAction);
            }
            jackson.writeValue(httpResponse.getWriter(), adxCreative);
        } else if (publisher.getId() == APPNEXUS_PUBLISHER_ID) {
            AppNexusCreativeRecord anxCreative = null;
            if (xAction == XauditAction.Render) {
                anxCreative = apnxSystem.buildAppNexusCreative(byydCreative, publisher);
            } else if (xAction == XauditAction.Query) {
                // For AppNexus we have to have their identifier for query or update...
                if (auditRecord != null) {
                    anxCreative = apnxSystem.getAppNexusCreative(auditRecord.getExternalReference());
                }
            } else if (xAction == XauditAction.Submit) {
                anxCreative = apnxSystem.buildAppNexusCreative(byydCreative, publisher);
                if (auditRecord != null) {
                    apnxClient.updateCreative(auditRecord.getExternalReference(), anxCreative);
                } else {
                    String externalReference = apnxClient.postCreative(anxCreative);
                    auditRecord = new PublisherAuditedCreative(publisher, byydCreative);
                    auditRecord.setExternalReference(externalReference);
                    publisherManager.create(auditRecord);
                }

            } else {
                throw new IllegalArgumentException("Unsupported action: " + xAction);
            }

            jackson.writeValue(httpResponse.getWriter(), anxCreative);
        } else {
            throw new IllegalArgumentException("Unsupported publisher: " + publisher);
        }
    }

    private void deleteAuditRecord(long publisherId, long creativeId) {
        jdbcTemplate.update("DELETE FROM PUBLISHER_AUDITED_CREATIVE WHERE PUBLISHER_ID=? AND CREATIVE_ID=?", publisherId, creativeId);
    }

    private long getCreativeId(String creativeSpec) {
        Long creativeId;
        try {
            try {
                creativeId = jdbcTemplate.queryForObject("SELECT ID FROM CREATIVE WHERE ID=?", Long.class, Long.parseLong(creativeSpec));
            } catch (NumberFormatException nfx) {
                creativeId = jdbcTemplate.queryForObject("SELECT ID FROM CREATIVE WHERE EXTERNAL_ID=?", Long.class, creativeSpec);
            }
        } catch (EmptyResultDataAccessException erdax) {
            throw new IllegalArgumentException("Creative not found: " + creativeSpec);
        }
        if (creativeId == null) {
            throw new IllegalArgumentException("Creative not found: " + creativeSpec);
        } else {
            return creativeId;
        }
    }

}
