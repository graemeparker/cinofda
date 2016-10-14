package com.adfonic.util;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class FileUpdateMonitor {
    private static final transient Logger LOG = Logger.getLogger(FileUpdateMonitor.class.getName());
    private final File file;
    private final String fileAbsolutePath;
    private final int monitorPeriodSec;
    private final Runnable runWhenUpdated;
    private FileAlterationMonitor monitor;

    public FileUpdateMonitor(File file, int monitorPeriodSec, Runnable runWhenUpdated) {
        this.file = file;
        this.fileAbsolutePath = file.getAbsolutePath();
        this.monitorPeriodSec = monitorPeriodSec;
        this.runWhenUpdated = runWhenUpdated;
    }

    public synchronized void start() {
        if (monitor != null) {
            throw new IllegalStateException("Already started");
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Starting monitor for " + file.getAbsolutePath());
        }

        // commons-io FileAlterationObserver only works with directories,
        // but we can provide a FileFilter to watch only our specific file.
        final File dir = new File(file.getAbsolutePath()).getParentFile();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Parent dir: " + dir.getAbsolutePath());
        }
        FileAlterationObserver observer = new FileAlterationObserver(dir, new FileFilter() {
            @Override
            public boolean accept(File candidate) {
                // NOTE!!! We used to simply check candidate.equals(file), but
                // that does
                // not work reliably when "file" was created with a
                // non-fully-qualified path.
                // So we're resorting to using absolute path comparison, which
                // seems to be
                // the most reliable way to go. Was going to use canonical path
                // here, but
                // I believe absolute path is slightly more efficient (and it
                // doesn't throw).
                return fileAbsolutePath.equals(candidate.getAbsolutePath());
            }
        });

        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(File file) {
                LOG.info(file.getAbsolutePath() + " was updated");
                try {
                    runWhenUpdated.run();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Runnable failed", e);
                }
            }
        });

        monitor = new FileAlterationMonitor(monitorPeriodSec * 1000L);
        monitor.setThreadFactory(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "FileUpdateMonitor-" + file.getAbsolutePath());
                thread.setDaemon(true);
                return thread;
            }
        });
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void stop() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Stopping monitor for " + file.getAbsolutePath());
        }
        // As of commons-io 2.1, we can call stop(interval) on the monitor.
        try {
            // We want the monitor to stop immediately.
            // We can't pass -1 here since this value gets passed to
            // Thread.join.
            // 0 means wait forever, so let's go with 1ms...best we can do.
            monitor.stop(1);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
        monitor = null;
    }
}
