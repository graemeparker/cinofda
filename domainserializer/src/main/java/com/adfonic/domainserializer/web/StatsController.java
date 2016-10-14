package com.adfonic.domainserializer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfonic.domainserializer.CacheBuildStats;
import com.adfonic.domainserializer.DomainSerializerS3;
import com.adfonic.domainserializer.DsCacheManager;

@Controller
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private DomainSerializerS3 ds;

    @Autowired
    private DsCacheManager cacheManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void index(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("text/html");
        writer.println("<html>");
        //printHeader(writer);
        printStatistics(writer, ds.getStatistics());
        writer.println("</html>");
    }

    private static void printStatistics(PrintWriter writer, List<CacheBuildStats> statistics) {
        writer.println("Number of Cache Statistics: " + statistics.size());
        writer.println("<br/>");
        for (CacheBuildStats stats : statistics) {
            writer.println("<hr/>");
            writer.println("Cache Id: " + stats.getLabel());
            writer.println("<br/>");
            writer.println("DbSelectionStartedAt: " + format(stats.getDbSelectionStartedAt()));
            writer.println("<br/>");
            if (stats.getEligibilityStartedAt() != null) {
                // Not avaliable for domain/datacollector cache
                writer.println("DbEligibilityStartedAt: " + format(stats.getEligibilityStartedAt()));
                writer.println("<br/>");
            }
            writer.println("SerializationStartedAt: " + format(stats.getSerializationStartedAt()));
            writer.println("<br/>");
            writer.println("DistributionStartedAt: " + format(stats.getDistributionStartedAt()));
            writer.println("<br/>");
            writer.println("DistributionCompletedAt: " + format(stats.getDistributionCompletedAt()));
            writer.println("<br/>");

            Exception exception = stats.getException();
            if (exception != null) {
                writer.println(exception);
                writer.println("<pre>");
                exception.printStackTrace(writer);
                writer.println("</pre>");
            }

            String contentStatsString = stats.getContectStatsString();
            if (contentStatsString != null) {
                writer.println("Content Stats:");
                writer.println("<pre>");
                writer.println(contentStatsString);
                writer.println("</pre>");
            }
            String stopWatchString = stats.getStopWatchString();
            if (stopWatchString != null) {
                writer.println("StopWatch Stats:");
                writer.println("<pre>");
                writer.println(stopWatchString);
                writer.println("</pre>");
            }
            writer.flush();
        }
    }

    public static String format(Date date) {
        if (date != null) {
            return IndexController.FDF.format(date);
        } else {
            return null;
        }
    }
}
