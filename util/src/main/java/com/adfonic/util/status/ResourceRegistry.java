package com.adfonic.util.status;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author mvanek
 *
 */
public class ResourceRegistry<ID extends Serializable> implements Iterable<ResourceStatus>, Closeable {

    private final Map<ID, ResourceEntry<ID>> resources = new HashMap<ID, ResourceEntry<ID>>();

    /**
     * x = 0 - status check is made every request
     * x > 0 - status check is cached for x seconds. Used in both request and background thread modes
     */
    private final int periodMillis;

    // caller thread periodic status checks (optional)
    private final Lock lock = new ReentrantLock();

    private long nextCheckMillis = 0;

    // background thread periodic status checks (optional)
    private final ResourceStatusCheckThread thread;

    public ResourceRegistry() {
        this(0);
    }

    public ResourceRegistry(int periodSeconds) {
        this(periodSeconds, false);
    }

    public ResourceRegistry(int periodSeconds, boolean backgroundThread) {
        this.periodMillis = periodSeconds * 1000;
        if (backgroundThread) {
            if (periodSeconds <= 0) {
                throw new IllegalStateException("Check period must be > 0 when background thread is used");
            }
            this.thread = new ResourceStatusCheckThread();
            this.thread.start();
        } else {
            this.thread = null;
        }
    }

    public ResourceId<ID> addResource(ID id, ResourceCheck<ID> check) {
        return addResource(id, null, check);
    }

    public ResourceId<ID> addResource(ID id, String description, ResourceCheck<ID> check) {
        ResourceId<ID> resourceId = new ResourceId<ID>(id, localHostname, description);
        if (resources.containsKey(id)) {
            throw new IllegalArgumentException("Resource already registered: " + id);
        }
        //really check right away?
        ResourceStatus status = check.checkStatus(resourceId);
        resources.put(id, new ResourceEntry<ID>(resourceId, check, status));
        return resourceId;
    }

    @Override
    public void close() throws IOException {
        if (thread != null) {
            thread.setStopFlag();
            thread.interrupt();
        }
    }

    public ResourceStatus checkStatus(ID id) {
        Date started = new Date();
        ResourceEntry<ID> entry = resources.get(id);
        if (entry == null) {
            return new ResourceStatus(new ResourceId<ID>(id, localHostname, null), started, new IllegalArgumentException("Resource not registered " + id));
        }
        ResourceStatus status;
        try {
            status = entry.getCheck().checkStatus(entry.getId());
        } catch (Exception x) {
            status = new ResourceStatus(entry.getId(), started, x);
        }
        entry.setStatus(status);
        return status;
    }

    private void checkResources() {
        for (ResourceEntry<ID> resource : resources.values()) {
            checkStatus(resource.getId().getId());
        }
    }

    @Override
    public Iterator<ResourceStatus> iterator() {
        // do not execute checks if background thread exists 
        if (thread == null && periodMillis > 0) {
            lock.lock();
            try {
                long currentMillis = System.currentTimeMillis();
                if (currentMillis > nextCheckMillis) {
                    checkResources();
                    nextCheckMillis = currentMillis + periodMillis;
                }
            } finally {
                lock.unlock();
            }
        }
        return new StatusIterator(periodMillis <= 0);
    }

    class StatusIterator implements Iterator<ResourceStatus> {

        private final boolean check;

        public StatusIterator(boolean check) {
            this.check = check;
        }

        Iterator<ResourceEntry<ID>> internal = resources.values().iterator();

        @Override
        public boolean hasNext() {
            return internal.hasNext();
        }

        @Override
        public ResourceStatus next() {
            ResourceEntry<ID> entry = internal.next();
            if (check) {
                return checkStatus(entry.getId().getId());
            } else {
                return entry.getStatus();
            }
        }

        @Override
        public void remove() {
            internal.remove();
        }

    }

    class ResourceStatusCheckThread extends Thread {

        private boolean doContinue = true;

        public ResourceStatusCheckThread() {
            setDaemon(true);
            setName("ResourceStatusCheck");
        }

        public void setStopFlag() {
            this.doContinue = false;
        }

        @Override
        public void run() {
            while (doContinue) {
                checkResources();
                try {
                    Thread.sleep(periodMillis);
                } catch (InterruptedException ix) {
                    // ignore, wakeup call
                }
            }
        }
    }

    static class ResourceEntry<ID extends Serializable> {

        private final ResourceId<ID> id;
        private final ResourceCheck<ID> check;
        private ResourceStatus status;

        public ResourceEntry(ResourceId<ID> id, ResourceCheck<ID> check, ResourceStatus status) {
            this.id = id;
            this.check = check;
            this.status = status;
        }

        public ResourceId<ID> getId() {
            return id;
        }

        public ResourceCheck<ID> getCheck() {
            return check;
        }

        public ResourceStatus getStatus() {
            return status;
        }

        public void setStatus(ResourceStatus status) {
            this.status = status;
        }
    }

    private static String localHostname;

    static {
        InetAddress localHost;
        try {
            localHost = InetAddress.getLocalHost();
            localHostname = localHost.getHostName();
        } catch (Exception x) {
            localHostname = "unknown";

        }
    }

    public static String getLocalHostname() {
        return localHostname;
    }

}
