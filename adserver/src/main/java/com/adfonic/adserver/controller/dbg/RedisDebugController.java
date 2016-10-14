package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto.AudienceType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.retargeting.redis.DeviceDataRedisReader;
import com.adfonic.retargeting.redis.GeoAudienceReader;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping(RedisDebugController.URL_CONTEXT)
public class RedisDebugController {

    public static final String URL_CONTEXT = "/adserver/redis";

    @Autowired
    private DeviceDataRedisReader deviceReader;

    @Autowired
    private GeoAudienceReader locationReader;

    //@Autowired
    //private LocalBudgetManagerRedis budgetRedis;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Autowired
    private DomainCacheManager domainCacheManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void allFormView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printDeviceIdForm(writer);
        printLocationForm(writer);
        //printBudgetForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @RequestMapping(value = "/budget", method = RequestMethod.POST)
    public void budgetFormPost(@RequestParam("campaign") String campaign, HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(URL_CONTEXT + "/budget/" + campaign);
    }

    @RequestMapping(value = "/budget", method = RequestMethod.GET)
    public void budgetFormView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printBudgetForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private void printBudgetForm(PrintWriter writer) {
        writer.println("<form method='POST' action='" + URL_CONTEXT + "/budget' accept-charset='UTF-8'>");
        writer.println("Campaign: <input name='campaign' size='40' />");
        writer.println("<input type='submit' value='Budget Redis'/>");
        writer.println("</form>");
    }

    /*
     * FIXME budget from Cassandra
    @ResponseBody
    @RequestMapping(value = "/budget/{campaign}", method = RequestMethod.GET, produces = "application/json")
    public String getBudgetData(@PathVariable("campaign") String campaignIdent, HttpServletResponse httpResponse) throws IOException {

        Long campaignId = DbgUiUtil.tryToLong(campaignIdent);
        if (campaignId == null) {
            CampaignDto campaign = DbgUiUtil.findCampaign(campaignIdent, adserverCacheManager.getCache());
            if (campaign != null) {
                campaignId = campaign.getId();
            } else {
                httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Campaign not in cache: " + campaignIdent);
                return null;
            }
        }
       
        String redisResponse = budgetRedis.retrieveCluster().get("c." + campaignId);
        if (redisResponse != null) {
            return redisResponse;
        } else {
            httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Campaign budget not redis: " + campaignId);
            return null;
        }
    }
    */

    @RequestMapping(value = "/location", method = RequestMethod.GET)
    public void locationFormView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printLocationForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private void printLocationForm(PrintWriter writer) {
        writer.println("<form method='POST' action='" + URL_CONTEXT + "/location' accept-charset='UTF-8'>");
        writer.println("Latitude: <input name='latitude' size='40' />");
        writer.println("Longitude: <input name='longitude' size='40' />");
        writer.println("<input type='submit' value='Geo Redis'/>");
        writer.println("</form>");
    }

    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public void locationFormPost(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude, HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(URL_CONTEXT + "/location/" + latitude + "/" + longitude);
    }

    /**
     * Simple mapping /location/{latitude}/{longitude} would make latitude be truncated to integer
     * because by default SpringMVC is using part after last dot as mime type (.xml, .json, ...) 
     */
    @ResponseBody
    @RequestMapping(value = "/location/{latitude:.+}/{longitude:.+}", method = RequestMethod.GET, produces = "application/json")
    public String getLocation(@PathVariable("latitude") float latitude, @PathVariable("longitude") float longitude) throws IOException {
        Set<Long> audienceIds = locationReader.getAudiences(latitude, longitude);
        Map<String, Object> jsonMap = getMatchingCampaigns(audienceIds, CampaignAudienceDto.AudienceType.LOCATION);
        return DebugBidController.debugJsonMapper.writeValueAsString(jsonMap);

    }

    private Map<String, Object> getMatchingCampaigns(Set<Long> audienceIds, CampaignAudienceDto.AudienceType type) {
        Set<Long> campaignIds = new HashSet<Long>();
        Set<Long> creativeIds = new HashSet<Long>();
        CreativeDto[] creatives = adserverCacheManager.getCache().getAllCreatives();
        for (CreativeDto creative : creatives) {
            CampaignDto campaign = creative.getCampaign();
            Set<CampaignAudienceDto> campaignAudiences;
            if (type == AudienceType.LOCATION) {
                campaignAudiences = creative.getCampaign().getLocationAudiences();
            } else {
                campaignAudiences = creative.getCampaign().getLocationAudiences();
            }
            for (CampaignAudienceDto campaignAudience : campaignAudiences) {
                if (audienceIds.contains(campaignAudience.getAudienceId())) {
                    campaignIds.add(campaign.getId());
                    creativeIds.add(creative.getId());
                }
            }
        }
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("audienceIds", audienceIds);
        jsonMap.put("campaignIds", campaignIds);
        jsonMap.put("creativeIds", creativeIds);
        return jsonMap;
    }

    @RequestMapping(value = "/deviceid", method = RequestMethod.GET)
    public void deviceFormView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printDeviceIdForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private void printDeviceIdForm(PrintWriter writer) {
        writer.println("<form method='POST' action='" + URL_CONTEXT + "/deviceid' accept-charset='UTF-8'>");
        DomainCache domainCache = domainCacheManager.getCache();
        Map<String, Long> typesBySystemName = domainCache.getDeviceIdentifierTypeIdsBySystemName();
        for (Map.Entry<String, Long> entry : typesBySystemName.entrySet()) {
            DeviceIdentifierTypeDto didTypeDto = domainCache.getDeviceIdentifierTypeById(entry.getValue());
            writer.println(didTypeDto.getSystemName() + ": <input type='radio' name='deviceIdType' value='" + didTypeDto.getId() + "' title='" + didTypeDto.getValidationPattern()
                    + "'/>");
        }
        writer.println("<br/>");
        writer.println("DeviceId: <input name='deviceId' size='40' />");
        writer.println("<input type='submit' value='Device Redis'/>");
        writer.println("</form>");
    }

    @RequestMapping(value = "/deviceid", method = RequestMethod.POST)
    public void deviceFormPost(@RequestParam(value = "deviceIdType", required = false) String deviceIdType, @RequestParam("deviceId") String deviceId,
            HttpServletResponse httpResponse) throws IOException {

        if (deviceIdType == null) {
            //try to guess from deviceId
            deviceIdType = guessDeviceIdType(deviceId)[0];
        }
        httpResponse.sendRedirect(URL_CONTEXT + "/deviceid/" + deviceIdType + "." + deviceId);
    }

    private String[] guessDeviceIdType(String deviceId) {
        int length = deviceId.length();
        if (length == 36) { // IDFA or ADID (32hex + 4hyphens) 8-4-4-4-12 (AEBE52E7-03EE-455A-B3C4-E57283966239)
            for (int i = 0; i < deviceId.length(); ++i) {
                if (Character.isUpperCase(deviceId.charAt(i))) {
                    return new String[] { DeviceIdentifierType.SYSTEM_NAME_IFA };
                }
            }
            return new String[] { DeviceIdentifierType.SYSTEM_NAME_ADID };
        } else if (length == 40) { // sha1
            // UDID (iOS<=5) - 40-digit sequence of letters and numbers (0e83ff56a12a9cf0c7290cbb08ab6752181fb54b)
            return new String[] { DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1, DeviceIdentifierType.SYSTEM_NAME_HIFA, DeviceIdentifierType.SYSTEM_NAME_DPID };
        } else if (length == 32) { // md5 (128bit->32hex)
            return new String[] { DeviceIdentifierType.SYSTEM_NAME_ADID_MD5, DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5 };
        } else if (length == 16) { // Android ID 64bit->16hex (9774d56d682e549c)
            return new String[] { DeviceIdentifierType.SYSTEM_NAME_ADID };
        } else if (length == 27) { // 
            return new String[] { DeviceIdentifierType.SYSTEM_NAME_GOUID }; // CAESENCiLX-jcKzH5a8Ro0LvA1c, CAESEL88R8tiv4gra08nD_7IG4w
        } else {
            return new String[] { "x" + length }; // ehm...
        }

    }

    @ResponseBody
    @RequestMapping(value = "/deviceid/{deviceIdType}.{deviceId}", method = RequestMethod.GET, produces = "application/json")
    public String getDeviceData(@PathVariable("deviceIdType") String deviceIdTypeSpec, @PathVariable("deviceId") String deviceId) throws IOException {

        Long deviceIdType = DbgUiUtil.tryToLong(deviceIdTypeSpec);
        if (deviceIdType == null) {
            deviceIdType = domainCacheManager.getCache().getDeviceIdentifierTypeIdBySystemName(deviceIdTypeSpec);
            if (deviceIdType == null) {
                throw new IllegalArgumentException("Unknown deviceIdType: " + deviceIdTypeSpec);
            }
        }

        DeviceData deviceData = deviceReader.getData(deviceIdType + "." + deviceId);
        if (deviceData == null) {
            return "";
        }
        Set<Long> audienceIds = deviceData.getAudienceIds();
        Map<String, Object> jsonMap = getMatchingCampaigns(audienceIds, CampaignAudienceDto.AudienceType.LOCATION);
        return DebugBidController.debugJsonMapper.writeValueAsString(jsonMap);
    }
}
