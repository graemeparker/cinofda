package com.adfonic.adserver.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdserverConstants;
import com.adfonic.adserver.AdserverUtils;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.rtb.impl.AdsquareWorker;
import com.adfonic.adserver.rtb.impl.RtbBidLogicImpl;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.jms.ClickMessage;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

@Component
public class ClickUtils {

    private static final transient Logger LOG = Logger.getLogger(ClickUtils.class.getName());

    @Autowired
    private AdServerStats astats;
    @Autowired
    private KryoManager kryoManager;
    @Autowired
    private BackupLogger backupLogger;
    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;
    @Autowired
    private AdsquareWorker adsquareWorker;

    @Value("${click.default.ttlSeconds}")
    private int clickDefaultTtlSeconds;
    @Value("${click.installTracking.ttlSeconds}")
    private int installTrackingTtlSeconds;
    @Value("${click.conversionTracking.ttlSeconds}")
    private int conversionTrackingTtlSeconds;
    @Value("${ClickThrough.fallbackURL}")
    private String fallbackURL;

    private final Pattern yospaceVideoUrlPattern;

    private static final UrlValidator urlValidator = new UrlValidator(new String[] { "http" });

    @Autowired
    private JmsUtils jmsUtils;

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;

    @Autowired
    public ClickUtils(@Value("${Yospace.destination.pattern}") String yospaceVideoUrlPattern) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Yospace URL pattern: " + yospaceVideoUrlPattern);
        }
        this.yospaceVideoUrlPattern = Pattern.compile(yospaceVideoUrlPattern);
    }

    // AI - 65 rtb adx needs us to forward the clicks. Currently info(like validtion pattern) is stored in RTBconfig
    //  not to impact space for other stuff. If we generalize this feature, then we don't need rtb dependency. 
    // Map so as to avoid compiling and creating pattern object each time.
    private ConcurrentMap<String, Pattern> fwdURLvalidator = new ConcurrentHashMap<String, Pattern>();

    private Pattern getPattern(String patternString) {// Since pattern is in RTB_LOGIC table, cannot be injected
        Pattern pattern = fwdURLvalidator.get(patternString);// performance suggestion from jbloch
        if (pattern == null) {// *is_not* doublecheckedlocking since putIfAbsent is good by itself. just performance
            Pattern patSimul;
            pattern = fwdURLvalidator.putIfAbsent(patternString, patSimul = Pattern.compile(patternString));
            if (pattern == null) {
                return patSimul;
            }
        }
        return pattern;
    }

    /**
     * Utility method for establishing the clicbkthrough target URL for a
     * given Impression and Creative.  If the campaign is Ad-X install
     * tracking enabled, then the URL returned will redirect the user to
     * Ad-X, who will redirect to the actual destination URL.  Otherwise,
     * if the impression represents a proxied destination (3rd party plugin),
     * then the proxied destination URL will be returned.  Otherwise, our
     * destination.data is used to build the target URL.  Click-to-video
     * URLs will have any appropriate tracking information appended
     * automatically, i.e. the clickId and urlId parameters for Yospace.
     * @param impression the Impression
     * @param creative the targeted Creative from the impression
     * @param context the TargetingContext
     * @return the target URL, without %variable% post-processing applied
     */
    public String getTargetUrl(Impression impression, CreativeDto creative) {
        String targetUrl;
        //if a campaign has set installTrackingAdXEnabled as true, then we need to redirect this click to
        //adx which will provide actual destination url, and inform us about the install.
        if (impression.isProxiedDestination()) {
            // The impression was served using a plugin...grab the URL from the proxied destination
            targetUrl = impression.getPdDestinationUrl();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("ProxiedDestination URL: " + targetUrl);
            }
        } else {
            // Regular impression on a regular creative...grab the URL from the creative's destination
            DestinationDto destination = creative.getDestination();
            switch (destination.getDestinationType()) {
            case CALL:
                targetUrl = "tel:" + destination.getData(); // click-to-call URL format
                break;
            case VIDEO:
                targetUrl = destination.getData();
                // See if this is a Yospace video URL
                if (yospaceVideoUrlPattern.matcher(targetUrl).matches()) {
                    // Append clickId and urlId params for Yospace
                    String yospaceTrackerUrlId = adserverDataCacheManager.getCache().getProperties().getProperty("Yospace.tracker.urlId");
                    if (targetUrl.indexOf('?') == -1) {
                        targetUrl = targetUrl + "?clickId=" + impression.getExternalID() + "&urlId=" + yospaceTrackerUrlId;
                    } else {
                        targetUrl = targetUrl + "&clickId=" + impression.getExternalID() + "&urlId=" + yospaceTrackerUrlId;
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Appended Yospace params: " + targetUrl);
                    }
                }
                break;
            default:
                targetUrl = destination.getData();
                break;
            }
        }
        return targetUrl;
    }

    /**
     * Set the click id cookie on an HTTP response to an end user
     * @param response the servlet response object
     * @param impression the impression that was clicked on
     * @param creative the creative that was clicked on, which is used to
     * determine the TTL of the cookie, based on whether or not its campaign
     * requires install or conversion tracking
     */
    public void setClickIdCookie(HttpServletResponse response, Impression impression, CreativeDto creative) {
        // By default, the click expires when the impression expires, but we
        // may extend the TTL based on install or conversion tracking needs.
        CampaignDto campaign = creative == null ? null : creative.getCampaign();
        int ttlSeconds;
        if (campaign != null) {
            ttlSeconds = AdserverUtils.getClickTtlSeconds(impression, campaign.getApplicationID(), campaign.isInstallTrackingEnabled(), campaign.isInstallTrackingAdXEnabled(),
                    campaign.isConversionTrackingEnabled(), clickDefaultTtlSeconds, installTrackingTtlSeconds, conversionTrackingTtlSeconds);
        } else {
            ttlSeconds = AdserverUtils.getClickTtlSeconds(impression, null, null, null, null, clickDefaultTtlSeconds, installTrackingTtlSeconds, conversionTrackingTtlSeconds);
        }

        // Set the click ID cookie for conversion tracking

        // We used to set same cookie twice - one without domain and additional one with domain (.byyd.net)
        // Domain-less cookie usage was removed - MAD-2707 

        Cookie cookie = new Cookie(AdserverConstants.CLICK_ID_COOKIE, impression.getExternalID());

        String cookieDomain = adserverDataCacheManager.getCache().getProperties().getProperty("click.additionalCookieDomain");
        if (StringUtils.isNotBlank(cookieDomain)) {
            // Add the additional cookie with the specific domain
            cookie = new Cookie(AdserverConstants.CLICK_ID_COOKIE, impression.getExternalID());
            cookie.setPath("/");
            cookie.setMaxAge(ttlSeconds);
            cookie.setDomain(cookieDomain);

            response.addCookie(cookie);
            LOG.fine("Cookie " + AdserverConstants.CLICK_ID_COOKIE + " with impression id " + impression.getExternalID() + " added for domain: " + cookieDomain);
        } else {
            LOG.severe("Cookie " + AdserverConstants.CLICK_ID_COOKIE + " cannot be added as click.additionalCookieDomain property is missing");
        }
    }

    /**
     * Send a redirect to a configurable fallback URL
     * @param request the servlet request object
     * @param response the servlet response object
     * @throws java.io.IOException if the servlet API redirect call fails
     */
    public void redirectToFallbackUrl(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        String url;
        if (fallbackURL.startsWith("http")) {
            url = fallbackURL;
        } else {
            url = request.getContextPath() + fallbackURL;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Redirecting click to: " + url);
        }
        response.sendRedirect(url);
    }

    /**
     * Track a click by queueing a JMS message for datacollector to process.
     * Before we do that, we make sure the clickthrough request isn't coming
     * from some device/country other than the one from which the impression
     * originated.  Wes bumped into some fraud issues where impressions from
     * South Africa were then followed up with clicks from India, and from
     * what sounded like desktop browsers to boot.
     * @return TODO
     */
    public boolean trackClick(AdSpaceDto adSpace, CreativeDto creative, Impression impression, TargetingContext context, String clickForwardURLEscd) {
        if (adSpace == null) {
            // The case in which we see this happen is...we just introduced
            // sharding, and Mobclix traffic is switching to its own shard.
            // Because some apps cache DNS longer than TTLs, clicks end up
            // coming into the old byyd.net (which is no longer serving
            // Mobclix) instead of mobclix.byyd.net.  byyd.net doesn't
            // have this given AdSpace in its AdserverDomainCache, so we don't
            // have the AdSpace object handy at this point.
            //
            // Anyway, the catch is that click tracking requires that we pass
            // the Publication id to datacollector, since it currently doesn't
            // have any knowledge of AdSpace per se, just Publication.
            //
            // We don't have a Publication id to pass to datacollector, so
            // there isn't much we can do at this point.  If this problem was
            // something that occurred more than once in a long while, we could
            // write some code around it, allow datacollector to do by-AdSpace-id
            // lookups, or something like that.
            //
            // At a minimum, don't NPE down below where we dereference AdSpace.
            // For now, just log a warning and bail.
            LOG.warning("No AdSpace, not tracking click (probably from another shard)");
            backupLogger.logClickFailure(impression, "no AdSpace", context);
            return false;
        }

        ModelDto model = context.getAttribute(TargetingContext.MODEL);
        if (impression.getModelId() != null && (model == null || !model.getId().equals(impression.getModelId()))) {
            // The click is coming from a different model than the impression
            String modelId = model == null ? "null" : String.valueOf(model.getId());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Click detected with a Model mismatch!!! impression=" + impression.getExternalID() + ", adSpace=" + impression.getAdSpaceId() + "; impression.model="
                        + impression.getModelId() + ", click.model=" + modelId + ", click.ua=" + context.getEffectiveUserAgent());
            }
            astats.increment(adSpace, AsCounter.ClickDeviceModelMismatch);
            backupLogger.logClickFailure(impression, "Model mismatch", context, String.valueOf(impression.getModelId()), modelId);
            return false; // don't track the click
        }

        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (impression.getCountryId() != null && (country == null || !(country.getId().equals(impression.getCountryId())))) {
            // The click is coming from a different country than the impression
            String countryId = country == null ? "null" : String.valueOf(country.getId());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Click detected with a Country mismatch!!! impression=" + impression.getExternalID() + ", adSpace=" + impression.getAdSpaceId()
                        + "; impression.country.id=" + impression.getCountryId() + ", click.country.id=" + countryId + ", click.ip=" + context.getAttribute(Parameters.IP));
            }
            astats.increment(adSpace, AsCounter.ClickCountryMismatch);
            backupLogger.logClickFailure(impression, "Country mismatch", context, String.valueOf(impression.getCountryId()), countryId);
            return false; // don't track the click
        }

        // Query Cassandra and call adsquare api if necessary - later this may be in same query as for impression cache
        if (adsquareWorker.isCountryWhitelisted(country)) {
            adsquareWorker.reportClick(impression.getExternalID());
        }

        // Track the click
        byte[] serializedImpression = kryoManager.writeObject(impression);
        String ipAddress = context.getAttribute(Parameters.IP);
        String userAgentHeader = context.getEffectiveUserAgent();
        ClickMessage clickMessage = new ClickMessage(serializedImpression, creative.getCampaign().getId(), adSpace.getPublication().getId(), ipAddress, userAgentHeader);
        //TODO Clean up code after decomission JMS
        //        adserverJms.logClickMessage(clickMessage);

        // If click_forward specified, attempt to schedule a ping. All is well even if it fails. just log and carry on..
        if (clickForwardURLEscd != null && RtbBidLogicImpl.isRtbEnabled(context)) {// TODO check needed?
            RtbConfigDto rtbConfig = adSpace.getPublication().getPublisher().getRtbConfig();
            if (rtbConfig != null) {// defensive. not needed
                try {
                    String clickForwardURL = URLDecoder.decode(clickForwardURLEscd, "UTF-8");
                    String valdnPattern = rtbConfig.getClickForwardValidationPattern();
                    if (!urlValidator.isValid(clickForwardURL)) {
                        LOG.severe("Invalid click-forward URL on adspace[" + adSpace.getId() + "]; url[" + clickForwardURL + "]");
                    } else if (valdnPattern != null && !getPattern(valdnPattern).matcher(clickForwardURL).matches()) {
                        LOG.severe("Match failed for click-forward URL on adspace[" + adSpace.getId() + "]; url[" + clickForwardURL + "]");
                    } else {
                        jmsUtils.sendText(centralJmsTemplate, JmsResource.CLICK_FORWARD, clickForwardURL);
                    }
                } catch (UnsupportedEncodingException e) {
                    LOG.severe("Unable to decode click Forward URL on adspace[" + adSpace.getId() + "]; url[" + clickForwardURLEscd + "]");
                }
            }
        }

        backupLogger.logClickSuccess(impression, adSpace, clickMessage.getCreationTime(), creative.getCampaign().getId(), context);
        return true;
    }

    /**
     * unifying pre-redirect logic - stuff like macro substitution etc
     */
    public String processRedirectUrl(String redirectUrl, boolean isTracked, AdSpaceDto adSpace, CreativeDto creative, Impression impression, TargetingContext context,
            boolean doProcVars) throws IOException {

        redirectUrl = MarkupGenerator.resolveMacros(redirectUrl, adSpace, creative, impression, context, null, doProcVars, isTracked);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Redirecting to: " + redirectUrl);
        }
        return redirectUrl;
    }

}
