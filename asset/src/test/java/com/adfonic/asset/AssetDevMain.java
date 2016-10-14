package com.adfonic.asset;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adfonic.util.TomcatUtil;

public class AssetDevMain {

    /**
     * Asset uses Java util logging in production but for development we need something more flexible (slf4j + logback)
     */
    public static void main(String[] args) {

        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            TomcatUtil.startTomcatByydApp("asset", 5555);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
