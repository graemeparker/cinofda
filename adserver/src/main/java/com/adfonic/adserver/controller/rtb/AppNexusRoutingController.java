package com.adfonic.adserver.controller.rtb;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.adfonic.adserver.cst.AppNexusShared;

@Controller
@RequestMapping("/anxs")
public class AppNexusRoutingController extends WebApplicationObjectSupport {

    private static final transient Logger LOG = Logger.getLogger(AppNexusRoutingController.class.getName());

    private static final int MIN_IDF_LEN = 18;

    private final String wsBaseUrl;// = "http://localhost:8080/adfonic-webservices/";

    @Autowired
    public AppNexusRoutingController(@Value("${Rtb.appnxs.routing.wsbaseurl}") String wsBaseUrl) {
        this.wsBaseUrl = wsBaseUrl + "/";
    }

    @RequestMapping("/ready")
    public void anxsReady(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "text/plain");
        PrintWriter writer = response.getWriter();
        writer.write("1");
        writer.close();
    }

    @RequestMapping("/ct/{adSpaceExternalID}/")
    public void route1(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID) throws java.io.IOException, ServletException {
        route(request, response, "ct", adSpaceExternalID, ".clk");
    }

    @RequestMapping("/{actionCode}/{adSpaceExternalID}/{impressionExternalIDEtc:.*}")
    public void route(HttpServletRequest request, HttpServletResponse response, @PathVariable String actionCode, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalIDEtc) throws java.io.IOException, ServletException {
        LOG.warning(request.getRequestURI());
        LOG.warning(request.getScheme());
        LOG.warning(request.getServerName());
        LOG.warning(request.getServerPort() + "");
        LOG.warning(request.getServletPath());
        LOG.warning(request.getQueryString());
        LOG.warning(getServletContext().getContextPath());

        if (adSpaceExternalID.length() < MIN_IDF_LEN && adSpaceExternalID.contains(AppNexusShared.ADSPACE_ID_MACRO)) {
            if (!actionCode.equals("bc")) {
                actionCode = "ct";
                impressionExternalIDEtc += ".clk";
            }
            String redirectUrl = wsBaseUrl + actionCode + "/" + adSpaceExternalID + "/" + impressionExternalIDEtc + "?" + request.getQueryString();
            LOG.fine("Redirecting to " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } else {
            String forwardPath = "/" + actionCode + "/" + adSpaceExternalID + "/" + impressionExternalIDEtc;
            LOG.fine("Forwarding to " + forwardPath);
            request.getRequestDispatcher(forwardPath).forward(request, response);
        }

    }

    /*
    TODO /0 or /1 now based on length of adSpaceExtId field
    @RequestMapping("/1/{actionCode}/{adSpaceExternalID}/{impressionExternalIDEtc:.*}")
    public void handlePreview(HttpServletRequest request,
                             HttpServletResponse response,
                             @PathVariable
                             String actionCode,
                             @PathVariable
                             String adSpaceExternalID,
                             @PathVariable
                             String impressionExternalIDEtc) throws java.io.IOException {
        LOG.warning(request.getRequestURI());
        LOG.warning(request.getScheme());
        LOG.warning(request.getServerName());
        LOG.warning(request.getServerPort()+"");
        LOG.warning(request.getServletPath());
        LOG.warning(request.getQueryString());
        LOG.warning(getServletContext().getContextPath());
        
        response.sendRedirect("http://localhost:8080/adfonic-adserver/test/anxs/3/"+adSpaceExternalID+"/"+impressionExternalIDEtc);

    }
    */

    @RequestMapping(value = "/3/{actionCode}/{adSpaceExternalID}/{impressionExternalIDEtc:.*}", produces = "application/json")
    @ResponseBody
    public String handleRedirect(HttpServletRequest request, HttpServletResponse response, @PathVariable String actionCode, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalIDEtc) throws java.io.IOException {
        LOG.warning(request.getRequestURI());
        LOG.warning(request.getScheme());
        LOG.warning(request.getServerName());
        LOG.warning(request.getServerPort() + "");
        LOG.warning(request.getServletPath());
        LOG.warning(request.getQueryString());
        LOG.warning(getServletContext().getContextPath());

        return "{}";
    }

}
