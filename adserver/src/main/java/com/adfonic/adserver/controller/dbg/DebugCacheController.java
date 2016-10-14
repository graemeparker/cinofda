package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.Stoppage;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.controller.dbg.dto.DbgAdCacheDto;
import com.adfonic.adserver.controller.dbg.dto.DbgAdSpaceDto;
import com.adfonic.adserver.controller.dbg.dto.DbgCacheDto;
import com.adfonic.adserver.controller.dbg.dto.DbgCampaignDto;
import com.adfonic.adserver.controller.dbg.dto.DbgCreativeDto;
import com.adfonic.adserver.controller.dbg.dto.DbgDomainCacheDto;
import com.adfonic.adserver.controller.dbg.dto.DbgPublisherDto;
import com.adfonic.adserver.controller.dbg.dto.DbgStoppageDto;
import com.adfonic.adserver.impl.LocalBudgetManagerCassandra;
import com.adfonic.adserver.impl.LocalBudgetManagerCassandra.AdserverBudget;
import com.adfonic.adserver.rtb.util.RtbStats;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.ConfUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping("/adcache")
public class DebugCacheController {

    @Autowired
    private DomainCacheManager domainCacheManager;

    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Value("${AdserverDomainCache.label}")
    private String adCacheLabel;

    @Value("${DomainCache.label}")
    private String domainCacheLabel;

    @Value(ConfUtils.CACHE_DIR_CONFIG)
    private String cacheDir;

    @Autowired
    private LocalBudgetManagerCassandra budgetManager;

    @Autowired
    private StoppageManager stoppageManager;

    // Cannot use Spring MVC integrated json mapper as it is configured specifically for RTB controllers
    // So responses are marshalled manually here...  
    private final ObjectMapper debugJsonMapper = DebugBidController.debugJsonMapper;

    @RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String cache(HttpServletRequest httpRequest) throws IOException {
        DbgCacheDto dbgDto = new DbgCacheDto();
        dbgDto.setCacheDir(cacheDir);

        if (httpRequest.getParameter("nodomaincache") == null) {
            doDomainCache(httpRequest, dbgDto);
        }

        if (httpRequest.getParameter("noadcache") == null) {
            doAdCache(httpRequest, dbgDto);
        }

        return debugJsonMapper.writeValueAsString(dbgDto);
    }

    /**
     * @param adSpaceSpec - Numeric adspaceId or externalId
     */
    @RequestMapping(value = "/adspace/{adSpaceSpec}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String adSpace(@PathVariable("adSpaceSpec") String adSpaceSpec, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        AdSpaceDto adSpace = DbgUiUtil.findAdSpace(adSpaceSpec, adCache);

        if (adSpace == null) {
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "AdSpace not found in cache: " + adSpaceSpec);
            return null;//"AdSpace: " + adSpaceId + " not found";
        } else {
            DbgAdSpaceDto dbgDto = new DbgAdSpaceDto(adSpace);
            AdspaceWeightedCreative[] eligibleCreatives = adCache.getEligibleCreatives(adSpace.getId());
            if (httpRequest.getParameter("noeligible") == null) {
                dbgDto.setCreatives(eligibleCreatives);
            }
            return debugJsonMapper.writeValueAsString(dbgDto);
        }
    }

    /**
     * @param publisherSelector - Numeric publisherId or externalId
     */
    @RequestMapping(value = "/publisher/{publisherSelector}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String publisher(@PathVariable("publisherSelector") String publisherSelector, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        PublisherDto publisher = DbgUiUtil.findPublisher(publisherSelector, adCache);

        if (publisher == null) {
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Publisher not found in cache: " + publisherSelector);
            return null;
        } else {
            DomainCache domainCache = domainCacheManager.getCache();
            Map<Long, IntegrationTypeDto> integrations = new HashMap<Long, IntegrationTypeDto>();
            Map<Long, Long> map = publisher.getDefaultIntegrationTypeIdsByPublicationTypeId();
            for (Map.Entry<Long, Long> entry : map.entrySet()) {
                Long publicationTypeId = entry.getKey();
                Long integrationTypeId = entry.getValue();
                IntegrationTypeDto integrationType = domainCache.getIntegrationTypeById(integrationTypeId);
                integrations.put(publicationTypeId, integrationType);
            }
            DbgPublisherDto dbgPublisherDto = new DbgPublisherDto();
            dbgPublisherDto.setPublisher(publisher);
            dbgPublisherDto.setIntegrations(integrations);
            return debugJsonMapper.writeValueAsString(dbgPublisherDto);
        }
    }

    @RequestMapping(value = "/creative/{creativeSpec}/adspaces", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String eligibleAdSpaces(@PathVariable("creativeSpec") String creativeSpec, //
            @RequestParam(value = "rtb", defaultValue = "true") Boolean rtb,//
            @RequestParam(value = "publisher", required = false) String publisherSpec,//
            @RequestParam(value = "medium", required = false) Medium medium,//
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        AdserverDomainCache adCache = adserverCacheManager.getCache();
        CreativeDto creative = DbgUiUtil.findCreative(creativeSpec, adCache);
        if (creative == null) {
            httpResponse.setHeader("X-Debug", "Creative not found: " + creativeSpec);
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Creative not found in cache: " + creativeSpec);
            return null;
        }
        PublisherDto publisher = DbgUiUtil.findPublisher(publisherSpec, adCache);
        if (publisher == null) {
            httpResponse.setHeader("X-Debug", "Publisher not found: " + publisherSpec);
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Publisher not found in cache: " + publisherSpec);
            return null;
        }
        Set<Long> eligibleAdSpaces = eligibleAdSpaces(creative.getId(), rtb, publisher.getId(), medium);

        return debugJsonMapper.writeValueAsString(eligibleAdSpaces);
    }

    private Set<Long> eligibleAdSpaces(long creativeId, Boolean rtbOnly, Long publisherId, Medium medium) {
        DomainCache domainCache = domainCacheManager.getCache();
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        Map<Long, AdspaceWeightedCreative[]> allEligibleCreatives = adCache.getAllEligibleCreatives();

        Set<Long> adSpaceIds = new HashSet<Long>();
        for (Entry<Long, AdspaceWeightedCreative[]> entry : allEligibleCreatives.entrySet()) {
            Long adSpaceId = entry.getKey();
            AdspaceWeightedCreative[] weightedCreatives = entry.getValue();
            for (AdspaceWeightedCreative weightedCreative : weightedCreatives) {
                Long[] wCreativeIds = weightedCreative.getCreativeIds();
                for (Long wCreativeId : wCreativeIds) {
                    if (wCreativeId.longValue() == creativeId) {
                        AdSpaceDto adSpace = adCache.getAdSpaceById(adSpaceId);
                        PublicationDto publication = adSpace.getPublication();
                        PublisherDto publisher = publication.getPublisher();

                        if (rtbOnly != null && (rtbOnly && !publisher.isRtbEnabled() || !rtbOnly && publisher.isRtbEnabled())) {
                            continue;
                        }

                        if (publisherId != null && publisher.getId() != publisherId.longValue()) {
                            continue;
                        }

                        if (medium != null && medium != domainCache.getPublicationTypeById(publication.getPublicationTypeId()).getMedium()) {
                            continue;
                        }
                        adSpaceIds.add(adSpaceId);
                    }
                }
            }
        }
        return adSpaceIds;
    }

    @RequestMapping(value = "/creative/{creativeSpec}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String creative(@PathVariable("creativeSpec") String creativeSpec, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        CreativeDto creative = DbgUiUtil.findCreative(creativeSpec, adCache);

        if (creative == null) {
            httpResponse.setHeader("X-Debug", "Creative not found: " + creativeSpec);
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Creative not found in cache: " + creativeSpec);
            return null;
        } else {
            DbgCreativeDto dbgDto = new DbgCreativeDto(creative);
            DomainCache domainCache = domainCacheManager.getCache();
            Long formatId = creative.getFormatId();
            dbgDto.setFormat(domainCache.getFormatById(formatId));
            Long extendedCreativeTypeId = creative.getExtendedCreativeTypeId();
            if (extendedCreativeTypeId != null) {
                dbgDto.setExtendedType(domainCache.getExtendedCreativeTypeById(extendedCreativeTypeId));
            }
            if (httpRequest.getParameter("noeligible") == null) {
                Set<Long> eligibleAdSpaces = eligibleAdSpaces(creative.getId(), Boolean.TRUE, null, null);
                dbgDto.setEligibleAdSpaceIds(eligibleAdSpaces);
            }
            if (httpRequest.getParameter("nostats") == null) {
                dbgDto.setRtbStats(RtbStats.i().get(creative.getId()));
            }
            return debugJsonMapper.writeValueAsString(dbgDto);
        }
    }

    @RequestMapping(value = "/campaign/{campaignSpec}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String campaign(@PathVariable("campaignSpec") String campaignSpec, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        Long id = DbgUiUtil.tryToLong(campaignSpec);

        DbgCampaignDto dbgDto = new DbgCampaignDto();

        // There is no campaign lookup by id, we have to go through all creatives... 
        CreativeDto[] creativesCached = adCache.getAllCreatives();
        for (CreativeDto creativeDto : creativesCached) {
            CampaignDto campaign = creativeDto.getCampaign();
            if (campaign.getId().equals(id) || campaign.getExternalID().equals(campaignSpec)) {
                dbgDto.setCampaign(campaign);
                dbgDto.getCreatives().add(creativeDto);
            }
        }
        CampaignDto campaign = dbgDto.getCampaign();
        if (campaign == null) {
            httpResponse.setHeader("X-Debug", "Campaign not found: " + campaignSpec);
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Campaign not found in cache: " + campaignSpec);
            return null;
        }
        if (campaign.isBudgetManagerEnabled()) {
            try {
                // too fresh so rather trychatch around...
                AdserverBudget budget = budgetManager.readCurrent(campaign.getId());
                dbgDto.setBudget(budget);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        Stoppage campaingStoppage = stoppageManager.getCampaignStoppages().get(campaign.getId());
        if (campaingStoppage != null) {
            dbgDto.setCampaignStoppage(buildStoppage(campaingStoppage));
        }
        Stoppage advertiserStoppage = stoppageManager.getAdvertiserStoppages().get(campaign.getAdvertiser().getId());
        if (advertiserStoppage != null) {
            dbgDto.setAdvertiserStoppage(buildStoppage(advertiserStoppage));
        }
        return debugJsonMapper.writeValueAsString(dbgDto);
    }

    private void doDomainCache(HttpServletRequest httpRequest, DbgCacheDto dbgResponse) throws IOException {
        DbgDomainCacheDto doCacheDto = new DbgDomainCacheDto();
        dbgResponse.setDomainCache(doCacheDto);
        doCacheDto.setMetaData(DbgBuilder.getCacheMetaData(domainCacheManager));

        //DomainCache cache = domainCacheManager.getCache();
        //doCacheDto.setFormats(cache.getAllFormats());
        //doCacheDto.setIntegrationTypes(cache.getAllIntegrationTypes());
    }

    private void doAdCache(HttpServletRequest httpRequest, DbgCacheDto dbgResponse) throws IOException {
        DbgAdCacheDto adCacheDto = new DbgAdCacheDto();
        dbgResponse.setAdCache(adCacheDto);
        adCacheDto.setMetaData(DbgBuilder.getCacheMetaData(adserverCacheManager));

        AdserverDomainCache adCache = adserverCacheManager.getCache();

        AdSpaceDto[] allAdSpaces = adCache.getAllAdSpaces();
        adCacheDto.setAdSpacesTotal(allAdSpaces.length);

        adCacheDto.setPublishersTotal(DbgBuilder.getAllPublishers(adCache).size());//all publishers - not only with adspaces

        if (httpRequest.getParameter("nopublishers") == null) {
            Set<Long> publisherIds = new HashSet<Long>();
            Set<PublisherDto> servingPublishers = DbgBuilder.getAllPublishers(adCache);
            for (PublisherDto publisherDto : servingPublishers) {
                publisherIds.add(publisherDto.getId());
            }
            adCacheDto.setPublisherIds(publisherIds);
        }

        Set<Long> creativeIds = new HashSet<Long>();
        Set<Long> campaignIds = new HashSet<Long>();
        Set<Long> advertiserIds = new HashSet<Long>();
        //Set<AdvertiserDto> advertisers = new HashSet<AdvertiserDto>();
        CreativeDto[] allCreatives = adCache.getAllCreatives();
        adCacheDto.setCreativesTotal(allCreatives.length);

        for (CreativeDto creativeDto : allCreatives) {
            creativeIds.add(creativeDto.getId());
            Long campaignId = creativeDto.getCampaign().getId();
            campaignIds.add(campaignId);
            advertiserIds.add(creativeDto.getCampaign().getAdvertiser().getId());
        }
        if (httpRequest.getParameter("nocreatives") == null) {
            adCacheDto.setCreativeIds(creativeIds);
        }
        if (httpRequest.getParameter("nocampaigns") == null) {
            adCacheDto.setCampaignIds(campaignIds);
        }
        if (httpRequest.getParameter("noadvertisers") == null) {
            adCacheDto.setAdvertiserIds(advertiserIds);
        }
        adCacheDto.setAdvertisersTotal(advertiserIds.size());

        Map<Long, AdspaceWeightedCreative[]> allEligibleCreatives = adCache.getAllEligibleCreatives();
        adCacheDto.setEligibilitiesTotal(allEligibleCreatives.size());
    }

    private DbgStoppageDto buildStoppage(Stoppage stoppage) {
        Date timestampAt = new Date(stoppage.getTimestamp());
        Long reactivateMs = stoppage.getReactivateDate();
        Date reactivateAt = reactivateMs != null ? new Date(reactivateMs) : null;
        return new DbgStoppageDto(timestampAt, reactivateAt);
    }

    /*
    

    List<PublisherDto> publishers = new ArrayList<PublisherDto>();
    for (Entry<String, Long> entry : publisherByExternalId.entrySet()) {
        long publisherId = entry.getValue();
        PublisherDto publisher;
        Map<String, AdSpaceDto> adSpaceMap = adCache.getPublisherRtbAdSpacesMap(publisherId);
        if (adSpaceMap != null && !adSpaceMap.isEmpty()) {
            AdSpaceDto adSpace = adSpaceMap.entrySet().iterator().next().getValue();
            publisher = adSpace.getPublication().getPublisher();
            //int publisherAdSpaces = adSpaceMap.size();
            publishers.add(publisher);
        }
    }
    */

    public static class HmLink {

        private final String rel;
        private final String href;

        public HmLink(String rel, String href) {
            this.rel = rel;
            this.href = href;
        }

        public String getRel() {
            return rel;
        }

        public String getHref() {
            return href;
        }

    }
}
