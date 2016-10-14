package com.adfonic.adserver.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.quova.QuovaClient;
import com.quova.data._1.Ipinfo;

@Controller
public class QuovaCheckController {
    private static final String DEFAULT_IP = "98.67.157.168";

    private final QuovaClient quovaClient;

    @Autowired
    public QuovaCheckController(QuovaClient quovaClient) {
        this.quovaClient = quovaClient;
    }

    @RequestMapping("/internal/quovacheck")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false, defaultValue = DEFAULT_IP) String ip)
            throws javax.servlet.ServletException, java.io.IOException {
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        response.setContentType("text/plain");

        try {
            final long startTime = System.currentTimeMillis();
            final Ipinfo ipInfo = quovaClient.getIpinfo(ip);
            final long elapsed = System.currentTimeMillis() - startTime;
            if (ipInfo == null) {
                response.getWriter().append("ERROR: No response from Quova");
            } else {
                if (ipInfo.getLocation() == null) {
                    response.getWriter().append("ERROR: no location");
                } else if (ipInfo.getLocation().getCountryData() == null) {
                    response.getWriter().append("ERROR: no country data");
                } else {
                    final String country = ipInfo.getLocation().getCountryData().getCountryCode();
                    if (!"us".equals(country)) {
                        response.getWriter().append("ERROR: expected country=us, got " + country);
                    } else {
                        response.getWriter().append("Quova OK, response time: " + elapsed + "ms");
                    }
                }
            }
        } catch (Exception e) {
            response.getWriter().append("ERROR: " + ExceptionUtils.getFullStackTrace(e));
        }
    }
}