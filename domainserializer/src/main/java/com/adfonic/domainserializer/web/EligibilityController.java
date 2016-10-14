package com.adfonic.domainserializer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCache.ShardMode;
import com.adfonic.domain.cache.listener.DSRejectionListener;
import com.adfonic.domainserializer.DsShard;
import com.adfonic.domainserializer.loader.AdCacheBuildParams;
import com.adfonic.domainserializer.loader.AdserverDomainCacheLoader;
import com.adfonic.util.ConfUtils;

@Controller
@RequestMapping(EligibilityController.URL_CONTEXT)
public class EligibilityController {

    public static final String HTML_OPEN = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head><body>";
    public static final String HTML_CLOSE = "</body></html>";

    public static final String URL_CONTEXT = "/eligibility";

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    private AdserverDomainCacheLoader cacheLoader;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void formView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(HTML_OPEN);
        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("Creative: <input name='creativeSpec' size='40' />");
        writer.println("Campaing: <input name='campaignSpec' size='40' />");
        writer.println("<br/>");
        writer.println("AdSpace: <input name='adSpaceSpec' size='40' />");
        writer.println("Publication: <input name='publicationSpec' size='40' />");
        writer.println("<br/>");
        writer.println("<input type='submit' value='Lookup'/>");
        writer.println(HTML_CLOSE);
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void formSubmit(HttpServletResponse httpResponse,//
            @RequestParam(required = false) String creativeSpec, @RequestParam(required = false) String campaignSpec,//
            @RequestParam(required = false) String adSpaceSpec, @RequestParam(required = false) String publicationSpec//
    ) throws IOException {

        if (StringUtils.isBlank(creativeSpec) && StringUtils.isBlank(campaignSpec)) {
            throw new IllegalArgumentException("Creative or Campaing must be specified");
        }

        if (StringUtils.isBlank(adSpaceSpec) && StringUtils.isBlank(publicationSpec)) {
            throw new IllegalArgumentException("AdSpace or Publication must be specified");
        }

        Long creativeId = findIfExist("CREATIVE", creativeSpec, "ACTIVE");
        Long campaignId = findIfExist("CAMPAIGN", campaignSpec, "ACTIVE", "PAUSED"); // See CreativeLoader query
        Long adSpaceId = findIfExist("AD_SPACE", adSpaceSpec, "VERIFIED", "UNVERIFIED"); // See AdSpaceLoader query 
        Long publicationId = findIfExist("PUBLICATION", publicationSpec, "ACTIVE");

        DsShard shard = new DsShard("eligibility-check", ShardMode.all, Collections.EMPTY_SET, true);
        AdCacheBuildParams params = new AdCacheBuildParams(shard);
        params.setDebugCreativeId(creativeId);
        params.setDebugCampaignId(campaignId);
        params.setDebugAdSpaceId(adSpaceId);
        params.setDebugPublicationId(publicationId);

        DebugEligibilityListener listener = new DebugEligibilityListener();
        params.setEligibilityListener(listener);

        AdserverDomainCache adserverCache;
        try {
            adserverCache = cacheLoader.loadAdserverDomainCache(params);
        } catch (Exception x) {
            throw new IllegalStateException("Eligibility check failed", x);
        }
        httpResponse.setContentType("text/html");
        PrintWriter writer = httpResponse.getWriter();

        List<EligibilityResult> results = listener.results;
        if (results.isEmpty()) {
            writer.print("No eligibility results.");
            writer.println("<br/>");
            if (adserverCache.getAllCreatives().length == 0) {
                writer.print("No Creative selected. Maybe no funds on account or stoppage");
                writer.println("<br/>");
            }
            if (adserverCache.getAllAdSpaces().length == 0) {
                writer.print("No Adspace selected. Maybe it is not in right state");
                writer.println("<br/>");
            }
            writer.println("<br/>");
        }

        for (EligibilityResult result : listener.results) {
            String rejectionReason = result.getRejectionReason();
            if (result.getAdSpace() != null) {
                if (result.getCreative() != null) {
                    writer.print("Creative: " + result.getCreative().getId() + ", AdSpace: " + result.getAdSpace().getId()
                            + (rejectionReason != null ? " are NOT eligible: " + rejectionReason : " are eligible"));
                } else {
                    writer.print("Creative: ALL, AdSpace: " + result.getAdSpace().getId()
                            + (rejectionReason != null ? " are NOT eligible because: " + rejectionReason : " is eligible? whith what?"));
                }

            } else {
                if (result.getCreative() != null) {
                    writer.print("Creative: " + result.getCreative().getId()
                            + (rejectionReason != null ? " is NOT eligible because: " + rejectionReason : " is eligible? whith what?"));
                } else {
                    writer.print(result);
                }
            }

            writer.print("<br/>");
        }

    }

    private Long tryLong(String string) {
        try {
            return Long.valueOf(string);
        } catch (NumberFormatException nfx) {
            return null;
        }
    }

    private Long findIfExist(String table, String specifier, String... allowedStatuses) {
        if (StringUtils.isNotBlank(specifier)) {
            Long id = tryLong(specifier);
            JdbcTemplate template = new JdbcTemplate(toolsDataSource);
            IdAndStatusMapper mapper = new IdAndStatusMapper();
            Object[] idAndStatus = null;
            try {
                if (id != null) {
                    // check if exists
                    idAndStatus = template.queryForObject("SELECT ID, STATUS FROM " + table + " WHERE ID=?", mapper, id);
                } else {
                    // find by external id
                    idAndStatus = template.queryForObject("SELECT ID, STATUS FROM " + table + " WHERE EXTERNAL_ID=?", mapper, specifier);
                }
            } catch (EmptyResultDataAccessException erdax) {
                // ok, not found
            }
            if (idAndStatus == null) {
                throw new IllegalArgumentException(table + " not found: '" + specifier + "'");
            } else {
                id = (Long) idAndStatus[0];
                String status = (String) idAndStatus[1];
                if (allowedStatuses != null) {
                    List<String> allowedList = Arrays.asList(allowedStatuses);
                    if (!allowedList.contains(status)) {
                        throw new IllegalArgumentException(table + " '" + specifier + "' state: " + status + " not in: " + allowedList);
                    }
                }
                return id;
            }
        } else {
            return null;
        }
    }

    private class IdAndStatusMapper implements RowMapper<Object[]> {

        @Override
        public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong(1);
            String status = rs.getString(2);
            return new Object[] { id, status };
        }

    }

    public static class EligibilityResult {

        private final CreativeDto creative;

        private final AdSpaceDto adSpace;

        private final String rejectionReason;

        /*
                public EligibilityResult(Long idCreative, Long idAdSpace) {
                    this(idCreative, idAdSpace, null);
                }

                public EligibilityResult(Long idCreative, Long idAdSpace, String rejectionReason) {
                    this.idCreative = idCreative;
                    this.idAdSpace = idAdSpace;
                    this.rejectionReason = rejectionReason;
                }
        */
        public EligibilityResult(CreativeDto creative, AdSpaceDto adSpace) {
            this(creative, adSpace, null);
        }

        public EligibilityResult(CreativeDto creative, AdSpaceDto adSpace, String rejectionReson) {
            this.creative = creative;
            this.adSpace = adSpace;
            rejectionReason = rejectionReson;
        }

        public CreativeDto getCreative() {
            return creative;
        }

        public AdSpaceDto getAdSpace() {
            return adSpace;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        @Override
        public String toString() {
            return "EligibilityResult {creative=" + (creative != null ? creative.getId() : null) + ", adSpace=" + (adSpace != null ? adSpace.getId() : null)
                    + (rejectionReason == null ? ", eligible" : ", rejected=" + rejectionReason) + "}";
        }

    }

    public static enum Eligible {
        YES, NO, UNKNOWN;
    }

    public static class DebugEligibilityListener implements DSRejectionListener {

        // For Campaigns with more Creatives, we need List...
        private List<EligibilityResult> results = new ArrayList<EligibilityResult>();

        /*
        private final Long idCreative;

        private final Long idCampaign;

        private final Long idAdSpace;

        private final Long idPublication;

        public DebugEligibilityListener(Long idCreative, Long idAdSpace) {
            Objects.requireNonNull(idCreative);
            this.idCreative = idCreative;
            this.idCampaign = null;
            Objects.requireNonNull(idAdSpace);
            this.idAdSpace = idAdSpace;
            this.idPublication = null;
        }

        public DebugEligibilityListener(Long idCreative, Long idCampaign, Long idAdSpace, Long idPublication) {
            if (idCreative == null && idCampaign == null) {
                throw new IllegalArgumentException("Creative or Campaing Id must be specified");
            }
            this.idCreative = idCreative;
            this.idCampaign = idCampaign;
            if (idAdSpace == null && idPublication == null) {
                throw new IllegalArgumentException("AdSpace or Publication Id must be specified");
            }
            this.idAdSpace = idAdSpace;
            this.idPublication = idPublication;
        }
        */

        @Override
        public void reject(CreativeDto creative, String rejectionReson) {
            this.results.add(new EligibilityResult(creative, null, rejectionReson));
        }

        @Override
        public void ineligible(AdSpaceDto adSpace, CreativeDto creative, String rejectionReson) {
            if (isTraced(adSpace, creative)) {
                this.results.add(new EligibilityResult(creative, adSpace, rejectionReson));
            }
        }

        @Override
        public void ineligible(AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, String rejectionReson) {
            if (isTraced(adSpace, creative)) {
                this.results.add(new EligibilityResult(creative, adSpace, rejectionReson));
            }
        }

        @Override
        public void eligible(AdSpaceDto adSpace, CreativeDto creative, int effectivePriority) {
            if (isTraced(adSpace, creative)) {
                this.results.add(new EligibilityResult(creative, adSpace));
            }
        }

        public List<EligibilityResult> getResults() {
            return results;
        }

        private boolean isTraced(AdSpaceDto adSpace, CreativeDto creative) {
            return true;
            /*
            if ((idAdSpace != null && idAdSpace.equals(adSpace.getId())) || (idPublication != null && idPublication.equals(adSpace.getPublication().getId()))) {
                if (creative == null) {
                    // null creative means all creatives with some adspace format
                    return false;
                } else if (creative.getId().equals(idCreative) || (idCampaign != null && creative.getCampaign().getId().equals(idCampaign))) {
                    return true;
                }
            }
            return false;
            */
        }

    }
}
