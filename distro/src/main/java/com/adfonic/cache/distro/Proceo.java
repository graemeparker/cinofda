package com.adfonic.cache.distro;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class Proceo {
    private static final transient Logger LOG = Logger.getLogger(Proceo.class.getName());
    
    private static final int BUFFER_SIZE_1500 = 1500;
    private static final int BUFFER_SIZE_500 = 500;

    public static void main(String... args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        LOG.info("Proceo called directly from main. You shouldn't be doing this! Use Distro!");
    }
    
    private  String arrayToString(String[] a) {
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(a[i]);
                result.append(" ");
            }
        }
        return result.toString();
    }
    
    public String process(String... command) throws IOException {
        LOG.info("Executing command ["+arrayToString(command)+"]");
        Process p = new ProcessBuilder().command(command)
                                        .redirectErrorStream(true)
                                        .start();
        // We need to call Process.waitFor to truly detach the background process.
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted");
        }
        
        byte[] bo = new byte[BUFFER_SIZE_1500];
        byte[] eo = new byte[BUFFER_SIZE_500];
        p.getInputStream().read(bo);
        p.getErrorStream().read(eo);
        if(bo.length > 0) {
            return new String(bo);
        } else if (eo.length > 0) {
            LOG.error("Error executing the command [" +  Arrays.toString(command) + "]:" + new String(eo));
        }
        return null;
    }
}
