package com.adfonic.adserver.controller.dbg;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.AdserverConstants;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.BeaconController;
import com.adfonic.adserver.controller.ClickRedirectController;
import com.adfonic.adserver.controller.ClickThroughController;
import com.adfonic.adserver.controller.rtb.OpenRtbV1Controller;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;

/**
 * 
 * @author mvanek
 *
 */
@Controller
public class DebugTrackerController {

    private static final transient Logger LOG = Logger.getLogger(DebugTrackerController.class.getName());

    @Autowired
    private RtbWinLogicImpl rtbWinLogic;

    @Autowired
    private BeaconController beaconController;

    @Autowired
    private ClickThroughController clickThroughController;

    @Autowired
    private ClickRedirectController clickRedirectController;

    /**
     * Debug for OpenRtbV1Controller.handleWinNotice...
     */
    @RequestMapping(value = DebugBidController.DBG_WIN_URL + "/{impressionExternalID}")
    public void winNotice(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String impressionExternalID,
            @RequestParam(value = Constant.SP_URL_PARAM, required = false) String settlementPrice) throws java.io.IOException {

        LOG.info("Debug win notice. Impression: " + impressionExternalID + ", SettlementPrice: " + settlementPrice);
        //This is same code as in OpenRtbV1Controller. Maybe we could introduce some sort of debuging
        String[] rendered = rtbWinLogic.winOnRtbNurl(impressionExternalID, settlementPrice, httpRequest);
        if (rendered != null) {
            httpResponse.setHeader("Expires", "0");
            httpResponse.setHeader("Pragma", "No-Cache");
            String contentType = rendered[1] + "; charset=utf-8";
            httpResponse.setContentType(contentType);
            if (contentType.startsWith(Constant.APPL_XML)) {
                OpenRtbV1Controller.addCorsHeaders(httpRequest, httpResponse);
            }
            httpResponse.getWriter().write(rendered[0]);
        } else {
            // no response, no fun
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @RequestMapping(value = DebugBidController.DBG_LOSS_URL + "/{impressionExternalID}")
    public void winNotice(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String impressionExternalID) {
        LOG.info("Rtb Loss Notification. Impression: " + impressionExternalID);
        RtbBidDetails bidDetails = rtbWinLogic.bidLoss(impressionExternalID, "debug");
        if (bidDetails == null) {
            LOG.info("RtbBidDetails not found for Impression: " + impressionExternalID);
        }
    }

    /**
     * Debug for OpenRtbV1Controller.handleBeacon...
     */
    @RequestMapping(DebugBidController.DBG_IMPRESSION_URL + "/{adSpaceExternalID}/{impressionExternalID}.gif")
    public void impression(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID, @PathVariable String impressionExternalID,
            @RequestParam(value = Constant.SP_URL_PARAM, required = false) String settlementPrice) throws java.io.IOException {

        LOG.info("Ad Impression. AdSpace: " + adSpaceExternalID + " Impression: " + impressionExternalID + ", SettlementPrice: " + settlementPrice);
        beaconController.handleBeacon(request, response, adSpaceExternalID, impressionExternalID, settlementPrice);
    }

    /**
     * Debug for ClickThroughController.handleClickThroughRequest...
     */
    @RequestMapping(DebugBidController.DBG_CLICK_THROUGH_URL + "/{adSpaceExternalID}/{impressionExternalID}")
    public void clickThrough(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID, @PathVariable String impressionExternalID,
            @RequestParam(value = AdMarkupRenderer.CLICK_FORWARD_URL_PARAM, required = false) String clickForwardURL) throws java.io.IOException {

        LOG.info("ClickThrough. AdSpace: " + adSpaceExternalID + " Impression: " + impressionExternalID + ", ping: " + clickForwardURL);
        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, clickForwardURL);
    }

    /**
     * Debug for ClickRedirectController.handleClickRedirectRequest...
     */
    @RequestMapping(DebugBidController.DBG_CLICK_REDIRECT_URL + "/{adSpaceExternalID}/{impressionExternalID}")
    public void clickRedirect(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID, @PathVariable String impressionExternalID,
            @RequestParam("redir") String redir) throws java.io.IOException {

        LOG.info("ClickRedirect. AdSpace: " + adSpaceExternalID + " Impression: " + impressionExternalID + ", redir: " + redir);
        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
    }

    /**
     * Client (Browser) conversion
     * https://developer.byyd-tech.com/index.php/Mobile_Site_Conversions
     * 
     * Debug for adfonic-tracker com.adfonic.tracker.controller.ConversionController which supesedes adserver's ConversionTrackingController
     */
    @RequestMapping({ "/rtb/debug/cb/{advertiserExternalID}/conversion.gif", "/rtb/debug/scb/{advertiserExternalID}/conversion.gif" })
    public void conversionFromClient(HttpServletRequest request, HttpServletResponse response, @PathVariable String advertiserExternalID,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws java.io.IOException {
        if (clickExternalID == null) {
            response.getWriter().println("Cookie: '" + AdserverConstants.CLICK_ID_COOKIE + "' not present");
        } else {
            // Clicks are stored in Tracker DB. We can't access it from AdServer so no more checks here
            response.getWriter().println("Cookie: '" + AdserverConstants.CLICK_ID_COOKIE + "' value: " + clickExternalID);
        }
    }

    /**
     * Server to Server conversion
     * https://developer.byyd-tech.com/index.php/Mobile_Site_Conversions
     * 
     * Debug for adfonic-tracker com.adfonic.tracker.controller.ConversionController which supesedes adserver's ConversionTrackingController
     */
    @RequestMapping({ "/rtb/debug/cs/{advertiserExternalID}/{clickExternalID}", "/rtb/debug/scs/{advertiserExternalID}/{clickExternalID}" })
    public void conversionFromServer(@PathVariable String advertiserExternalID, @PathVariable String clickExternalID) throws java.io.IOException {

    }

    /**
     * /is/com.pcloud.pcloud/3003db00614ea48b
     * /is/577741918?d.ifa=3B9F7CDE-4FC5-4D1C-B61E-78DCEFB4B7EC
     * 
     */
    public void install() {

    }
}
