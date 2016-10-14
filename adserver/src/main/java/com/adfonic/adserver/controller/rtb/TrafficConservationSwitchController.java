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

import com.adfonic.adserver.TrafficConservationFilter;

@Controller
public class TrafficConservationSwitchController {

    private static final transient Logger LOG = Logger.getLogger(TrafficConservationSwitchController.class.getName());

    public static volatile Boolean ENABLED = Boolean.FALSE;

    @Value("${Rtb.trafficSwitch.authCode:91718bcacf964956}")
    private String swichAuthCode = "91718bcacf964956";

    // bidding master switch
    @RequestMapping(value = "/tcs", method = RequestMethod.GET, produces = "text/plain")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getBidRequest(HttpServletRequest request, HttpServletResponse response) {
        String retval = "capturing: " + ENABLED + ", filter hit: " + TrafficConservationFilter.FILTER_HIT + "\n";

        return retval;
    }

    @RequestMapping(value = "/tcs/{authCode}/{action}", method = RequestMethod.POST, produces = "text/plain")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleBidRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String authCode, @PathVariable String action) {
        if (swichAuthCode.equals(authCode)) {
            boolean toAction = action.toLowerCase().equals("on") || action.toLowerCase().equals("true");
            LOG.info("Swtiching bidding switch from : " + ENABLED + " to " + toAction);
            ENABLED = toAction;
        } else {
            LOG.warning("Bidding switch hit with invalid swich code: " + authCode);
        }

        return getBidRequest(request, response);
    }

}
