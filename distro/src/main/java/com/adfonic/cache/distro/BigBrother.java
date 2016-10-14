package com.adfonic.cache.distro;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;

/**
 * @author anuj
 *
 */
public class BigBrother {

    private static final transient Logger LOG = Logger.getLogger(BigBrother.class.getName());
    
    private WatchService watcher;
    private Path dir;
    
    private Fileo fileo;
    private File sourceCacheFile;
    
    @Autowired
    private AdserverDomainCacheManager adserverDomainCacheManager;
    @Autowired
    private DomainCacheManager domainCacheManager;
    @Value("${aws.cache.distro.use}")
    private boolean shouldUseNewDistro;
    
    public BigBrother(String pathToWatch) throws IOException {
        LOG.info("Watching for cache files");
        this.watcher = FileSystems.getDefault().newWatchService();
        fileo = new Fileo();
        this.dir = Paths.get(pathToWatch);
        dir.register(watcher, ENTRY_CREATE);
    }
    
    public static void main(String[] args) throws IOException {
        LOG.info("BigBrother called directly from main. You shouldn't be doing this! Create a SpringBean!");
    }
    
    public void init() throws IOException {
        if(shouldUseNewDistro) {
            processEvents();
            LOG.info("Watching for cache file update using new AWS cache distribution in: " + dir.toFile().getAbsolutePath());
        } else {
            LOG.info("We are not using new cache distribution. Will continue with existing cache distribution.");
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private void processEvents() {
        LOG.info("Processing events");
        for (;;) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();
                if (kind == OVERFLOW) {
                    continue;
                }
                WatchEvent<Path> ev = cast(event);
                Path filename = ev.context();
                String extension = FilenameUtils.getExtension(filename.getFileName().toString());
                if("gz".equals(extension)) {
                    manageCompressedFile(filename);
                } 
            }
            
            //reset the key - we need to do this so that we can continue to receive events.
            //invalid key will result in breaking 
            boolean valid = key.reset();
            if (!valid) {
                    break;
            }
        }
    }

    private void manageCompressedFile(Path filename) {
        String compressedCacheFile = dir.toFile().getAbsolutePath() + "/"+filename.getFileName().toString();
        LOG.info("Compressed cache file found. Decompressing " + compressedCacheFile);
        sourceCacheFile = fileo.decompress(compressedCacheFile);
        if(sourceCacheFile != null) {
            LOG.info("Checking....: " + sourceCacheFile.getAbsoluteFile().getName());
            //call onCacheReSerialized in adserver
            if(sourceCacheFile.getAbsoluteFile().getName().matches(Fileo.DOMAIN_CACHE_FILENAME_REGEX)) {
                LOG.info("DomainCache file : " + sourceCacheFile.getAbsolutePath() + " is ready for use!");
                domainCacheManager.onCacheReserialized(FilenameUtils.getBaseName(sourceCacheFile.getName()));
                LOG.info("Instructed Adserver to use : "+ FilenameUtils.getBaseName(sourceCacheFile.getName()));
            } else if (sourceCacheFile.getAbsoluteFile().getName().matches(Fileo.ADSERVER_DOMAIN_CACHE_FILENAME_REGEX)) {
                LOG.info("AdserverDomainCache file : " + sourceCacheFile.getAbsolutePath() + " is ready for use!");
                adserverDomainCacheManager.onCacheReserialized(FilenameUtils.getBaseName(sourceCacheFile.getName()));
                LOG.info("Instructed Adserver to use : "+ FilenameUtils.getBaseName(sourceCacheFile.getName()));
            }
            //delete the files
            //cacheFile
            LOG.info("Cleaning up and deleting " + sourceCacheFile.getAbsolutePath() + ".gz");
            fileo.deleteFile(sourceCacheFile.getAbsolutePath()+ ".gz");
            //md5
            LOG.info("Cleaning up and deleting " + sourceCacheFile.getAbsolutePath() + ".md5");
            fileo.deleteFile(sourceCacheFile.getAbsolutePath() + ".md5");
            //done
            LOG.info("Finished cleaningup. Going back to watching: " + dir.toFile().getAbsolutePath());
        }
    }
}
