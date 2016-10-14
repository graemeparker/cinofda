package com.adfonic.cache.distro;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class Testy {
    private static final transient Logger LOG = Logger.getLogger(Testy.class.getName());
    static String baseName = CacheDistro.class.getName();
    
    private Testy(){
    }

    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        Fileo fileo = new Fileo();
        File sourceCacheFile = fileo.decompress("/mnt/data/cache/incoming/AdserverDomainCache-shard-ch1-shared-20140212111910.dat.gz");
        if(fileo.verify(sourceCacheFile, "/mnt/data/cache/incoming/AdserverDomainCache-shard-ch1-shared-20140212111910.dat.md5")) {
            LOG.info("DONE");
        } else {
            LOG.info("NOT DONE");
        }
    }
}
