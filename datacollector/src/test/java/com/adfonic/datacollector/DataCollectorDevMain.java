package com.adfonic.datacollector;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adfonic.datacollector.app.DataCollectorApp;
import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.ConfUtils;

/**
 * @author mvanek
 * 
 * -Xms256m -Xmx512m
 * -Dadfonic.config.home=/Devel/byyd/repo-master/byyd-tech/conf/files/local
 * -Dadfonic.cache.home=/Devel/byyd/adfonic.cache.home
 *
 */
public class DataCollectorDevMain {

    /**
     * Processing AdEvents bids/wins/impresions/clicks/installs...
     */
    public static void main(String[] args) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            ConfUtils.checkAppProperties("datacollector");
            ConfUtils.checkSysPropDirectory(ConfUtils.CACHE_DIR_PROPERTY, ConfUtils.CACHE_DIR_DEFAULT);

            ActiveMqUtil.ensureLocalActiveMq();
            DataCollectorApp.main(args); // Spring Boot App

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
