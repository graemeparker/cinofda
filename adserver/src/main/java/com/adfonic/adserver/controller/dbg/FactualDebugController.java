package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.http.ApiClient;
import com.byyd.breaker.CircuitTargetTemplate.TargetBreaker;
import com.byyd.factual.FactualOnPremHttpClient;
import com.byyd.factual.MatchResponse;

/**
 * http://developer.factual.com/geopulse-on-prem-http/
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping(FactualDebugController.URL_CONTEXT)
public class FactualDebugController {

    public static final String URL_CONTEXT = "/adserver/factual";

    @Autowired
    private FactualOnPremHttpClient client;

    public void printForm(PrintWriter writer) {

        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("Proximity<br/>");
        writer.println("Latitude: <input name='latitude' size='15' />");
        writer.println("Longitude: <input name='longitude' size='15' />");
        writer.println("<br/>Audience<br/>");
        writer.println("DeviceId: <input name='deviceId' size='44' />");
        writer.println("<br/>");
        writer.println("<input type='submit' value='Query'/>");
        //writer.println("<a href='https://api.factual.com/categories?KEY=Y7AxNaM1pNNxqTqgeDsbBq3HX9sUUmfDXKLJ9fCC' >Categories</a>");
        writer.println("</form>");
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public void formView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        ApiClient apiClient = client.getApiClient();
        writer.println("Connections: " + apiClient.getTotalStats());
        writer.println("<a href='" + URL_CONTEXT + "/reset'>Reset</a><br/>");
        List<TargetBreaker<HttpHost>> targetBreakers = apiClient.getBreakerTemplate().getTargetBreakers();
        writer.print("Servers: ");
        for (TargetBreaker<HttpHost> targetBreaker : targetBreakers) {
            HttpHost target = targetBreaker.getTarget();
            int port = target.getPort();
            String statusUrl = target.getSchemeName() + "://" + target.getHostName() + (port != -1 ? ":" + port : "") + "/zz/status";
            boolean broken = targetBreaker.getBreaker().isBroken();
            writer.print("<a href='" + statusUrl + "'>" + target.getHostName() + (broken ? " DOWN" : "") + "</a> ");

        }
        printForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public String formPost(@RequestParam(name = "latitude", required = false) Double latitude, @RequestParam(name = "longitude", required = false) Double longitude,//
            @RequestParam(name = "deviceId", required = false) String deviceId) throws IOException {

        Map<String, Object> response = new HashMap<>();
        if (StringUtils.isNotBlank(deviceId)) {
            List<MatchResponse> factualResponse = client.audience(deviceId);
            response.put("audience", factualResponse);
        }
        if (latitude != null && longitude != null) {
            List<MatchResponse> factualResponse = client.proximity(latitude, longitude);
            response.put("proximity", factualResponse);
        }

        if (response.isEmpty()) {
            return "No input. No query!";
        } else {
            return DebugBidController.debugJsonMapper.writeValueAsString(response);
        }
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET, produces = "text/html")
    public void reset(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        client.reset();
        httpResponse.sendRedirect(URL_CONTEXT);
    }
}
