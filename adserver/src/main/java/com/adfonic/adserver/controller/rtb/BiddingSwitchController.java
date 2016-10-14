package com.adfonic.adserver.controller.rtb;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class BiddingSwitchController {

    private static final transient Logger LOG = Logger.getLogger(BiddingSwitchController.class.getName());

    public static volatile Boolean BIDDING_ENABLED = Boolean.TRUE;

    @Value("${Rtb.biddingSwitch.authCode:30d9529ff51c6977}")
    private String swichAuthCode = "30d9529ff51c6977";

    // bidding master switch
    @RequestMapping(value = "/bms", method = RequestMethod.GET, produces = "text/plain")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getBidRequest(HttpServletRequest request, HttpServletResponse response) {
        String retval = "bidding: " + BIDDING_ENABLED;

        return retval;
    }

    @RequestMapping(value = "/bms/{authCode}/{action}", method = RequestMethod.POST, produces = "text/plain")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleBidRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String authCode, @PathVariable String action) {
        if (swichAuthCode.equals(authCode)) {
            boolean toAction = action.toLowerCase().equals("on") || action.toLowerCase().equals("true");
            LOG.info("Swtiching bidding switch from : " + BIDDING_ENABLED + " to " + toAction);
            BIDDING_ENABLED = toAction;
        } else {
            LOG.warning("Bidding switch hit with invalid swich code: " + authCode);
        }

        return getBidRequest(request, response);
    }

}
