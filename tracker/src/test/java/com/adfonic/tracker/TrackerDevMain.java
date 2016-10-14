package com.adfonic.tracker;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.TomcatUtil;

public class TrackerDevMain {

    public static void main(String[] args) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            ActiveMqUtil.ensureLocalActiveMq();
            TomcatUtil.startTomcatByydApp("tracker", 4444);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
