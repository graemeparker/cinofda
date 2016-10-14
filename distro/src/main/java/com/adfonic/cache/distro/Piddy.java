package com.adfonic.cache.distro;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class Piddy {
    
    private static final transient Logger LOG = Logger.getLogger(Piddy.class.getName());
    
    private Proceo proceo;
    
    public Piddy() {
        proceo = new Proceo();
    }
    
    public static void main(String... args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        LOG.info("Piddy called directly from main. You shouldn't be doing this! Use Distro!");
    }
    
    protected String generatePid() throws IOException {
        return proceo.process("/bin/bash", "-c","echo $PPID");
    }
}
