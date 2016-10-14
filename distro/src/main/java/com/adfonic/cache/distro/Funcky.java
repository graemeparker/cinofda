package com.adfonic.cache.distro;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Funcky {

    private static final transient Logger LOG = Logger.getLogger(Funcky.class.getName());
    private Proceo proceo;
    
    private static final String INCOMING_DIR_PATH = "/mnt/data/cache/incoming";
    private static final String AMPERSAND = " && ";
    
    public Funcky() {
        proceo = new Proceo();
    }
    
    public static void main(String[] args) throws UnknownHostException {
        LOG.info("Funcky called directly from main. You shouldn't be doing this! Use Distro!");
    }
    
    protected void func(String minion, String command) {
        String[] func = {"func","-t 60",minion,"call","--json","command","run",command};
        try {
            String response = proceo.process(func);
            LOG.info(response);
        } catch (IOException ioe) {
            LOG.error("Problem func-ing", ioe);
        }
    }
    
    public String getMegaFunc(String host, String cacheFileName) {
        LOG.info("Funcing with compressed file of " + cacheFileName);
        StringBuilder megaFunc = new StringBuilder();
        megaFunc.append(makeDirectory())
                .append(AMPERSAND)
                .append(rsync(host,cacheFileName + ".gz"))
                .append(AMPERSAND)
                .append(rsync(host,cacheFileName+".md5"));
        LOG.debug("Mega-Func-ing with " + megaFunc.toString());
        return megaFunc.toString();
    }
    
    public String makeDirectory() {
        return "mkdir -p "+ INCOMING_DIR_PATH;
    }
    
    private String rsync(String host, String gzfileName) {
        return "rsync -a "+host+"::cache/"+gzfileName+" "+ INCOMING_DIR_PATH;
    }
}
