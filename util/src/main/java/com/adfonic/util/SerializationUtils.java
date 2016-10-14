package com.adfonic.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;

/**
 * Utilities for serializing objects to/from files. This class has the logic for
 * serializing and deserializing caches to files, providing GZIP compression
 * when needed, etc.
 */
public class SerializationUtils {
    
    private static final transient Logger LOG = Logger.getLogger(SerializationUtils.class.getName());

    private SerializationUtils(){        
    }
    
    /**
     * Serialize an object to the file system. Compression will be determined
     * automatically based on the given filename. If the filename ends with .gz
     * then compression will be enabled.
     * 
     * @param obj
     *            the object to be serialized
     * @param file
     *            the file to which the object should be serialized
     * @param useMemory
     *            whether we should serialize the object into memory before
     *            writing to the file system. Enabling this option, when enough
     *            memory is available, tends to speed the process up
     *            considerably, since serializing to disk tends to take a while.
     *            When there may not be enough memory to serialize the whole
     *            object into memory first, it may be more prudent to serialize
     *            straight to disk (slower, but maybe necessary for large
     *            objects).
     * @throws java.io.IOException
     */
    public static void serialize(java.io.Serializable obj, File file, boolean useMemory) throws java.io.IOException {
        boolean gzip = isGzip(file);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Serializing " + obj.getClass().getName() + " to " + file.getCanonicalPath() + " (memory=" + useMemory + ", gzip=" + gzip + ")");
        }

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        byte[] serializedDataInMemory = null;
        if (useMemory) {
            // Serialize in memory for best speed
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(obj);
            serializedDataInMemory = baos.toByteArray();
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Serialized " + obj.getClass().getName() + " to " + serializedDataInMemory.length + " bytes in memory");
            }
        }

        // First we'll write it to a temp file
        final File tempFile = new File(file.getCanonicalPath() + ".tmp");
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Writing serialized content to temp file: " + tempFile.getCanonicalPath());
        }
        final FileOutputStream tempFileOut = new FileOutputStream(tempFile);
        GZIPOutputStream gzipOut = null;
        final OutputStream outputStream;
        if (gzip) {
            // Apply GZIP Compression
            outputStream = gzipOut = new GZIPOutputStream(tempFileOut);
        } else {
            outputStream = tempFileOut;
        }
        if (serializedDataInMemory != null) {
            // We already serialized, use the data in memory
            IOUtils.write(serializedDataInMemory, outputStream);
        } else {
            // We need to serialize now
            new ObjectOutputStream(outputStream).writeObject(obj);
        }
        if (gzipOut != null) {
            gzipOut.flush();
            gzipOut.close();
        }
        tempFileOut.flush();
        tempFileOut.close();

        // Now just move the temp file over the destination file
        if (!tempFile.renameTo(file)) {
            if (!tempFile.delete()) {
                LOG.warning("Failed to delete " + tempFile.getCanonicalPath());
            }
            throw new RuntimeException("Failed to rename " + tempFile.getCanonicalPath() + " to " + file.getCanonicalPath());
        }

        stopWatch.stop();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("******** Serialization of " + file.getName() + " took " + stopWatch);
        }
    }

    /**
     * Deserialize an object from the file system. Compression will be
     * determined automatically based on the given filename. If the filename
     * ends with .gz then compression will be enabled.
     * 
     * @param clazz
     *            the class of the object to be deserialized
     * @param file
     *            the file from which the object should be deserialized
     * @param useMemory
     *            whether we should read the content into memory before
     *            deserializing. Enabling this option, when enough memory is
     *            available, tends to speed the process up considerably, since
     *            deserializing from disk tends to take a while. When there may
     *            not be enough memory to read the whole file into memory first,
     *            it may be more prudent to deserialize straight from disk
     *            (slower, but maybe necessary for large objects).
     * @return the deserialized object
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    public static <T extends java.io.Serializable> T deserialize(Class<T> clazz, File file, boolean useMemory) throws java.io.IOException, ClassNotFoundException {
        boolean gzip = isGzip(file);
        final int maxAttempts = 3;
        int attempts = 0;
        // Try the read in a loop so we can retry if we bump into a stale NFS
        // handle issue
        while (true) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Deserializing " + clazz.getName() + " from " + file.getCanonicalPath() + " (memory=" + useMemory + ", gzip=" + gzip + ")");
            }

            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            try (final FileInputStream fileInputStream = new FileInputStream(file);) {

                InputStream inputStream;
                if (useMemory) {
                    // Read it into memory first
                    final byte[] data = IOUtils.toByteArray(fileInputStream);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Read " + data.length + " bytes into memory from " + file.getName());
                    }
                    inputStream = new ByteArrayInputStream(data);
                } else {
                    // Read it straight from the file system
                    inputStream = fileInputStream;
                }

                // Buffer the input
                inputStream = new BufferedInputStream(inputStream);

                if (gzip) {
                    // Apply GZIP decompression
                    inputStream = new GZIPInputStream(inputStream);
                }

                // Deserialize the object
                return (T) new ObjectInputStream(inputStream).readObject();
            } catch (java.io.IOException e) {
                if (++attempts < maxAttempts) {
                    LOG.log(Level.WARNING, "Attempt #" + attempts + " of " + maxAttempts + " failed to read " + file.getCanonicalPath(), e);
                    // Sleep for a second and then retry
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException x) {
                        //do nothing
                    }
                } else {
                    LOG.log(Level.SEVERE, "Failed to read " + file.getCanonicalPath() + " after " + attempts + " attempts", e);
                    throw e;
                }
            } finally {
                stopWatch.stop();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("******** Deserialization of " + file.getName() + " took " + stopWatch);
                }
            }
        }
    }

    /**
     * Does a given file need/have GZIP compression?
     * 
     * @return true if the filename ends with .gz, otherwise false
     */
    public static boolean isGzip(File file) {
        return file.getName().endsWith(".gz");
    }
}
