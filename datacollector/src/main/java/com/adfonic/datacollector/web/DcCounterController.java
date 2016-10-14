package com.adfonic.datacollector.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfonic.util.stats.CounterManager;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Copy of AsCouterController from AdServer 
 */
@Controller
@RequestMapping(DcCounterController.URL_CONTEXT)
public class DcCounterController {

    public static final String HTML_OPEN = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head><body>";
    public static final String HTML_CLOSE = "</body></html>";

    public static final String URL_CONTEXT = "/counters";

    @Autowired
    private CounterManager counterManager;

    private final ObjectMapper debugJsonMapper = new ObjectMapper();

    @RequestMapping(path = "{filter}", method = RequestMethod.GET, produces = "text/html")
    public void debugCounter(@PathVariable("filter") String filter, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        printCounters(httpResponse, filter, MediaType.APPLICATION_JSON_UTF8);
    }

    @RequestMapping(path = "reset", method = RequestMethod.GET)
    public void reset(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        Map<String, AtomicLong> counters = counterManager.getCounterValues();
        for (Entry<String, AtomicLong> entry : counters.entrySet()) {
            entry.getValue().set(0);
        }
        httpResponse.sendRedirect(URL_CONTEXT);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public void debugCounters(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        printCounters(httpResponse, null, MediaType.APPLICATION_JSON_UTF8);
    }

    private void printCounters(HttpServletResponse httpResponse, String filter, MediaType mediaType) throws IOException {
        if (mediaType == MediaType.APPLICATION_JSON_UTF8) {
            PrintWriter writer = httpResponse.getWriter();
            List<String> counterNames = new ArrayList<String>(counterManager.getCounterNames());
            Collections.sort(counterNames); // sort counters by name
            TreeMap<String, Long> counts = new TreeMap<String, Long>();
            for (String counterName : counterNames) {
                if (filter == null || counterName.contains(filter)) {
                    counts.put(counterName, counterManager.getCount(counterName));
                }
            }
            debugJsonMapper.writeValue(writer, counts);
        } else {
            printHtml(httpResponse, filter);
        }
    }

    private void printHtml(HttpServletResponse httpResponse, String filter) throws IOException {
        httpResponse.setContentType(MediaType.TEXT_HTML_VALUE);
        httpResponse.setCharacterEncoding("utf-8");
        PrintWriter writer = httpResponse.getWriter();
        writer.println(HTML_OPEN);
        writer.println("<table>");
        List<String> counterNames = new ArrayList<String>(counterManager.getCounterNames());
        Collections.sort(counterNames); // sort counters by name
        for (String counterName : counterNames) {
            if (filter == null || counterName.contains(filter)) {
                writer.print("<tr><td>");
                writer.print(counterName);
                writer.println("</td><td>");
                writer.print(counterManager.getCount(counterName));
                writer.println("</td><tr>");
            }
        }
        writer.println("</table>");
        writer.println(HTML_CLOSE);
    }
}
