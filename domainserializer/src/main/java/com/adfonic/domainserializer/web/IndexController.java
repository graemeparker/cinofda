package com.adfonic.domainserializer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfonic.domainserializer.DomainSerializerS3;
import com.adfonic.domainserializer.DsCacheManager;
import com.adfonic.domainserializer.DsCluster;
import com.adfonic.domainserializer.DsShard;

@Controller
@RequestMapping
public class IndexController {

    @Autowired
    private DomainSerializerS3 ds;

    @Autowired
    private DsCacheManager cacheManager;

    @Autowired
    @Qualifier("domainSerializerProperties")
    private Properties properties;

    public static final FastDateFormat FDF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSSZ");

    @RequestMapping(value = "properties", method = RequestMethod.GET)
    public void properties(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        properties.store(httpResponse.getWriter(), null);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void index(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("text/html");
        PrintWriter writer = httpResponse.getWriter();
        writer.println("<html>");
        printHeader(writer);
        writer.println("<ul>");
        writer.println("<li><a href='stats'>Cache build stats</a></li>");
        writer.println("<li><a href='eligibility'>Eligibility checker</a></li>");
        writer.println("<li><a href='status'>DomainSerializer status</a></li>");
        writer.println("<li><a href='properties'>DomainSerializer properties</a></li>");
        writer.println("</ul>");
        writer.println("</html>");
    }

    private void printHeader(PrintWriter writer) {

        if (!DomainSerializerS3.omitAdserverCache) {
            writer.println("AdServer Cache period: " + ds.getReloadAdserverPeriodSeconds());
            writer.println("<br/>");
        }
        if (!DomainSerializerS3.omitDomainCache) {
            writer.println("Domain Cache period: " + ds.getReloadDomainPeriodSeconds());
            writer.println("<br/>");
        }
        if (!DomainSerializerS3.omitCollectorCache) {
            writer.println("DataCollector Cache period: " + ds.getReloadCollectorPeriodSeconds());
            writer.println("<br/>");
        }

        List<DsShard> shards = ds.getActiveAdserverShards();
        if (!shards.isEmpty()) {
            writer.println("Shards: " + shards);
            for (DsShard dsShard : shards) {
                buildS3ViewLink(writer, dsShard.getName());
                writer.println("<br/>");
            }
        }

        List<DsCluster> clusters = ds.getActiveDomainClusters();
        if (!clusters.isEmpty()) {
            writer.println("Clusters: " + clusters);
            for (DsCluster dsCluster : clusters) {
                buildS3ViewLink(writer, dsCluster.getName());
                writer.println("<br/>");
            }
        }
    }

    private void buildS3ViewLink(PrintWriter writer, String label) {
        String s3path = cacheManager.getS3BasePath() + label;
        String s3Bucket = cacheManager.getS3Bucket();
        String s3ViewUrl = "https://s3-console-us-standard.console.aws.amazon.com/GetResource/Console.html?#&bucket=" + s3Bucket + "&prefix=" + s3path;
        writer.println("<a href='" + s3ViewUrl + "' target='_blank'>" + s3path + "</a>");
    }

}
