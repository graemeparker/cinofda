package com.adfonic.webservices;

import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.TomcatUtil;

public class ApiDevMain {

    public static void main(String[] args) {
        try {
            ActiveMqUtil.ensureLocalActiveMq();
            TomcatUtil.startTomcatByydApp("api", 7777);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
