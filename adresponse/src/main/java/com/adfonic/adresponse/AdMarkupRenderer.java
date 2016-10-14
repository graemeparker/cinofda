package com.adfonic.adresponse;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.OrtbNativeAdWorker;
import com.adfonic.adserver.rtb.open.v2.VideoV2;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.RtbConfig.AdmProfile;
import com.adfonic.domain.RtbConfig.RtbImpTrackMode;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.ortb.nativead.NativeAdRequest;
import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdResponseWrapper;
import com.adfonic.util.HttpUtils;
import com.adfonic.util.stats.CounterManager;

public class AdMarkupRenderer {

    // AI-65 currently used only by Rtb. This string should not have any url unsafe characters
    public static final String CLICK_FORWARD_URL_PARAM = "ping";

    private static Logger LOG = LoggerFactory.getLogger(AdMarkupRenderer.class.getName());

    private AdResponseLogic adResponseLogic;
    private DisplayTypeUtils displayTypeUtils;
    private CounterManager counterManager;

    /**
     * This is kind of risky. Spring populates this map of bean-name to class-path scanned implementations of MarkupGenerator
     */
    @Autowired
    private Map<String, MarkupGenerator> markupGenMap;

    private final OrtbNativeAdWorker nativeWorker = OrtbNativeAdWorker.instance();

    public AdMarkupRenderer(AdResponseLogic adResponseLogic, DisplayTypeUtils displayTypeUtils, CounterManager counterManager) {
        this.adResponseLogic = adResponseLogic;
        this.displayTypeUtils = displayTypeUtils;
        this.counterManager = counterManager;
    }

    /**
     * Return markup and it's content-type.
     * Be aware that this is called also on Rtb Win notification were TargetingContext is recreated from BidDetails loaded from impression cache, 
     * but there is no Bid Request or Response available 
     */
    public String[] createMarkup(TargetingContext context, Impression impression, CreativeDto creative, FormatDto format, AdSpaceDto adSpace, DisplayTypeDto displayType,
            HttpServletRequest httpRequest, ProxiedDestination proxiedDestination, RtbConfigDto rtbConfig) throws java.io.IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createMarkup creative: " + creative.getId() + ", format: " + format.getSystemName() + ", displayType: " + displayType.getSystemName());
        }
        // Add the required stuff back into the TargetingContext.  This is stuff
        // that we derived at bid time and is required now for ad generation, but
        // since we don't have the full User-Agent header at win notice time, we
        // can't re-derive it now.  Propagate...
        AdComponents adComponents = null;
        try {
            displayTypeUtils.setDisplayType(format, context, displayType);

            adComponents = adResponseLogic.generateBareAdComponents(context, adSpace, creative, proxiedDestination, impression, httpRequest);

            // Add the extendedData stuff to the AdComponents to support markup for
            // creatives with extended capabilities.
            adResponseLogic.addExtendedDataAsNeeded(adComponents, creative);

            adResponseLogic.addBackgroundImageAsNeeded(adComponents, context);

            // AF-1350 - for RTB, always add beacons (the way it was originally)
            String priceMacro = rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.BEACON ? rtbConfig.getSpMacro() : null;
            adResponseLogic.addBeacons(adComponents, creative, adSpace, priceMacro, context, impression, httpRequest);
        } catch (Exception e) {
            throw e;
        }

        // Post-process the AdComponents if needed, substituting %man% and %phn%, etc.
        adResponseLogic.postProcessAdComponents(adComponents, context);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + adComponents);
        }

        AdmProfile markupProfile = rtbConfig.getAdmProfile();
        boolean renderBeacons = rtbConfig.getImpTrackMode() == RtbImpTrackMode.AD_MARKUP;

        String escapedClickForwardURL;
        String prefixOnEscape = rtbConfig.getPrefixonEscapedURLs();
        if (prefixOnEscape != null) {
            String destinationURL = adComponents.getDestinationUrl();
            adComponents.setDestinationUrl(prefixOnEscape + HttpUtils.urlEncode(destinationURL));
        } else if ((escapedClickForwardURL = rtbConfig.getEscapedClickForwardURL()) != null) {
            String destinationURL = adComponents.getDestinationUrl();
            // Currently destinationURL does not have any params. Check, just in case in the future we do have
            // CLICK_FORWARD_URL should not need encoding and escapedClickForwardURL should be already escaped
            adComponents.setDestinationUrl(destinationURL + (destinationURL.indexOf('?') == -1 ? "?" : "&") + CLICK_FORWARD_URL_PARAM + "=" + escapedClickForwardURL);
        }

        // To build NativeAdResponse we need original NativeAdRequest to preserve native asset ids  
        // But on win notification, there is only BidRetails but no bid request or response -> no NativeAdRequest, we need put whole NativeAdRequest into BidDetails probably
        NativeAdRequest nativeAdRequest = context.getAttribute(TargetingContext.NATIVE_REQUEST);
        if (nativeAdRequest != null) {
            NativeAdResponseWrapper nativeResponse = nativeWorker.buildResponse(nativeAdRequest, creative, adComponents, context, renderBeacons);
            context.setAttribute(TargetingContext.NATIVE_RESPONSE, nativeResponse);
            String nativeJson = nativeWorker.toString(nativeResponse);
            return new String[] { nativeJson, Constant.APPL_JSON };
        }

        // Video ads (VAST)
        boolean isVideo = format.getSystemName().startsWith(SystemName.FORMAT_VIDEO_PREFIX);
        if (isVideo) {
            String vastXml;
            Integer vprotocol = context.getAttribute(TargetingContext.VIDEO_PROTOCOL);
            // When not set of VAST InLine allowed -> Go for InLine with trackers added
            if (vprotocol == null || vprotocol == VideoV2.VideoProtocol.VAST_2_0_CODE) {
                // This can be called after bidding or on win notification 
                List<String> impTrackList = context.getAttribute(TargetingContext.IMP_TRACK_LIST);
                vastXml = VastTagProcessor.buildVastInLine(creative, adSpace, context, impression, adComponents.getDestinationUrl(), impTrackList);
            } else if (vprotocol == VideoV2.VideoProtocol.VAST_2_0_WRAPPER_CODE) {
                // If only VAST Wrapper allowed - Create it on fly and point it to our VAST controller   
                String vastTagUrl = VhostManager.makeBaseUrl(httpRequest, Constant.VAST_URI_PATH, impression.getSslRequired()).append('/').append(adSpace.getExternalID())
                        .append('/').append(impression.getExternalID()).toString();
                vastXml = VastTagProcessor.buildVastWrapper(vastTagUrl, impression.getExternalID(), null);
            } else {
                throw new IllegalStateException("Unsupported VAST protocol: " + vprotocol);
            }

            return new String[] { vastXml, Constant.APPL_XML };
        }

        // Good old HTML markup
        MarkupGenerator markupGenerator = markupGenMap.get(markupProfile.name());
        if (markupGenerator == null) {
            throw new IllegalStateException("MarkupGenerator not found: " + markupProfile.name());
        }
        String htmlMarkup = markupGenerator.generateMarkup(adComponents, context, adSpace, creative, impression, renderBeacons);
        return new String[] { htmlMarkup, Constant.TEXT_HTML };

    }

}
