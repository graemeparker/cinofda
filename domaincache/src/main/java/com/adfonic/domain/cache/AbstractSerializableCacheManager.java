package com.adfonic.domain.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;

import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCacheImpl;
import com.adfonic.util.SerializationUtils;

/** Base class for singleton access to a SerializableCache */
@ManagedResource
public abstract class AbstractSerializableCacheManager<T extends SerializableCache> {

    protected final transient Logger LOG = Logger.getLogger(getClass().getName());

    // This is used to generate batch ids...package for unit test access
    static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");

    protected final Class<T> cacheClass;
    protected final String shortCacheClassName;
    protected final File rootDir;
    protected final String label;
    protected final boolean useMemory;
    protected final Pattern cacheFileNamePattern;

    // We use AtomicReference to provide access to the cache
    // so that as it gets reloaded, the reloaded cache can be
    // swapped in and out in a thread-safe manner.
    protected final AtomicReference<T> cacheRef = new AtomicReference<T>();

    protected volatile File loadedCacheFile;
    protected volatile long lastReloadTime;
    protected volatile Date lastCheckAt;
    protected volatile Date lastPopulationStartedAt;

    private Pattern cacheFileNameGZPattern;

    protected AbstractSerializableCacheManager(Class<T> cacheClass, File rootDir, String label, boolean useMemory) {
        this.cacheClass = cacheClass;
        this.rootDir = rootDir;
        this.label = label;
        this.useMemory = useMemory;

        shortCacheClassName = getShortCacheClassName(cacheClass);

        // Construct a pattern we'll use to recognize our cache files.
        // We optionally allow "Rtb" at the beginning and "-cluster" at the end,
        // so that RTB versions of this cache will be accepted even though
        // they have slightly different batch ids. For example, these are both
        // acceptable batch ids, assuming "AdserverDomainCache" is the value of
        // shortCacheClassName:
        //
        // AdserverDomainCache-mobclix-20110803193652.dat
        // RtbAdserverDomainCache-nexage-20110803193652.dat
        //
        cacheFileNamePattern = Pattern.compile("^(Rtb)?" + shortCacheClassName + "-" + label.replaceAll("\\.", "\\\\.") + "-\\d{14}\\.dat$");
        cacheFileNameGZPattern = Pattern.compile("^(Rtb)?" + shortCacheClassName + "-" + label.replaceAll("\\.", "\\\\.") + "-\\d{14}\\.dat\\.gz$");
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("For " + shortCacheClassName + " (" + label + "), cacheFileNamePattern=" + cacheFileNamePattern);
        }
    }

    @PostConstruct
    public void initialize() throws java.io.IOException, java.lang.ClassNotFoundException {
        if (!rootDir.exists()) {
            throw new FileNotFoundException(rootDir.getCanonicalPath());
        }

        // Pre-load the domain cache
        reloadCache(discoverMostRecentCache());
    }

    @ManagedAttribute(currencyTimeLimit = 3)
    public long getTimeSinceLastReload() {
        return (System.currentTimeMillis() - lastReloadTime) / 1000L;
    }

    protected void reloadCache(String batchId) throws java.io.IOException, java.lang.ClassNotFoundException {
        File cacheFile = new File(rootDir, batchId + ".dat");
        if (!cacheFile.exists()) {
            LOG.severe("Cache file does not exist: " + cacheFile.getAbsolutePath());
            return;
        }
        reloadCache(cacheFile);
    }

    protected T reloadCache(File serializedFile) throws java.io.IOException, java.lang.ClassNotFoundException {
        File loadFile = serializedFile;
        synchronized (this) {
            LOG.info("Reloading " + shortCacheClassName + " (" + label + ")");
            if (serializedFile.getName().endsWith(".gz")) {
                loadFile = decompress(serializedFile);
            }
            Date deserStartedAt = new Date();
            T cache = SerializationUtils.deserialize(cacheClass, loadFile, useMemory);

            if (cache instanceof AdserverDomainCache) {
                AdserverDomainCacheImpl cacheImpl = ((AdserverDomainCacheImpl) cache);
                cacheImpl.setDeserializationStartedAt(deserStartedAt);
                cacheImpl.afterDeserialize();
            }

            if (cache instanceof DomainCache) {
                DomainCacheImpl cacheImpl = ((DomainCacheImpl) cache);
                cacheImpl.setDeserializationStartedAt(deserStartedAt);
                cacheImpl.afterDeserialize();
            }

            cacheRef.set(cache);
            lastReloadTime = System.currentTimeMillis();
            loadedCacheFile = serializedFile;
            cache.logCounts("", LOG, Level.INFO);
            return cache;
        }
    }

    @ManagedAttribute
    public File getLoadedCacheFile() {
        return loadedCacheFile;
    }

    @ManagedAttribute
    public Date getLastReloadAt() {
        return new Date(lastReloadTime);
    }

    @ManagedAttribute
    public Date getLastCheckAt() {
        return lastCheckAt;
    }

    @ManagedAttribute
    public Date getPopulationStartedAt() {
        return lastPopulationStartedAt;
    }

    private File decompress(File source) {
        File retval = null;
        FileOutputStream fos = null;
        LOG.info("Decompressing file: " + source);
        try {
            if (source != null) {
                String outName = source.getAbsoluteFile().toString().replaceAll("\\.gz", "");
                fos = new FileOutputStream(outName);
                GZIPInputStream gz = new GZIPInputStream(new FileInputStream(source));
                IOUtils.copyLarge(gz, fos);
                fos.close();
                retval = new File(outName);
                source.delete();
                new File(outName + ".md5").delete();
            }
        } catch (Throwable t) {
            LOG.warning("Unable to uncompress file: " + t);
        }

        return retval;
    }

    public T getCache() {
        return cacheRef.get();
    }

    /**
     * Discover the most recent cache by scanning for cache files in the
     * rootDir. This method gets used only at context startup. After that,
     * freshly updated cache batchIds are explicitly passed to us via messages
     * on a JMS topic.
     * 
     * @return the most recent cache file
     * @throws Exception
     *             if the most recent cache file can't be determined
     */
    @Deprecated
    protected File discoverMostRecentCache() throws FileNotFoundException {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Attempting to discover most recent cache for " + shortCacheClassName + " (" + label + ")");
        }

        // Any file that matches our cacheFileNamePattern is eligible for discovery
        FileFilter cacheFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Checking against cacheFileNamePattern: " + file.getAbsolutePath());
                }
                if (!cacheFileNamePattern.matcher(file.getName()).matches() && !cacheFileNameGZPattern.matcher(file.getName()).matches()) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("No good: " + file.getAbsolutePath());
                    }
                    return false;
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Accepting: " + file.getAbsolutePath());
                }
                return true; // Yup, it's eligible
            }
        };

        // Find the most recent cacheFile that satisfies our requirements
        File mostRecentSoFar = null;
        for (File file : rootDir.listFiles(cacheFileFilter)) {
            if (mostRecentSoFar == null || FileUtils.isFileNewer(file, mostRecentSoFar)) {
                mostRecentSoFar = file;
            }
        }

        if (mostRecentSoFar == null) {
            File[] files = new File(rootDir.getAbsolutePath() + "/incoming/").listFiles(cacheFileFilter);
            if (files != null) {
                for (File file : files) {
                    if (mostRecentSoFar == null || FileUtils.isFileNewer(file, mostRecentSoFar)) {
                        mostRecentSoFar = file;
                    }
                }
            }
        }

        // Throw an exception if we didn't find anything
        if (mostRecentSoFar == null) {
            throw new FileNotFoundException("No " + shortCacheClassName + " (" + label + ") caches found in " + rootDir.getAbsolutePath());
        }

        // Return the most recent one we found
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Discovered most recent " + shortCacheClassName + " (" + label + "): " + mostRecentSoFar.getName());
        }
        return mostRecentSoFar;
    }

    /**
     * Every minute, auto-delete old caches
     */
    @Scheduled(fixedRate = 60000)
    public synchronized void autoDeleteOldCaches() {
        // If we haven't set up our initial mostRecentCacheFile yet, don't
        // delete
        // anything. We don't want to yank the rug out from under ourself.
        if (loadedCacheFile == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Skipping " + shortCacheClassName + " (" + label + ") auto-delete, mostRecentCacheFile not set yet");
            }
            return;
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Scanning for old " + shortCacheClassName + " (" + label + ") caches in " + rootDir.getAbsolutePath());
        }

        // Any file that matches our cacheFileNamePattern and is older than
        // our most recent cache file is eligible for deletion.
        FileFilter oldCacheFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return cacheFileNamePattern.matcher(file.getName()).matches() && !file.equals(loadedCacheFile) && FileUtils.isFileOlder(file, loadedCacheFile);
            }
        };

        for (File oldCacheFile : rootDir.listFiles(oldCacheFileFilter)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Deleting old cache: " + oldCacheFile.getAbsolutePath());
            }
            FileUtils.deleteQuietly(oldCacheFile);
        }
    }

    /**
     * JMS topic subscriber handler for reload events. This method gets passed
     * the "batchId" of the recently serialized cache. The batchId corresponds
     * to a file under rootDir in which the new cache lives.
     */
    public void onCacheReserialized(String batchId) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(shortCacheClassName + " (" + label + ") reserialized, batchId=" + batchId);
        }

        try {
            reloadCache(batchId);
        } catch (Exception e) {
            // We can tolerate this, just log it
            LOG.log(Level.SEVERE, "Failed to reloadCache " + batchId, e);
        }
    }

    /**
     * Generate a batchId. This method is used by domain.cacher. Produces a
     * string like: shortCacheClassName-label-yyyyMMddHHmmss i.e.:
     * AdserverDomainCache-mobclix-20110302095521
     * RtbAdserverDomainCache-dc1-20110302095521
     * 
     * @param clazz
     *            the domain cache class
     * @param label
     *            the label to include in the batch id
     * @param dateGenerated
     *            the timestamp at which the cache was generated
     */
    public static final <T extends SerializableCache> String generateBatchId(Class<T> clazz, String label, Date dateGenerated) {
        return getShortCacheClassName(clazz) + "-" + label + "-" + TIMESTAMP_FORMAT.format(dateGenerated);
    }

    public static <T extends SerializableCache> String getShortCacheClassName(Class<T> clazz) {
        // Grab just the short class name and strip "Impl" off the end
        return ClassUtils.getShortClassName(clazz).replaceAll("Impl$", "");
    }

    //	@Scheduled(fixedDelay = 10000)
    public void verifyUpdate() {
        LOG.fine("Discovering cache base: " + shortCacheClassName + "/" + label);
        try {
            File mostRecent = discoverMostRecentCache();
            if (mostRecent != null && (loadedCacheFile == null || !mostRecent.getName().equals(loadedCacheFile.getName()))) {
                reloadCache(mostRecent);
            }
        } catch (Throwable t) {
            LOG.warning("Unable to discover cache: " + t);
        }
    }
}
