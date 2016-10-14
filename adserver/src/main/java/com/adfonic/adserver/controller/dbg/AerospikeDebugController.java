package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.citrusleaf.CitrusleafClient;
import net.citrusleaf.CitrusleafInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.BidDetails;
import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.ParallelModeCacheService;
import com.adfonic.adserver.rtb.RtbCacheService;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping(AerospikeDebugController.URL_CONTEXT)
public class AerospikeDebugController {

    public static final String URL_CONTEXT = "/adserver/aspike";

    @Autowired
    private ImpressionService impCache;

    @Autowired
    private RtbCacheService rtbCache;

    @Autowired
    private ParallelModeCacheService parCache;

    @Autowired
    private FrequencyCounter frequencyCounter;

    @Value("${Citrusleaf.hostName}")
    String hostname;
    @Value("${Citrusleaf.port}")
    int port;

    @Autowired
    private CitrusleafClient client;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public void bothFormView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("Impression External Id: <input name='impressionExtId' size='40' />");
        writer.println("<input type='submit' value='Lookup'/>");
        writer.println("<br/>");

        List<String> nodeNameList = client.getNodeNameList();
        writer.println("Aerospike Nodes: " + nodeNameList);
        writer.println("<br/>");

        HashMap<String, String> nodeInfoMap = CitrusleafInfo.get(this.hostname, this.port);
        writer.println("<strong>Node: " + nodeInfoMap.remove("node") + "</strong>");
        writer.println("<br/>");
        String statsString = nodeInfoMap.remove("statistics");
        ArrayList<String> keyList = new ArrayList<String>(nodeInfoMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            writer.println(key + ": " + nodeInfoMap.get(key));
            writer.println("<br/>");
        }

        if (statsString != null) {
            writer.println("Node Statistics:<br/>");
            String[] split = statsString.split(";");
            Arrays.sort(split);
            for (String string : split) {
                writer.println(string);
                writer.println("<br/>");
            }
        }
        writer.println("<br/>");

        writer.println("<strong>Adfonic namespace</strong>");
        writer.println("<br/>");
        String namespaceString = CitrusleafInfo.get(this.hostname, this.port, "namespace/Adfonic");
        String[] split = namespaceString.split(";");
        Arrays.sort(split);
        for (String string : split) {
            writer.println(string);
            writer.println("<br/>");
        }
        /*
        String namespace = "Adfonic";
        String set = "prdel";
        String bin = "";
        ClWriteOptions wops = new ClWriteOptions();
        wops.expiration = 20;
        //client.set(namespace, set, "key1", bin, "value4", null, wops);

        ClResult result = client.get(namespace, set, "key1", bin, null);
        System.out.println("Get result: " + result.result + ", expiration: " + result.expiration + ", generation: " + result.generation);

        Map<String, ClResultCode> scanAllNodes = client.scanAllNodes(namespace, set, new ScanCallback() {

            @Override
            public void scanCallback(String namespace, String set, byte[] digest, Map<String, Object> bins, int generation, int expirationDate, Object userData) {
                System.out.println(namespace);
                System.out.println(set);
                System.out.println(digest);
                System.out.println(bins);
                System.out.println(generation);
                System.out.println(expirationDate);
                System.out.println(userData);
            }
        }, null);

        System.out.println(scanAllNodes);
        */
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void formPost(@RequestParam("impressionExtId") String impressionExtId, HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(URL_CONTEXT + "/" + impressionExtId);
    }

    @RequestMapping(value = "/{impressionExtId}", method = RequestMethod.GET, produces = "application/json")
    public void get(@PathVariable("impressionExtId") String impressionExtId, HttpServletResponse httpResponse) throws IOException {
        Map<String, Object> wrapper = new HashMap<String, Object>();
        Impression impression = impCache.getImpression(impressionExtId);
        if (impression != null) {
            wrapper.put("impression", impression);
        }
        BidDetails bidDetails = rtbCache.getBidDetails(impressionExtId);
        if (bidDetails != null) {
            wrapper.put("rtbcache", bidDetails);
        }
        DebugBidController.debugJsonMapper.writeValue(httpResponse.getWriter(), wrapper);
    }

    @RequestMapping(value = "/impression/{impressionExtId}", method = RequestMethod.GET, produces = "application/json")
    public void getImpression(@PathVariable("impressionExtId") String impressionExtId, HttpServletResponse httpResponse) throws IOException {
        Impression impression = impCache.getImpression(impressionExtId);
        if (impression != null) {
            DebugBidController.debugJsonMapper.writeValue(httpResponse.getWriter(), impression);
        }
        httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Impression not in Aerospike: " + impressionExtId);
    }

    @RequestMapping(value = "/biddetails/{impressionExtId}", method = RequestMethod.GET, produces = "application/json")
    public void getBidDetails(@PathVariable("impressionExtId") String impressionExtId, HttpServletResponse httpResponse) throws IOException {
        BidDetails bidDetails = rtbCache.getBidDetails(impressionExtId);
        if (bidDetails != null) {
            DebugBidController.debugJsonMapper.writeValue(httpResponse.getWriter(), bidDetails);
        }

        httpResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "No RtbBidDetails for impression:" + impressionExtId);
    }
}
