package com.adfonic.tasks.whatsup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.EqualPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.AdserverShard;
import com.adfonic.domain.AdserverStatus;
import com.adfonic.domain.AdserverStatus.Status;

public class WhatsUpConnection implements Runnable {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private static final String NEW_LINE = "\\n";

    private AdserverStatus adserverStatus;
    private String url;
    private int connectionTimeout;
    private int readTimeout;

    private List<AdserverShard> shards;

    public WhatsUpConnection(AdserverStatus adserverStatus, String url, int connectionTimeout, int readTimeout, List<AdserverShard> shards) {
        this.adserverStatus = adserverStatus;
        this.url = url;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.adserverStatus.setLastUpdated(new Date());
        this.shards = shards;
    }

    public AdserverStatus getAdserverStatus() {
        return adserverStatus;
    }

    private AdserverShard find(String shardName) throws IllegalArgumentException {
        EqualPredicate nameEqlPredicate = new EqualPredicate(shardName);
        BeanPredicate beanPredicate = new BeanPredicate("name", nameEqlPredicate);
        Collection<AdserverShard> filteredCollection = CollectionUtils.select(shards, beanPredicate);
        if (filteredCollection.size() == 1) {
            return filteredCollection.iterator().next();
        } else {
            throw new IllegalArgumentException("Invalid shard name");
        }
    }

    @Override
    public void run() {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(connectionTimeout);
            con.setReadTimeout(readTimeout);
            con.connect();

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                adserverStatus.setStatus(Status.FAILED);
                adserverStatus.setDescription(Status.FAILED.getDescription());
                return;
            } else {
                Scanner scanner = new Scanner(con.getInputStream());
                scanner.useDelimiter(NEW_LINE);
                AdserverShard shard = find(scanner.next());
                if (shard != null) {
                    LOG.debug("AdServer : {} is Up", adserverStatus.getName());
                    adserverStatus.setStatus(Status.OK);
                    adserverStatus.setDescription(Status.OK.getDescription());
                    if (!adserverStatus.getShard().equals(shard)) {
                        adserverStatus.setShard(shard);
                    }
                }
                scanner.close();
            }
        } catch (SocketTimeoutException e) {
            LOG.debug("AdServer : {} is hanging", adserverStatus.getName());
            adserverStatus.setStatus(Status.TIMEDOUT);
            adserverStatus.setDescription(Status.TIMEDOUT.getDescription());
        } catch (MalformedURLException e) {
            LOG.debug("AdServer : {} malformed. {}", adserverStatus.getName(), e);
            adserverStatus.setStatus(Status.BAD_URL);
            adserverStatus.setDescription(Status.BAD_URL.getDescription());
        } catch (UnknownHostException e) {
            LOG.debug("AdServer : {} not found", adserverStatus.getName());
            adserverStatus.setStatus(Status.DNS_FAILED);
            adserverStatus.setDescription(Status.DNS_FAILED.getDescription());
        } catch (IllegalArgumentException e) {
            LOG.debug("AdServer : {} belongs to invalid shard. {}", adserverStatus.getName(), e);
            adserverStatus.setStatus(Status.FAILED);
            adserverStatus.setDescription(Status.FAILED.getDescription());
        } catch (IOException e) {
            LOG.debug("AdServer : {} failed. {}", adserverStatus.getName(), e);
            adserverStatus.setStatus(Status.FAILED);
            adserverStatus.setDescription(Status.FAILED.getDescription());
        }
    }
}
