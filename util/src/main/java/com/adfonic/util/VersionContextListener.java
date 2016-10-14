package com.adfonic.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This Servlet Context Listener put the following attributes from the Manifext
 * on the Application scope for the page to render.
 *
 * <ul>
 * <li>VersionContextListener.VERSION</li>
 * <li>VersionContextListener.BUILD</li>
 * <li>VersionContextListener.MANIFEST</li>
 * <li>VersionContextListener.REVISION</li>
 * </ul>
 *
 * To configure this Lister copy the following and place in the web.xml
 * <listener>
 * <listener-class>com.adfonic.util.VersionContextListener</listener-class>
 * </listener>
 *
 * @author Antony Sohal
 * @version 1.7.0
 */
public final class VersionContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(VersionContextListener.class.getName());

    public static final String VERSION = "VersionContextListener_VERSION";
    public static final String BUILD = "VersionContextListener_BUILD";
    public static final String MANIFEST = "VersionContextListener_MANIFEST";
    public static final String REVISION = "VersionContextListener_REVISION";

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
            LOG.warning("Unable to read Web Application Manifest file");
            return;
        }

        Attributes attributes = manifest.getMainAttributes();

        application.setAttribute(VERSION, attributes.getValue(Name.IMPLEMENTATION_VERSION));
        application.setAttribute(REVISION, attributes.getValue("Revision"));
        application.setAttribute(BUILD, attributes.getValue("Build"));
        application.setAttribute(MANIFEST, attributes);
        return;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext application = sce.getServletContext();
        application.removeAttribute(VERSION);
        application.removeAttribute(BUILD);
        application.removeAttribute(REVISION);
        application.removeAttribute(MANIFEST);
        return;
    }

}
