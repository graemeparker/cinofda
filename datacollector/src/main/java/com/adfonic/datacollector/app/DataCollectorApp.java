package com.adfonic.datacollector.app;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.adfonic.util.ConfUtils;

public final class DataCollectorApp {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) {
        // Do not even consider this...
        System.setProperty("spring.datasource.initialize", "false");
        System.setProperty("endpoints.shutdown.enabled", "true");

        // Instruct Spring Boot to use our legacy externaly located property file
        String configFile = "file:" + System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT) + "/" + "adfonic-datacollector.properties";
        System.setProperty("spring.config.location", configFile);

        ConfigurableApplicationContext context = SpringApplication.run(DcSpringBootConfig.class, args); // embedded server blocks forever
    }

}
