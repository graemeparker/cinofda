package com.adfonic.domainserializer;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.ConfUtils;

public class DomainSerializerDevMain {

    public static void main(String[] args) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            ConfUtils.checkAppProperties("domainserializer");
            ConfUtils.checkSysPropDirectory("DomainSerializer.cacheRootDir", ConfUtils.CACHE_DIR_DEFAULT);
            ActiveMqUtil.ensureLocalActiveMq();

            DomainSerializerS3.main(args); // Simply execute main

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
