package com.adfonic.adserver.stoppages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.adfonic.adserver.Stoppage;

public class StoppagesFromDb implements StoppagesService {

    private RunnableFuture<StoppagesCollection> stoppagesCollectionFuture;

    private ExecutorService executor = Executors.newFixedThreadPool(1);


    public StoppagesFromDb(final DataSource dataSource) {
        Callable<StoppagesCollection> retrieveStoppagesFromDb = createTaskToRetrieveStoppagesFromDb(dataSource);

        stoppagesCollectionFuture = new FutureTask<StoppagesCollection>(retrieveStoppagesFromDb);

        executor.execute(stoppagesCollectionFuture);
    }


    @Override
    public Map<Long, Stoppage> getAdvertiserStoppages() throws IOException {
        return getStoppagesCollection().getAdvertiserStoppages();

    }

    private StoppagesCollection getStoppagesCollection() throws IOException {
        try {
            return stoppagesCollectionFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Map<Long, Stoppage> getCampaignStoppages() throws IOException {
        return getStoppagesCollection().getAdvertiserStoppages();

    }


    private Callable<StoppagesCollection> createTaskToRetrieveStoppagesFromDb(final DataSource dataSource) {

        return new Callable<StoppagesCollection>() {

            @Override
            public StoppagesCollection call() throws Exception {


                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                Map<Long, Stoppage> campaignStoppages = new HashMap<>();
                Map<Long, Stoppage> advertiserStoppages = new HashMap<>(); ;
                try {
                    conn = dataSource.getConnection();
                    ps = conn.prepareStatement("SELECT ADVERTISER_ID, TIMESTAMP, REACTIVATE_DATE"
                                               + " FROM ADVERTISER_STOPPAGE"
                                               + " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        Stoppage stoppage = new Stoppage(rs.getTimestamp(2), rs.getTimestamp(3));
                        advertiserStoppages.put(id, stoppage);
                    }
                    rs.close();
                    ps.close();

                    ps = conn.prepareStatement("SELECT CAMPAIGN_ID, TIMESTAMP, REACTIVATE_DATE"
                                               + " FROM CAMPAIGN_STOPPAGE"
                                               + " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        Stoppage stoppage = new Stoppage(rs.getTimestamp(2), rs.getTimestamp(3));
                        campaignStoppages.put(id, stoppage);
                    }
                } catch (java.sql.SQLException e) {
                    throw new IOException("Failed to load stoppages", e);
                } finally {
                    DbUtils.closeQuietly(conn, ps, rs);
                }

                return new StoppagesCollection(advertiserStoppages, campaignStoppages);


            }
        };
    }

}
