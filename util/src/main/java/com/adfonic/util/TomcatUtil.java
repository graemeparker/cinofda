package com.adfonic.util;

import java.io.File;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;

/**
 * Some platform apps are deployed into pre-installed Tomcat instance.
 * To start them comfortably in localhost/development environment, this class can be used instead of building war and deploying it into Tomcat
 *
 */
public class TomcatUtil {

    public static void startTomcatByydApp(String appname, int httpPort) {
        try {
            ConfUtils.checkAppProperties(appname);
            Pair<Tomcat, Context> tomcat = startTomcat(appname, httpPort);
            if (tomcat != null) {
                tomcat.first.getServer().await();
            }

        } catch (Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }

    public static Pair<Tomcat, Context> startTomcat(String appname, int httpPort) throws ServletException, LifecycleException {

        System.setProperty(Globals.CATALINA_HOME_PROP, "./target/tomcat");
        if (appname != null) {
            // Some apps use virtual classpath directory
            File file = new File("./target/tomcat/virtualcp/adfonic-" + appname);
            file.mkdirs();
        }

        Tomcat tomcat = new Tomcat();
        Service service = tomcat.getService();

        // Same property as Spring Boot has
        String syspropHttpPort = System.getProperty("server.port");
        if (StringUtils.isNotBlank(syspropHttpPort)) {
            httpPort = Integer.parseInt(syspropHttpPort);
        }

        Connector defaultConnector = new Connector(Http11NioProtocol.class.getName());
        defaultConnector.setPort(httpPort);
        defaultConnector.setEnableLookups(false);
        defaultConnector.setProperty("connectionTimeout", "500");
        service.addConnector(defaultConnector);

        tomcat.setConnector(defaultConnector);
        tomcat.setPort(httpPort);

        String webAppBaseDir = new File("src/main/webapp").getAbsolutePath();
        System.out.println("Configuring Webapp with basedir: " + webAppBaseDir);
        StandardContext context = (StandardContext) tomcat.addWebapp("", webAppBaseDir);
        context.setFailCtxIfServletStartFails(true); // Without this Tomcat does not mark context
        tomcat.start();

        if (context.getState() != LifecycleState.STARTED) {
            // Failed webapp start -> Stop Tomcat and JVM
            System.out.println("Shutdown because Webapp is " + context.getState());
            tomcat.stop();
            System.exit(-1);
            return null;
        }

        System.out.println("Tomcat started " + defaultConnector);
        return Pair.of(tomcat, context);
    }

}
