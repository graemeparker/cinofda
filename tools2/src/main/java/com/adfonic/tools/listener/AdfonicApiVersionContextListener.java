package com.adfonic.tools.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdfonicApiVersionContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdfonicApiVersionContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext application = sce.getServletContext();
        InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");

        if (inputStream == null) {
            return;
        }

        Manifest manifest = null;
        try {
            manifest = new Manifest(inputStream);
        } catch (IOException e) {
            LOGGER.warn("Unable to read application Manifest file");
            return;
        }

        Map<String, String> appAttributes = new HashMap<String, String>();
        Attributes attributes = manifest.getMainAttributes();

        appAttributes.put(Name.IMPLEMENTATION_VERSION.toString(), attributes.getValue(Name.IMPLEMENTATION_VERSION));
        appAttributes.put("Revision", attributes.getValue("Revision"));
        appAttributes.put("Build", attributes.getValue("Build"));
        appAttributes.put("Built-By", attributes.getValue("Built-By"));

        application.setAttribute("appAttributes", appAttributes);
        return;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext application = sce.getServletContext();
        application.removeAttribute("appAttributes");
        return;
    }

}
