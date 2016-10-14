package com.adfonic.tracker.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.InstallService;
import com.adfonic.tracker.controller.view.HtmlView;
import com.adfonic.tracker.controller.view.JsonView;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.adfonic.util.AdtruthUtil;
import com.adfonic.util.BasicAuthUtils;
import com.adfonic.util.DeviceIdentifierUtils;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@Controller
public class InstallController extends AbstractTrackerController {

    private static final transient Logger LOG = LoggerFactory.getLogger(InstallController.class.getName());

    // Package to expose these to unit tests
    static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(Creative_.campaign).addLeft(Campaign_.currentRichMediaAdServingFee).build();

    static final FetchStrategy AD_SPACE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(AdSpace_.publication).build();

    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private ClickService clickService;
    @Autowired
    private InstallService installService;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private Properties trackerProperties;
    @Autowired
    private HtmlView htmlView;
    @Autowired
    private JsonView jsonView;
    @Autowired
    private TrackerKafka trackerKafka;
    @Autowired
    private V1DomainModelMapper mapper;

    /**
     * De-duplicate an install tracking request where the advertiser has passed
     * us a campaign's applicationID and at least one device identifier (d.*=???)
     */
    // AF-1668 - need to specify a liberal regex here so appIds with periods don't have the "file extension" chopped off.
    @RequestMapping("/is/{appId:.+}")
    @ResponseBody
    public Map<String, Object> deDupInstallByDeviceIdentifiers(HttpServletRequest request, @PathVariable String appId, @RequestParam(required = false) String claim) {
        LOG.debug("Handling install tracking request for appId={}, claim={}", appId, claim);

        List<DeviceIdentifierType> didTypes = deviceManager.getAllDeviceIdentifierTypes();

        // The advertiser has passed us some form of d.<deviceIdentifierType>=<deviceIdentifier>,
        // so first step is to build a map of those device identifiers.
        Map<String, String> didsBySystemName = getSuppliedDeviceIdentifiersBySystemName(request, didTypes);

        // Auto-promote device identifiers to other forms as applicable
        promoteDeviceIdentifiers(didsBySystemName);

        if (didsBySystemName.isEmpty()) {
            // If they didn't supply any device identifiers, there's nothing to do
            return ConversionController.buildErrorResponse("No device identifier(s) provided, must pass d.*=...");
        }

        Click click = null;
        // This DeviceManager call returns DeviceIdentifierTypes in precedence order
        for (DeviceIdentifierType deviceIdentifierType : didTypes) {
            String deviceIdentifier = didsBySystemName.get(deviceIdentifierType.getSystemName());
            if (deviceIdentifier != null) { // That device identifier type was supplied
                // Look up the Click by this device identifier
                LOG.debug("Attempting click lookup via {}", deviceIdentifierType.getSystemName());

                // Note that the click device identifiers are stored in secure form, since
                // adserver ensures they're each made secure when deriving them.
                // AF-1282 - make sure the UDID gets lowercased prior to SHA1'ing it,
                // since that's what adserver is doing.  This fixes a rare scenario
                // where the publisher passes us a mixed case version of a device id.
                String secureForm = deviceIdentifierType.isSecure() ? deviceIdentifier : DigestUtils.sha1Hex(deviceIdentifier);

                click = clickService.getClickByAppIdAndDeviceIdentifier(appId, deviceIdentifierType.getId(), secureForm);
                if (click != null) {
                    // We found the Click, we're good to proceed
                    LOG.debug("Found Click via {}={}", deviceIdentifierType.getSystemName(), deviceIdentifier);
                    break;
                }
            }
            // Otherwise just continue and try the next device identifier type
        }

        if (click != null) {
            return attemptInstall(click, appId, claim);
        } else {
            return scheduleInstallRetry(appId, claim, didsBySystemName);
        }
    }

    @RequestMapping("/ai/{appId:.+}")
    public ModelAndView deDupInstallByAdTruth(HttpServletRequest request, HttpServletResponse response, @PathVariable String appId, @RequestParam(required = false) String claim) {
        // Look for d.adtruth_data in the request
        String adTruthData = request.getParameter("d.adtruth_data");
        Map<String, Object> model = null;
        if (adTruthData == null) {
            // attempt install with available DIs
            LOG.debug("No AdTruth Data found. Attempting install via available device identifiers");
            model = deDupInstallByDeviceIdentifiers(request, appId, claim);
            if (!((int) model.get(RESPONSE_PARAM_SUCCESS) == 1)) {
                // install with any of the available device identifiers wasn't successful
                LOG.info("Install unsuccessfull via available device identifiers");

                model.put("app_id", appId);
                model.put(RESPONSE_PARAM_SUCCESS, 0);
                model.put(RESPONSE_PARAM_ERROR, UNRECOGNIZED_DEVICE_IDENTIFIERS_ERROR);
                LOG.info("Render *Redirect Harness* to generate AdTruthData using htmlView");
                return htmlView.render(model);
            }

            LOG.info("Render *Redirect Harness* to generate AdTruthData using jsonView");
            return jsonView.render(model, response);
        } else {

            Map<String, String> atidDeviceIdentifiersBySystemName = new LinkedHashMap<String, String>();
            // AD-252 Adtruth want to log various deviceidentifiers that are supplied.
            Map<String, String> metaDataMapforAdtruthLogging = getSuppliedDeviceIdentifiersBySystemName(request, deviceManager.getAllDeviceIdentifierTypes());
            // AD-252 Adtruth want to log the event_type.
            metaDataMapforAdtruthLogging.put("event_type", AdAction.INSTALL.name().toUpperCase());
            //get the browserip
            String browserIp = request.getHeader("X-FORWARDED-FOR");
            if (browserIp == null) {
                browserIp = request.getRemoteAddr();
                LOG.debug("Couldn't find browserIp using X-FORWARDED-FOR, using request.RemoteAddress for client IP {}", browserIp);
            }
            // calculate the atid from the adTruthData
            String atid = AdtruthUtil.getAtid(request, adTruthData, browserIp, metaDataMapforAdtruthLogging);
            //LOG.info("Generated AdTruthId from AdTruthData: " + atid);
            LOG.info("Generated AdTruthId=" + atid + " from AdTruthData: " + adTruthData);
            DeviceIdentifierType deviceIdentifierType = deviceManager.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_ATID);
            //maybe we don't need to do this as atid is always considered as secure
            String secureForm = deviceIdentifierType.isSecure() ? atid : DigestUtils.sha1Hex(atid);
            Click click = clickService.getClickByAppIdAndDeviceIdentifier(appId, deviceIdentifierType.getId(), secureForm);
            atidDeviceIdentifiersBySystemName.put(deviceIdentifierType.getSystemName(), secureForm);

            if (click == null) {
                // click wasn't found with this atid so doing an install retry.
                return jsonView.render(scheduleInstallRetry(appId, claim, atidDeviceIdentifiersBySystemName), response);
            }
            // found the click, attempt install with atid
            return jsonView.render(attemptInstall(click, appId, claim), response);
        }
    }

    /**
     * Remove this method once all advertisers have been migrated to the
     * new d.* methodology.
     *
     * @deprecated
     */
    @Deprecated
    @RequestMapping("/is/{appId}/{udid}")
    @ResponseBody
    public Map<String, Object> deDupInstallByAppIdAndUdid(@PathVariable String appId, @PathVariable String udid, @RequestParam(required = false) String claim) {
        String localUdid = udid;
        String localAppId = appId;

        LOG.debug("Handling install tracking request for appId={}, udid={}, claim={}", localAppId, localUdid, claim);

        // Look up the Click by DPID (SHA1 of UDID)
        // AF-1282 - make sure the UDID gets lowercased prior to SHA1'ing it,
        // since that's what adserver is doing.  This fixes a rare scenario
        // where the publisher passes us a mixed case version of a device id.
        DeviceIdentifierType dpidType = deviceManager.getDeviceIdentifierTypeBySystemName("dpid");
        String dpid = DigestUtils.shaHex(localUdid.toLowerCase());
        Click click = clickService.getClickByAppIdAndDeviceIdentifier(localAppId, dpidType.getId(), dpid);
        if (click == null) {
            // Try the reverse...the advertiser might be a ssabmud.
            click = clickService.getClickByAppIdAndDeviceIdentifier(localUdid, dpidType.getId(), DigestUtils.shaHex(localAppId.toLowerCase()));
            if (click != null) {
                String swapparoni = localUdid;
                localUdid = localAppId;
                localAppId = swapparoni;
                LOG.warn("Advertiser (creative id={}) passed appId/udid in reverse, appId={}, udid={}, click externalID={}", click.getCreativeId(), localAppId, localUdid,
                        click.getExternalID());
            } else {
                // SC-2 - schedule a retry for some point in the future, since
                // the click may just not have been tracked yet (i.e. when
                // there's an AdEvent backlog).
                LOG.info("Click not found for appId={}, udid={}, scheduling retry", localAppId, localUdid);
                installService.scheduleInstallRetry(localAppId, dpidType.getId(), dpid, canClaim(claim));

                return ConversionController.buildErrorResponse(UNKNOWN_UNIQUE_IDENTIFIER_ERROR);
            }
        }

        // Make sure click.deviceIdentifiers is populated
        clickService.loadDeviceIdentifiers(click);

        try {
            trackInstall(click, canClaim(claim));
            return ConversionController.OK_RESPONSE;
        } catch (DuplicateException e) {
            LOG.warn("Duplicate install tracking request, appId={}, udid={}, click externalID={}", localAppId, localUdid, click.getExternalID());
            return ConversionController.buildErrorResponse(DUPLICATE_ERROR);
        } catch (DataDependencyException e) {
            return ConversionController.buildErrorResponse(INTERNAL_ERROR);
        }
    }

    private Map<String, Object> scheduleInstallRetry(String appId, String claim, Map<String, String> didsBySystemName) {
        // We didn't find the respective click.  Either the advertiser didn't
        // pass us any device identifiers, or the one(s) they provided didn't
        // match up with any clicks in our system.
        LOG.info("Click not found for appId={} using {}, scheduling retry", appId, didsBySystemName);

        // SC-2 - schedule a retry for some point in the future, since the
        // click may just not have been tracked yet (i.e. when there's an
        // AdEvent backlog).  We keep it simple and just retry the highest
        // precedence device identifier supplied.
        Map.Entry<String, String> firstEntry = didsBySystemName.entrySet().iterator().next();
        installService.scheduleInstallRetry(appId, deviceManager.getDeviceIdentifierTypeBySystemName(firstEntry.getKey()).getId(), firstEntry.getValue(), canClaim(claim));

        return ConversionController.buildErrorResponse(UNRECOGNIZED_DEVICE_IDENTIFIERS_ERROR);
    }

    private Map<String, Object> attemptInstall(Click click, String appId, String claim) {

        // We located the Click, try to track the install
        try {
            trackInstall(click, canClaim(claim));
            return ConversionController.OK_RESPONSE;
        } catch (DuplicateException e) {
            LOG.warn("Duplicate install tracking request, appId={}, click.externalId={}", appId, click.getExternalID());
            return ConversionController.buildErrorResponse(DUPLICATE_ERROR);
        } catch (DataDependencyException e) {
            return ConversionController.buildErrorResponse(INTERNAL_ERROR);
        }
    }

    private Map<String, String> getSuppliedDeviceIdentifiersBySystemName(HttpServletRequest request, List<DeviceIdentifierType> allDeviceIdentifierTypesInPrecedenceOrder) {
        Map<String, String> suppliedDeviceIdentifiersBySystemName = new LinkedHashMap<String, String>();
        for (DeviceIdentifierType deviceIdentifierType : allDeviceIdentifierTypesInPrecedenceOrder) {
            String param = "d." + deviceIdentifierType.getSystemName();
            String deviceIdentifier = request.getParameter(param);
            if (StringUtils.isNotBlank(deviceIdentifier)) {
                // MAD-1544 First validate against our regex for the type
                if (!deviceIdentifier.matches(deviceIdentifierType.getValidationRegex())) {
                    LOG.info("Invalid device identifier: " + param + "=" + deviceIdentifier);
                } else {
                    // SC-215 - IFA needs to be normalized for the rest, force the input value to lowercase here
                    deviceIdentifier = DeviceIdentifierUtils.normalizeDeviceIdentifier(deviceIdentifier, deviceIdentifierType.getSystemName());
                    suppliedDeviceIdentifiersBySystemName.put(deviceIdentifierType.getSystemName(), deviceIdentifier);
                }
            }
        }
        return suppliedDeviceIdentifiersBySystemName;
    }

    private void trackInstall(Click click, boolean claim) throws DuplicateException, DataDependencyException {
        if (!claim) {
            return; // outta here...don't do anything else
        }

        // Before we do anything else, make sure we haven't already logged the install.
        if (!installService.trackInstall(click)) {
            // Yup, it was already done and tracked
            throw new DuplicateException();
        }

        Creative creative = creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY);
        if (creative == null) {
            LOG.error("Failed to load creative " + click.getCreativeId());
            throw new DataDependencyException();
        }
        AdSpace adSpace = publicationManager.getAdSpaceById(click.getAdSpaceId(), AD_SPACE_FETCH_STRATEGY);
        if (adSpace == null) {
            LOG.error("Failed to load adSpace " + click.getAdSpaceId());
            throw new DataDependencyException();
        }

        // Log the event via data collector using the values from the initial Click, zero cost, and AdAction.INSTALL.
        AdEvent event = adEventFactory.newInstance(AdAction.INSTALL);
        event.populate(click, creative.getCampaign().getId(), adSpace.getPublication().getId());

        LOG.debug("Queueing INSTALL AdEvent for Creative id={}, AdSpace id={}", creative.getId(), adSpace.getId());
        
        //Log the event to kafka
        try {
            net.byyd.archive.model.v1.AdEvent ae = mapper.map(event);
            LOG.info("Logging to kafka INSTALL AdEvent for Creative id={}, AdSpace id={}", ae.getCreativeId(), ae.getAdSpaceId());
            trackerKafka.logAdEvent(ae);
        } catch (Exception e) {
            LOG.error("Error logging to kafka " + e.getMessage());
        }
    }

    /**
     * Since this method is more "RESTful" than others in tracker, in that it needs
     * to return specific HTTP status codes (201, 200, 401, 404), we make use of
     * Spring's ResponseEntity mechanism.
     *
     * @return a ResponseEntity with the given HTTP status code that will be returned
     *         to the caller.  The string message included is informational only.
     */
    @RequestMapping(value = "/ac/{applicationID}/{advertiserExternalID}/{creativeExternalID}/{impressionExternalID}", method = RequestMethod.POST)
    public ResponseEntity<String> trackAuthenticatedInstall(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String applicationID, @PathVariable String advertiserExternalID, @PathVariable String creativeExternalID, @PathVariable String impressionExternalID) {
        LOG.debug("Handling authenticated install tracking request for applicationID={}, advertiserExternalID={}, impression={}, creativeExternalID={}", applicationID,
                advertiserExternalID, impressionExternalID, creativeExternalID);

        // Authenticate
        if (StringUtils.isEmpty(authorizationHeader)) {
            return new ResponseEntity<String>("Authorization header not supplied", HttpStatus.UNAUTHORIZED);
        }

        String[] creds;
        try {
            creds = BasicAuthUtils.decodeAuthorizationHeader(authorizationHeader);
        } catch (BasicAuthUtils.BasicAuthException e) {
            // Return 401 Unauthorized
            return new ResponseEntity<String>("Invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String username = creds[0];
        String suppliedPassword = creds[1];
        LOG.debug("Authenticating username={}, password{}", username, StringUtils.repeat("*", suppliedPassword.length()));

        // Look up the credentials from tracker config
        String expectedPassword = trackerProperties.getProperty("auth." + username);
        if (expectedPassword == null) {
            // The username isn't configured -- return 401 Unauthorized
            return new ResponseEntity<String>("Username not recognized", HttpStatus.UNAUTHORIZED);
        } else if (!expectedPassword.equals(suppliedPassword)) {
            // The password doesn't match -- return 401 Unauthorized
            return new ResponseEntity<String>("Invalid password", HttpStatus.UNAUTHORIZED);
        }

        // The user is authenticated...good to go

        // Look up the Click
        Click click = clickService.getClickByExternalID(impressionExternalID);
        if (click == null) {
            LOG.info("Click not found for impressionExternalId={}, scheduling retry", impressionExternalID);

            // SC-2 - schedule a retry for some point in the future, since the
            // click may just not have been tracked yet (i.e. when there's an
            // AdEvent backlog).
            installService.scheduleAuthenticatedInstallRetry(impressionExternalID);

            // Return 404 Not Found
            return new ResponseEntity<String>("Click not found (invalid impression ID)", HttpStatus.NOT_FOUND);
        }

        // Make sure the app id matches...we need to load the Creative to check that
        Creative creative = creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY);
        AdSpace adSpace = publicationManager.getAdSpaceById(click.getAdSpaceId(), AD_SPACE_FETCH_STRATEGY);
        if (creative == null || adSpace == null) {
            LOG.error("Failed to load a required object, creativeId={}, adSpaceId={}", click.getCreativeId(), click.getAdSpaceId());
            return new ResponseEntity<String>("Required object not found", HttpStatus.NOT_FOUND);
        }

        if (!applicationID.equals(creative.getCampaign().getApplicationID())) {
            // Return 404 Not Found
            return new ResponseEntity<String>("Invalid applicationID", HttpStatus.NOT_FOUND);
        }

        // Cool the app id matches, we're good to go

        AdEvent event = adEventFactory.newInstance(AdAction.INSTALL);
        event.populate(click, creative.getCampaign().getId(), adSpace.getPublication().getId());

        // Before we do anything else, make sure we haven't already logged the install.
        if (!installService.trackInstall(click)) {
            // Yup, it was already done and tracked
            LOG.warn("Duplicate install tracking request, impression={}", click.getExternalID());
            // Return 200 OK
            return new ResponseEntity<String>(DUPLICATE_ERROR, HttpStatus.OK);
        }

        // Make sure click.deviceIdentifiers is populated
        clickService.loadDeviceIdentifiers(click);

        //Log the event to kafka
        try {
            net.byyd.archive.model.v1.AdEvent ae = mapper.map(event);
            LOG.debug("Logging to kafka INSTALL AdEvent for Creative id={}, AdSpace id={}", ae.getCreativeId(), ae.getAdSpaceId());
            trackerKafka.logAdEvent(ae);
        } catch (Exception e) {
            LOG.error("Error logging to kafka " + e.getMessage());
        }

        // Return 201 Created
        return new ResponseEntity<String>("Install tracked", HttpStatus.CREATED);
    }

    /**
     * Auto-promote device identifiers to other secure forms as applicable.
     * For example, d.android and d.udid may be "promoted" to d.dpid when
     * d.dpid isn't explicitly specified.  Likewise, d.android may be
     * promoted to d.odin-1.
     */
    static void promoteDeviceIdentifiers(Map<String, String> didsBySystemName) {
        // AF-1204 - since we don't store Android ID or UDID in tracker db,
        // we need to promote Android ID or UDID to DPID if supplied.
        if (!didsBySystemName.containsKey(DeviceIdentifierType.SYSTEM_NAME_DPID)) {
            LOG.debug("dpid not supplied, checking for possible promotions");
            String androidId = didsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ANDROID);
            if (StringUtils.isNotEmpty(androidId)) {
                // Promote Android ID to DPID by SHA1'ing it
                LOG.debug("Promoting android to dpid");
                didsBySystemName.put(DeviceIdentifierType.SYSTEM_NAME_DPID, DigestUtils.sha1Hex(androidId));
            } else {
                String udid = didsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_UDID);
                if (StringUtils.isNotEmpty(udid)) {
                    // Promote UDID to DPID by SHA1'ing it
                    LOG.debug("Promoting udid to dpid");
                    didsBySystemName.put(DeviceIdentifierType.SYSTEM_NAME_DPID, DigestUtils.sha1Hex(udid));
                }
            }
        }

        // We can also promote Android ID to ODIN-1 if ODIN-1 wasn't supplied
        if (!didsBySystemName.containsKey(DeviceIdentifierType.SYSTEM_NAME_ODIN_1)) {
            LOG.debug("odin-1 not supplied, checking for possible promotions");
            String androidId = didsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ANDROID);
            if (StringUtils.isNotEmpty(androidId)) {
                // Promote Android ID to DPID by SHA1'ing it
                LOG.debug("Promoting android to odin-1");
                didsBySystemName.put(DeviceIdentifierType.SYSTEM_NAME_ODIN_1, DigestUtils.sha1Hex(androidId));
            }
        }

        // We need to remove Android ID and UDID from the map, since the map
        // is going to be iterated for click lookups, and Android ID and UDID
        // aren't stored in tracker db.
        didsBySystemName.remove(DeviceIdentifierType.SYSTEM_NAME_ANDROID);
        didsBySystemName.remove(DeviceIdentifierType.SYSTEM_NAME_UDID);

        // SC-128 - ifa -> hifa
        if (!didsBySystemName.containsKey(DeviceIdentifierType.SYSTEM_NAME_HIFA)) {
            LOG.debug("hifa not supplied, checking for possible promotions");
            String ifa = didsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IFA);
            if (StringUtils.isNotEmpty(ifa)) {
                // Promote ifa to hifa by SHA1'ing it
                LOG.debug("Promoting ifa to hifa");
                didsBySystemName.put(DeviceIdentifierType.SYSTEM_NAME_HIFA, DigestUtils.sha1Hex(ifa));
            }
        }
        // And remove ifa from the map, since only hifa is tracked
        didsBySystemName.remove(DeviceIdentifierType.SYSTEM_NAME_IFA);
    }

    Map<Long, String> getAllDeviceIdentifiers(Click click, Map<String, String> didsBySystemName) {
        // Make sure click.deviceIdentifiers is populated
        clickService.loadDeviceIdentifiers(click);

        Map<Long, String> allDeviceIdentifiers = new LinkedHashMap<Long, String>();
        // We build the union of the ad-served-time device identifiers...
        allDeviceIdentifiers.putAll(click.getDeviceIdentifiers());
        // ...with any additional device identifiers supplied at install time
        for (Map.Entry<String, String> entry : didsBySystemName.entrySet()) {
            allDeviceIdentifiers.put(deviceManager.getDeviceIdentifierTypeBySystemName(entry.getKey()).getId(), entry.getValue());
        }
        return allDeviceIdentifiers;
    }

    public static final class DuplicateException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static final class DataDependencyException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    static boolean canClaim(String claimParam) {
        return !"0".equals(claimParam);
    }
}
