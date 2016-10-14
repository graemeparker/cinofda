package com.adfonic.tasks;

import org.junit.Test;

public class TestLogging {

    @Test
    public void test() {
        java.util.logging.Logger jul = java.util.logging.Logger.getLogger("TestLogging");
        org.slf4j.Logger slf4j = org.slf4j.LoggerFactory.getLogger(TestLogging.class);
        org.apache.commons.logging.Log cul = org.apache.commons.logging.LogFactory.getLog(TestLogging.class);
        //org.apache.log4j.Logger log4j = org.apache.log4j.LogManager.getLogger(TestLogging.class);

        jul.info("test jul");
        slf4j.info("test slf4j");
        cul.info("test cul");
        //log4j.info("test log4j");

    }

}
