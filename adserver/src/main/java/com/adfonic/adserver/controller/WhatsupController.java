package com.adfonic.adserver.controller;

import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WhatsupController extends AbstractAdServerController {

    @Value("${AdserverDomainCache.label}")
    private String shardName;

    @RequestMapping("/shard")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        PrintStream out = new PrintStream(response.getOutputStream());
        out.print(shardName);
        out.close();
    }
}
