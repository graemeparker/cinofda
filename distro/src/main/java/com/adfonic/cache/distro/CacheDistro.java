package com.adfonic.cache.distro;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * process func responses for failures
 * @author anuj
 *
 */
public class CacheDistro {

    static String baseName = CacheDistro.class.getName();
    private static final transient Logger LOG = Logger.getLogger(CacheDistro.class.getName());
    
    private Fileo file;
    private Funcky func;
    
    private String batchId;
    private String cluster;
    
    public CacheDistro(String batchId, String cluster) {
        file = new Fileo();
        func = new Funcky();
        this.batchId = batchId;
        this.cluster = cluster;
    }
    
    public void start() throws UnknownHostException    {
        if(!StringUtils.isBlank(file.getVersion(batchId))) {
            File compressedCacheFile = file.compress(batchId);
            if(compressedCacheFile != null) {
                //func the compressed + md5 file
                func.func(cluster, func.getMegaFunc(InetAddress.getLocalHost().getHostName(),FilenameUtils.removeExtension(compressedCacheFile.getName())));
                
                // this should be processed only if func doesn't return any error
                file.setHealth(file.getVersion(batchId), batchId);
                
                // this should happen only if delete is set to true
                //delete cache file
                LOG.info("Deleting cache files");
                String cacheFileName = compressedCacheFile.getParent() + "/" + FilenameUtils.getBaseName(compressedCacheFile.getPath());
                LOG.debug("Deleting: "+ cacheFileName);

                //delete compressed file
                LOG.debug("Deleting: "+ compressedCacheFile.getPath());
                file.deleteFile(compressedCacheFile.getPath());
                //delete compressed file
                LOG.debug("Deleting: "+ cacheFileName+Fileo.MD5);
                file.deleteFile(cacheFileName+Fileo.MD5);
            }
        } else {
            //warn
            LOG.error("Unrecognized batchId format: " + batchId);
        }
    }
    
    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        // DO NOTHING
    }
}
