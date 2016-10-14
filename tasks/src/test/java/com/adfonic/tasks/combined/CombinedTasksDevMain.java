package com.adfonic.tasks.combined;

import org.subethamail.wiser.Wiser;

import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.ConfUtils;

/**
 * @author mvanek
 * 
 * -Xms1g -Xmx2g -Dcom.sun.management.jmxremote
 * -Dadfonic.config.home=/Devel/byyd/repo-clean/byyd-tech/conf/files/local
 */
public class CombinedTasksDevMain {

    public static void main(String[] args) {

        try {
            ConfUtils.checkAppProperties("tasks");

            startSmtpServer(2500);
            ActiveMqUtil.ensureLocalActiveMq();
            CombinedTask.main(args);

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * adfonic-tasks.properties
     * 
     * email.outbound.port=2500
     */
    private static Wiser startSmtpServer(int port) {
        Wiser wiser = new Wiser();
        wiser.setPort(port);
        wiser.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                wiser.stop();
            }
        });
        System.out.println("SMTP server started, port: " + wiser.getServer().getPort());
        return wiser;
    }
}
