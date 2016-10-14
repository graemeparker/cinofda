package com.adfonic.weve;

import com.adfonic.util.TomcatUtil;

public class WeveDevMain {

    public static void main(String[] args) {
        try {
            TomcatUtil.startTomcatByydApp("weve", 5555);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
