package com.adfonic.tasks.combined;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.adfonic.tasks.config.combined.CombinedSpringBootConfig;
import com.adfonic.util.ConfUtils;

/**
 * 
 * Spring-Boot launcher of combined tasks - see CombinedSpringBootConfig
 *
 */
public class CombinedTask {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    private static final transient Logger LOG = LoggerFactory.getLogger(CombinedTask.class.getName());

    public static void main(String[] args) {
        // Instruct Spring Boot to use our legacy externaly located property file
        String configFile = "file:" + System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT) + "/" + "adfonic-tasks.properties";
        System.setProperty("spring.config.location", configFile);

        int exitCode = 0;
        try {
            ConfigurableApplicationContext context = SpringApplication.run(CombinedSpringBootConfig.class, args); // embedded server blocks forever
        } catch (Throwable e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
            Runtime.getRuntime().exit(exitCode);
        }
    }
    /*
    public static void main(String[] args) {
        
        int exitCode = 0;
        try {
            SpringTaskBase.runBean("adfonic-tasks-context.xml",
                                   "adfonic-toolsdb-context.xml",
                                   "adfonic-admreportingdb-context.xml",
                                   "adfonic-optdb-context.xml",
                                   "adfonic-optoutdb-context.xml",
                                   "adfonic-trackerdb-context.xml",
                                   "adfonic-combined-context.xml");
        } catch (Throwable e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
    */
}
