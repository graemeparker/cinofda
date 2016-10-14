package com.adfonic.tasks.combined;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.util.DaemonThreadFactory;
import com.adfonic.util.Pair;

/**
 * Initial list of blacklisted domain has more than 140k entries on it.
 * Some blacklist entries are domains (manga2u.me) some are subdomains (ecrater.com.au)
 * 
 * @author mvanek
 *
 */
public class BlacklistPublicationsTask {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistPublicationsTask.class.getName());

    private static final String SQL_PUBLICATION_TYPE = "SELECT ID FROM PUBLICATION_TYPE WHERE MEDIUM='" + Medium.SITE + "'";
    private static final String SQL_PUBLICATION_LIST = "SELECT ID, URL_STRING FROM PUBLICATION WHERE STATUS='" + Publication.Status.ACTIVE + "' AND PUBLICATION_TYPE_ID IN ("
            + SQL_PUBLICATION_TYPE + ") AND URL_STRING IS NOT NULL";

    private static final String SQL_PUBLICATION_UPDATE = "UPDATE PUBLICATION SET STATUS='" + Publication.Status.REJECTED + "' WHERE ID=?";

    private static final int DEFAULT_THREADS = 0; // 0 - derive from number of processors
    private static final int DEFAULT_BATCH = 1000;

    private final int threads;
    private final int batch;

    private final JdbcTemplate jdbc;
    private final List<String> blacklistedDomains;

    public BlacklistPublicationsTask(JdbcTemplate jdbc, File blacklistFile) throws IOException {
        this(jdbc, loadBlacklist(blacklistFile));
    }

    public BlacklistPublicationsTask(JdbcTemplate jdbc, String classpathResource) throws IOException {
        this(jdbc, loadBlacklist(classpathResource));
    }

    public BlacklistPublicationsTask(JdbcTemplate jdbc, List<String> blacklistedDomains) {
        this(jdbc, blacklistedDomains, DEFAULT_THREADS, DEFAULT_BATCH);
    }

    public BlacklistPublicationsTask(JdbcTemplate jdbc, List<String> blacklistedDomains, int threads, int batch) {
        Objects.requireNonNull(jdbc);
        this.jdbc = jdbc;
        Objects.requireNonNull(blacklistedDomains);
        if (blacklistedDomains.size() == 0) {
            throw new IllegalArgumentException("Empty blacklist");
        }
        this.blacklistedDomains = blacklistedDomains;

        if (threads < 1) {
            // leave one available core unoccupied
            threads = Runtime.getRuntime().availableProcessors() - 1;
            if (threads < 1) {
                logger.warn("Only one core available. Sequential run enforced");
                threads = 1;
            }
        }
        this.threads = threads;
        if (threads > 1 && batch < 100) {
            throw new IllegalArgumentException("Batch size " + batch + " is too small for paralel execution");
        }
        this.batch = batch;
        logger.info("Initialized with Blacklist: " + blacklistedDomains.size() + " Threads: " + threads + " Batch: " + batch);
    }

    public void execute() {
        if (threads > 1) {
            executeParalel(threads, batch);
        } else {
            logger.info("started");
            int[] counters = new int[2];
            jdbc.query(SQL_PUBLICATION_LIST, (ResultSet rs) -> {
                ++counters[0];
                boolean rejected = doPublication(rs.getLong(1), rs.getString(2));
                if (rejected) {
                    ++counters[1];
                }
            });
            logger.info("Completed. publications: " + counters[0] + " rejections:" + counters[1]);
        }
    }

    private void executeParalel(int threadCount, int batchSize) {
        logger.info("Started. Threads: " + threadCount + " Batch: " + batchSize);
        DaemonThreadFactory threadFactory = new DaemonThreadFactory("blacklist-");
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount, threadFactory);
        final List<Pair<Long, String>> listToSubmit = new LinkedList<Pair<Long, String>>();
        final List<Future<Integer>> futures = new LinkedList<Future<Integer>>();
        int[] counters = new int[2];
        jdbc.query(SQL_PUBLICATION_LIST, (ResultSet rs) -> {
            ++counters[0];
            listToSubmit.add(Pair.of(rs.getLong(1), rs.getString(2)));
            if (listToSubmit.size() == batchSize) {
                // pass copy of list to submission
                LinkedList<Pair<Long, String>> copyOfList = new LinkedList<Pair<Long, String>>(listToSubmit);
                ++counters[1];
                Future<Integer> future = executorService.submit(() -> {
                    return doBatch(copyOfList);
                });
                futures.add(future);
                listToSubmit.clear();
            }
        });
        // last (not completely full) batch...
        Future<Integer> future = executorService.submit(() -> {
            ++counters[1];
            return doBatch(new LinkedList<Pair<Long, String>>(listToSubmit));
        });
        futures.add(future);
        logger.info("Submitted. publications: " + counters[0] + " batches: " + counters[1]);
        int rejections = waitForCompletion(futures, executorService);
        logger.info("Completed. publications: " + counters[0] + " rejections: " + rejections);
    }

    private int waitForCompletion(List<Future<Integer>> futures, ExecutorService executorService) {
        int rejections = 0;
        for (Future<Integer> future : futures) {
            try {
                rejections += future.get(5, TimeUnit.MINUTES);
            } catch (TimeoutException tx) {
                logger.error("Batch task timeout: " + tx + " Shutting down executor service");
                executorService.shutdown();
            } catch (InterruptedException ix) {
                logger.error("Batch task interrupted", ix);
            } catch (ExecutionException ex) {
                logger.error("Batch task failed", ex);
            }
        }
        return rejections;
    }

    private int doBatch(LinkedList<Pair<Long, String>> list) {
        int rejections = 0;
        logger.debug("doBatch " + list.size());
        for (Pair<Long, String> pair : list) {
            boolean rejected = doPublication(pair.first, pair.second);
            if (rejected) {
                ++rejections;
            }
        }
        return rejections;
    }

    private boolean doPublication(long publicationId, String urlString) {
        boolean blacklisted = isBlacklisted(publicationId, urlString);
        if (blacklisted) {
            jdbc.update(SQL_PUBLICATION_UPDATE, publicationId);
        }
        return blacklisted;
    }

    public boolean isBlacklisted(long publicationId, String urlString) {
        String[] pHostAndDomain;
        try {
            pHostAndDomain = getHostAndDomains(urlString);
        } catch (MalformedURLException x) {
            logger.warn("Unparseable publication " + publicationId + " url " + urlString);
            return false;
        }
        for (String bDomain : blacklistedDomains) {
            for (String pName : pHostAndDomain) {
                if (pName.equals(bDomain)) {
                    logger.debug("Rejection: " + publicationId + " '" + urlString + "' vs '" + bDomain + "'");
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Break down publication url into hostname and all domains and subdomains that exist
     */
    private String[] getHostAndDomains(String urlString) throws MalformedURLException {
        if (!urlString.startsWith("http")) {
            urlString = "http://" + urlString;
        }
        URL url = new URL(urlString);
        String hostName = url.getHost();
        String[] parts = hostName.split("\\.");
        int partCount = parts.length;
        if (partCount > 2) {
            List<String> names = new ArrayList<String>(partCount - 1);
            StringBuilder sb = new StringBuilder();
            for (int i = partCount - 1; i > 0; --i) {
                if (i == partCount - 1) {
                    sb.insert(0, parts[i]); // top level domain
                } else {
                    sb.insert(0, '.'); // domains and subdomains...
                    sb.insert(0, parts[i]);
                    names.add(sb.toString());
                }
            }
            names.add(hostName);
            return names.toArray(new String[names.size()]);
        } else {
            return new String[] { hostName };
        }

    }

    /**
     * There might be some rubbish in input file so filter out too short lines and lines with spaces
     */
    private static List<String> loadBlacklist(File file) throws IOException {
        StreamCompression compression = StreamCompression.getByExtension(file.getName());
        return loadBlacklist(new FileInputStream(file), compression);
    }

    private static List<String> loadBlacklist(String classpathResource) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = BlacklistPublicationsTask.class.getClassLoader();
        }
        InputStream stream = classLoader.getResourceAsStream(classpathResource);
        if (stream == null) {
            throw new IllegalArgumentException("ClassLoader resource not found: " + classpathResource);
        }
        StreamCompression compression = StreamCompression.getByExtension(classpathResource);
        return loadBlacklist(stream, compression);
    }

    public static enum StreamCompression {
        ZIP, GZIP, NONE;

        public static StreamCompression getByExtension(String extension) {
            StreamCompression compression = StreamCompression.NONE;
            if (extension.endsWith(".gzip") || extension.endsWith(".gz")) {
                compression = StreamCompression.GZIP;
            } else if (extension.endsWith(".zip")) {
                compression = StreamCompression.ZIP;
            }
            return compression;
        }
    }

    private static List<String> loadBlacklist(InputStream stream, StreamCompression compression) throws IOException {
        switch (compression) {
        case GZIP:
            stream = new GZIPInputStream(stream);
            break;
        case ZIP:
            ZipInputStream zstream = new ZipInputStream(stream);
            ZipEntry zipEntry = zstream.getNextEntry();
            // Skip any directories and stop on first file ZipEntry
            while (zipEntry != null && zipEntry.isDirectory()) {
                zipEntry = zstream.getNextEntry();
            }
            if (zipEntry == null) {
                throw new IllegalArgumentException("No file ZipEntry found in stream");
            }
            stream = zstream;
            break;
        case NONE:
            break;
        default:
            throw new IllegalArgumentException("Unsupported compression: " + compression);
        }
        List<String> list = new LinkedList<String>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.forName("utf-8")))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 3 && !line.startsWith("#") && !line.contains(" ")) {
                    list.add(line);
                } else {
                    logger.debug("Skipping blacklist entry: " + line);
                }
            }
        }
        return list;
    }
}
