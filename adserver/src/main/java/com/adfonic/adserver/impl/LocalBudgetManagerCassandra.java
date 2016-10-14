package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.util.stats.CounterManager;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.QueryExecutionException;

/**
 * add counters
 * 
 * @author bijanfathi
 */
public class LocalBudgetManagerCassandra implements LocalBudgetManager {

    private static final String READ_CURRENT = "SELECT current, reserved, spent, click, cpcClicks FROM budget_adserver WHERE id = ? AND adserver = ?;";
    private static final String COND_UPDATE = "UPDATE budget_adserver set current = ?, reserved = ?, spent = ?, click = ?, cpcClicks = ? WHERE id = ? AND adserver = ? IF current = ? AND reserved = ? AND spent = ? AND click = ? AND cpcClicks = ?;";

    private static final String SELECT_ADSERVER_OPEN_BID = "SELECT campaign_id, adserver, type, reserved, reservedLoss, timestamp, reference FROM budget_open_bid WHERE adserver= ?;";
    private static final String READ_OPEN_BID = "SELECT campaign_id, adserver, type, reserved, reservedLoss, timestamp, reference FROM budget_open_bid WHERE reference = ?;";
    private static final String INSERT_OPEN_BID = "INSERT INTO budget_open_bid (reserved, reservedLoss, campaign_id, adserver, type, timestamp, reference) VALUES (?, ?, ?, ?, ?, ?, ?) USING TTL ?;";
    
    private static final String PROB_LOSS_OPEN_BID = "UPDATE budget_open_bid USING TTL ? set reserved = ?, reservedLoss = ?, campaign_id = ?, adserver = ?, type = ?, timestamp = ? WHERE reference = ?;";
    private static final String DELETE_OPEN_BID = "DELETE FROM budget_open_bid WHERE reference = ?;";

    private static final String INSERT_DEDUP_CLICK = "INSERT INTO budget_click_dedup (reference, adserver, timestamp) VALUES (?, ?, ?) IF NOT EXISTS USING TTL ?;";
    private static final String SELECT_DEDUP_CLICK = "SELECT adserver, timestamp FROM budget_click_dedup WHERE reference = ?;";

    private static final String READ_TOTAL_CLICKS = "SELECT clicktotalperformed, clickdailyperformed, clicktotalbudget, clickdailybudget FROM budget WHERE id = ?;";
    private static final String COND_UPDATE_TOTAL_CLICKS = "UPDATE budget set clicktotalperformed = ?, clickdailyperformed = ? WHERE id = ? IF clicktotalperformed = ? and clickdailyperformed = ?;";

    private static final transient Logger LOG = Logger.getLogger(LocalBudgetManagerCassandra.class.getName());
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final MathContext MC = new MathContext(8, RoundingMode.CEILING);

    private final CounterManager counterManager;
    

    protected final Cluster cluster;
    protected final Session session;
    protected PreparedStatement psRead;
    protected PreparedStatement psUpdate;

    protected PreparedStatement psSelectAdserverOpenBid;
    protected PreparedStatement psReadOpenBid;
    protected PreparedStatement psInsertOpenBid;
    protected PreparedStatement psProbLossOpenBid;
    protected PreparedStatement psDeleteOpenBid;

    protected PreparedStatement psInsertDedupClick;
    protected PreparedStatement psSelectDedupClick;

    protected PreparedStatement psReadTotalClicks;
    protected PreparedStatement psUpdateTotalClicks;
    protected boolean extraLogging = false;

    public LocalBudgetManagerCassandra(Cluster cluster, Session session, CounterManager counterManager, int clickTtlSec, boolean extraLogging) {
        this.cluster = cluster;
        this.session = session;
        this.counterManager = counterManager;
        this.clickTtlSec = clickTtlSec;
        this.extraLogging = extraLogging;
        try {
            this.currentAdserver = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warning("Unable to determine hostname: " + e.getMessage());
        }

        ConsistencyLevel write = ConsistencyLevel.QUORUM;
        initialize(write);
        cleanupThread = new TimeoutCleanupThread();
        cleanupThread.start();
    }

    private TimeoutCleanupThread cleanupThread;

    public int underfundedTimeoutMs = 2000;
    private String currentAdserver;
    protected int maxRetries = 2;
    protected int maxCasRetries = 30;
    protected int maxErrors = 3;
    protected long errorTrackTimeframeMs = 5000;
    protected long longBidTimeoutMs = 60000;
    
    protected BigDecimal maxLocalBudgetMonetary = new BigDecimal("0.50");
    protected BigDecimal maxLocalBudgetImpression = new BigDecimal("20");
    protected BigDecimal longBidLossProbability = new BigDecimal("0.80");
    protected long failBidTimeoutMs = 1_800_000;
    protected AtomicLong errorsOccured = new AtomicLong();
    
    private Map <Long, AtomicLong> localBudgets = new ConcurrentHashMap<>();
    protected int clickTtlSec;

    ThreadLocal<Map<Long, Long>> recentUnderfunded = new ThreadLocal<Map<Long, Long>>() {
        @Override
        protected Map<Long, Long> initialValue() {
            return new HashMap<Long, Long>();
        };
    };

    @Override
    public boolean verifyAndReserveBudget(String ref, CampaignDto campaign, BigDecimal value) {
        boolean success = false;
        try {
            if (campaign.isBudgetManagerEnabled() && campaign.getCurrentBid().getBidType() == BidType.CPM || //
            campaign.isBudgetManagerEnabled() && campaign.getCurrentBid().getBidType() == BidType.CPC) {
                Long effValueLong = calcEffectiveValueAsLong(campaign.getCurrentBid().getBudgetType(), value);
                
                if(extraLogging) LOG.info("effValueLong " + effValueLong);
                
                AtomicLong currentValue = localBudgets.get(campaign.getId());
                if (currentValue == null) {
                    if(extraLogging)  LOG.info("currentValue is null ");
                    localBudgets.putIfAbsent(campaign.getId(), new AtomicLong() );
                    currentValue = localBudgets.get(campaign.getId());
                }

                BigDecimal effValue = calcEffectiveValue(campaign.getCurrentBid().getBudgetType(), value);
                if(extraLogging) LOG.info("effValue " + effValue);
                
                Long afterDeduct = currentValue.addAndGet(effValueLong*-1);
                if(extraLogging) LOG.info("afterDeduct " + afterDeduct);
                
                if (afterDeduct >= 0) {
                	success = true;
                } else {
                	success = remoteAcquireBudgetOrAll(ref, campaign, getMaxLocalBudget(campaign), currentValue);
                	if(extraLogging) LOG.info("after remoteAcquireBudgetOrAll currentValue "+ currentValue.get());
                	// succesfull acquire does not guarantee enough funds
                	success &= (currentValue.get() > 0);
                	if (!success) {
                	    if(extraLogging) LOG.info("not success currentValue "+ currentValue.get());
                		currentValue.addAndGet(effValueLong);
                	}
                }
                
                if (success) {
                    if(extraLogging) LOG.info("success effValue "+ effValue);
    		        insertOpenBid(ref, campaign, effValue);
                }
                
            } else {
                success = true;
            }
        } catch (Exception t) {
            errorsOccured.incrementAndGet();
            counterManager.incrementCounter("bm.reserveError." + campaign.getId());
            counterManager.incrementCounter("bm.reserveException." + t.getMessage());
            LOG.warning("Unable to perform reserve budget: " + t.getClass().getName() + ":" + t.getMessage());
        }

        return success;
    }

	private BigDecimal getMaxLocalBudget(CampaignDto campaign) {
		return campaign.getCurrentBid().getBudgetType() == BudgetType.IMPRESSIONS ? maxLocalBudgetImpression : maxLocalBudgetMonetary;
	}

	private boolean remoteAcquireBudgetOrAll(String ref, CampaignDto campaign, BigDecimal maxAmount, AtomicLong localReserve) {
		AdserverBudget budget = readCurrent(campaign.getId());
		AdserverBudget newVal = budget.copy();
		BigDecimal effValue = budget.current.min(maxAmount);
		boolean success = false;

		if (effValue.signum() > 0) {
		    int m = maxCasRetries;
		    boolean outOfFunds = false;
		    // TODO here we only want to move effValue from current to reserved, not need to check any other field in particular spent, click
		    // TODO in case of Timeout we should assume not budget and stop hammering cassandra
		    boolean wasApplied = false;
		    if(extraLogging) LOG.info("m " + m  + " effValue " + effValue + " budget.current "+ budget.current + " maxAmount " + maxAmount);
		    while (!(wasApplied=updateBudget(campaign.getId(), newVal.reserve(effValue), budget, currentAdserver)) && m > 0) {
		        
		        if(extraLogging) LOG.info("m " + m  + "wasApplied " + wasApplied + " budget.current "+ budget.current + " maxAmount " + maxAmount);
		        budget = readCurrent(campaign.getId());
		        effValue = budget.current.min(maxAmount);
		        newVal = budget.copy();
		        m--;
		        if (effValue.signum() <= 0) {
		            outOfFunds = true;
		            break;
		        }
		    }
		    
		    if(extraLogging) LOG.info("wasApplied " + wasApplied + " outOfFunds " + outOfFunds);
		    if (!wasApplied || outOfFunds) {
		        if (outOfFunds) {
		            if (LOG.isLoggable(Level.FINE)) {
		                LOG.fine("Campaign " + campaign.getId() + " out of funds: " + budget + ", not serving.");
		            }
		            counterManager.incrementCounter("bm.outOfFunds." + campaign.getId());
		            recentUnderfunded.get().put(campaign.getId(), System.currentTimeMillis());
		        } else {
		            if (LOG.isLoggable(Level.FINE)) {
		                LOG.warning("Unable to acquire budget, max retries failed: " + ref);
		            }
		            counterManager.incrementCounter("bm.maxRetries.verify." + campaign.getId());
		        }
		    } else {
		    	if (campaign.getCurrentBid().getBudgetType() == BudgetType.IMPRESSIONS) {
		    	    if(extraLogging) LOG.info("IMPRESSIONS effValue " + effValue);
		    		localReserve.addAndGet(effValue.longValue());
		    	} else {
		    	    long effV = effValue.multiply(MILLION).round(MC).longValue();
		    	    if(extraLogging) LOG.info("not IMPRESSIONS effV " + effV);
		    		localReserve.addAndGet(effV);
		    	}
		        success = true;
		    }

		} else {
		    counterManager.incrementCounter("bm.outOfFunds." + campaign.getId());
		    if (LOG.isLoggable(Level.FINE)) {
		        LOG.fine("Campaign " + campaign.getId() + " out of funds: " + budget + ", not serving.");
		    }
		    recentUnderfunded.get().put(campaign.getId(), System.currentTimeMillis());
		}
		return success;
	}

    @Override
    public boolean acquireBudget(String ref, BigDecimal settlementPrice, boolean useReservedValue) {
        CampaignBudget cb = readOpenBid(ref);
        boolean found = cb != null;
        if (found) {
            BigDecimal reserved = cb.getReserved().round(MC);
            BigDecimal spent = calcEffectiveValue(cb.getBudgetType(), settlementPrice);
            try {
                boolean success = retryAcquireBudget(useReservedValue, cb, reserved, spent);
                deleteOpenBid(ref);
                if (!success) {
                    counterManager.incrementCounter("bm.maxRetries.acquire." + cb.campaignId);
                    LOG.warning("Unable to acquire budget, max retries failed: " + ref);
                }
            } catch (Exception t) {
                errorsOccured.incrementAndGet();
                counterManager.incrementCounter("bm.acquireError." + cb.campaignId);
                counterManager.incrementCounter("bm.acquireException." + t.getMessage());
                LOG.warning("Unable to perform acquire budget: " + t.getClass().getName() + ":" + t.getMessage());
            }
        } else {
            counterManager.incrementCounter("bm.acquireUnknown");
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Received acquire for unknown bid: " + ref);
            }
        }
        return found;
    }

    private boolean retryAcquireBudget(boolean useReservedValue, CampaignBudget cb, BigDecimal reserved, BigDecimal spent) throws InterruptedException {
        boolean success = false;
        int m = maxCasRetries;
        AdserverBudget budget = readCurrent(cb.campaignId, cb.adserver);
        AdserverBudget newVal = budget.copy();

        // TODO click is not updated here, but its being checked as part of condition
        // TODO if updateBudget throws timeout and if records was updated then by repeating we adding spent multiple times
        while (!(success = updateBudget(cb.getCampaignId(), newVal.spend(reserved, useReservedValue ? reserved : spent, cb.reservedLoss), budget, cb.adserver)) && m > 0) {
            budget = readCurrent(cb.getCampaignId(), cb.adserver);
            newVal = budget.copy();
            m--;
        }
        return success;
    }

    private CampaignBudget readOpenBid(String ref) {
        CampaignBudget retval = null;

        if (psReadOpenBid != null) {
            ResultSet rs = session.execute(psReadOpenBid.bind(ref));
            List<Row> rows = rs.all();
            if (!rows.isEmpty()) {
                retval = readOpenBidRow(rows.get(0));
            } else {
                counterManager.incrementCounter("bm.readOpenBid.unknownReference");
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Unable to retrieve open bid for and reference: " + ref);
                }
            }
        }

        return retval;
    }

    private List<CampaignBudget> selectAdserverOpenBids(String adserver) {
        List<CampaignBudget> retval = new ArrayList<>();

        if (psReadOpenBid != null) {
            ResultSet rs = session.execute(psSelectAdserverOpenBid.bind(adserver));
            for (Row r : rs.all()) {
                retval.add(readOpenBidRow(r));
            }
        }

        return retval;
    }

    private CampaignBudget readOpenBidRow(Row row) {
        CampaignBudget retval;
        retval = new CampaignBudget();
        retval.campaignId = row.getLong(0);
        retval.adserver = row.getString(1);
        retval.budgetType = BudgetType.valueOf(row.getString(2));
        retval.reserved = row.getDecimal(3);
        retval.reservedLoss = row.getDecimal(4);
        retval.timestamp = row.getLong(5);
        retval.reference = row.getString(6);
        return retval;
    }

    private boolean insertOpenBid(String ref, CampaignDto campaign, BigDecimal effValue) {
        int m = maxRetries;
        boolean successful = false;
        Long ttlSec = (failBidTimeoutMs+longBidTimeoutMs*15) / 1_000;

        while (m > 0 && !successful && psInsertOpenBid != null) {
            try {
                ResultSet rs = session.execute(psInsertOpenBid.bind(effValue, BigDecimal.ZERO, campaign.getId(), currentAdserver,
                        campaign.getCurrentBid().getBudgetType().toString(), System.currentTimeMillis(), ref, ttlSec.intValue()));
                successful = rs.wasApplied();
            } catch (QueryExecutionException qe) {
                LOG.warning("Unable to record bid: " + ref +" "+ qe.getMessage());
                errorsOccured.incrementAndGet();
            }
            m--;
        }
        if (!successful) {
            counterManager.incrementCounter("bm.insertOpenBid.insertFailed." + campaign.getId());
            LOG.warning("Unable to record bid: " + ref + " campaign: " + campaign.getId() + " value: " + effValue);
        }

        return successful;
    }

    private boolean updateProbLossInOpenBid(CampaignBudget cb, BigDecimal probLoss) {
        int m = maxRetries;
        boolean successful = false;
        Long ttlSec = (failBidTimeoutMs+longBidTimeoutMs*10) / 1_000;

        while (m > 0 && !successful && psProbLossOpenBid != null) {
            try {
                CampaignBudget current = readOpenBid(cb.reference);

                if (current != null) {
                    ResultSet rs = session.execute(psProbLossOpenBid.bind(ttlSec.intValue(), cb.reserved.subtract(probLoss), 
                    		probLoss, cb.campaignId, cb.adserver, cb.budgetType.toString(), cb.timestamp, cb.reference));
                    successful = rs.wasApplied();
                } else {
                	successful = true;
                }
                
            } catch (QueryExecutionException qe) {
                LOG.warning("psProbLossOpenBid failed campaign: " + cb.campaignId +" "+ qe.getMessage());
                errorsOccured.incrementAndGet();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    counterManager.incrementCounter("bm.updateProbLoss.queryError." + e.getMessage());
                }
            }
            m--;
        }
        if (!successful) {
            counterManager.incrementCounter("bm.updateProbLoss.retriesFailed." + cb.campaignId);
            LOG.warning("Unable to updateProbLossInOpenBid: " + cb.reference + " campaign: " + cb.campaignId + " value: " + probLoss);
        }

        return successful;
    }

    private boolean deleteOpenBid(String ref) {
        int m = maxRetries;
        boolean successful = false;

        while (m > 0 && !successful && psDeleteOpenBid != null) {
            try {
                ResultSet rs = session.execute(psDeleteOpenBid.bind(ref));
                successful = rs.wasApplied();
            } catch (QueryExecutionException qe) {
                errorsOccured.incrementAndGet();
                counterManager.incrementCounter("bm.deleteBid.queryError." + qe.getMessage());
            }
            m--;
        }
        if (!successful) {
            LOG.warning("Unable to delete open bid: " + ref);
        }
        return successful;
    }

    /**
     * TODO that is NOT cached in localBudget
     * R = open_budget.reserved
     * delete open_budget
     * return reserved to adserver.current += R
     * adserver.reserved += R
     * 
     * the only reason for updating adserver is to give back reserved.
     * consider instead :
     * localBudgets +=R
     * and remove adserver.reserved field
     * its stored anyway in open_bid
     * deleting open_bid doesnt conflict/compete, but updaing adserver does 
     */
    @Override
    public boolean releaseBudget(String ref) {
        boolean success = false;

        try {
            CampaignBudget cb = readOpenBid(ref);
            if (cb != null) {
                int m = retryReleaseBudget(cb, cb.reserved);
                deleteOpenBid(ref);
                if (m == 0) {
                    LOG.warning("Unable to release budget, max retries failed: " + ref);
                } else {
                    success = true;
                }
            } else {
                LOG.fine("Received acquire for unknown bid: " + ref);
            }
        } catch (Exception t) {
            errorsOccured.incrementAndGet();
            counterManager.incrementCounter("bm.releaseBudget.error." + t.getMessage());
            LOG.warning("Unable to perform release budget: " + t.getClass().getName() + ":" + t.getMessage());
        }

        return success;
    }

    private int retryReleaseBudget(CampaignBudget cb, BigDecimal reservedValue) throws InterruptedException {
        int m = maxCasRetries;
        AdserverBudget budget = readCurrent(cb.campaignId, cb.adserver);
        AdserverBudget newVal = budget.copy();

        // TODO only current and reserved changed, no need to check spent
        while (!updateBudget(cb.getCampaignId(), newVal.refund(reservedValue), budget, cb.adserver) && m > 0) {
            budget = readCurrent(cb.getCampaignId(), cb.adserver);
            newVal = budget.copy();
            m--;
        }
        return m;
    }

    private Long getLongOrNull(Row row, String col) {
        long longVal = row.getLong(col);
        if (row.isNull(col)) {
            return null;
        }

        return longVal;
    }

    boolean retryInsertDedupClick(String reference, long timestamp) {
        int m = maxRetries;
        do {
            m--;
            try {
                BoundStatement bs = psInsertDedupClick.bind(reference, this.currentAdserver, timestamp, clickTtlSec);
                boolean success = session.execute(bs).wasApplied();
                return success;
            } catch (QueryExecutionException qe) {
                errorsOccured.incrementAndGet();
                counterManager.incrementCounter("bm.insertDedupClick.error." + qe.getMessage());
            }

        } while (m > 0);

        LOG.warning("Unable to insert dedup click, max retries failed: " + maxRetries);
        return false;
    }

    @Override
    public ClickRegisterState registerClick(String reference, CampaignDto campaign) {
        ClickRegisterState retval = ClickRegisterState.NORMAL;

        // dedup is only performed on bm managed cpc campaigns
        if (!campaign.isBudgetManagerEnabled()) {
            return retval;
        }

        try {
            // insert => true, duplicate and/or errors => false
            boolean success = retryInsertDedupClick(reference, System.currentTimeMillis());

            int m = maxRetries;
            if (success) {
                boolean casSuccess = false;

                m = maxRetries * 10;
                while (!casSuccess && m > 0) {
                    ResultSet rs = session.executeAsync(psReadTotalClicks.bind(campaign.getId())).get();
                    List<Row> rows = rs.all();
                    if (!rows.isEmpty()) {
                        Row row = rows.get(0);
                        Long clickTotalPerformed = getLongOrNull(row, "clicktotalperformed");
                        Long clickDailyPerformed = getLongOrNull(row, "clickdailyperformed");
                        Long clickTotalBudget = getLongOrNull(row, "clicktotalbudget");
                        Long clickDailyBudget = getLongOrNull(row, "clickdailybudget");

                        if (clickDailyBudget != null && clickDailyPerformed != null && clickDailyBudget <= clickDailyPerformed) {
                            retval = ClickRegisterState.OVER_BUDGET;
                        }

                        if (clickTotalBudget != null && clickTotalPerformed != null && clickTotalBudget <= clickTotalPerformed) {
                            retval = ClickRegisterState.OVER_BUDGET;
                        }

                        if(retval == ClickRegisterState.OVER_BUDGET) {
                            break;
                        }
                        
                        long clickTotalPerformed1 = clickTotalPerformed == null ? 1 : clickTotalPerformed + 1;
                        long clickDailyPerformed1 = clickDailyPerformed == null ? 1 : clickDailyPerformed + 1;
                        ResultSet rsu = session
                                    .executeAsync(psUpdateTotalClicks.bind(clickTotalPerformed1, clickDailyPerformed1, campaign.getId(), clickTotalPerformed, clickDailyPerformed))
                                    .get();
                        casSuccess = rsu.wasApplied();
                    }
                    m--;
                }

                if (casSuccess && retval != ClickRegisterState.OVER_BUDGET) {
                    AdserverBudget budget = readCurrent(campaign.getId());
                    AdserverBudget newVal = budget.copy();

                    m = maxRetries;
                    success = false;
                    while (!(success = updateBudget(campaign.getId(), newVal.addClick(), budget, currentAdserver)) && m > 0) {
                        budget = readCurrent(campaign.getId());
                        newVal = budget.copy();
                        m--;
                    }

                    if (success) {
                        retval = ClickRegisterState.NORMAL;
                    } else {
                        counterManager.incrementCounter("bm.maxRetries.registerClick." + campaign.getId());
                        LOG.warning("Unable to register click, max retries failed: " + maxRetries);
                    }
                } else {
                    retval = ClickRegisterState.OVER_BUDGET;
                }
            } else {
                retval = ClickRegisterState.DUPLICATE;
            }
        } catch (Exception t) {
            errorsOccured.incrementAndGet();
            counterManager.incrementCounter("bm.registerClick.error." + t.getMessage());
            LOG.warning("Unable to perform register click: " + t.getClass().getName() + ":" + t.getMessage());
        }

        return retval;
    }

    private void initialize(ConsistencyLevel write) {

        ConsistencyLevel read = ConsistencyLevel.ONE;

        if (psRead == null) {
            psRead = session.prepare(READ_CURRENT);
            psRead.setConsistencyLevel(read);
        }
        if (psUpdate == null) {
            psUpdate = session.prepare(COND_UPDATE);
            psUpdate.setConsistencyLevel(write);
        }
        if (psSelectAdserverOpenBid == null) {
            psSelectAdserverOpenBid = session.prepare(SELECT_ADSERVER_OPEN_BID);
            psSelectAdserverOpenBid.setConsistencyLevel(read);
        }
        if (psReadOpenBid == null) {
            psReadOpenBid = session.prepare(READ_OPEN_BID);
            psReadOpenBid.setConsistencyLevel(read);
        }
        if (psInsertOpenBid == null) {
            psInsertOpenBid = session.prepare(INSERT_OPEN_BID);
            psInsertOpenBid.setConsistencyLevel(write);
        }
        if (psProbLossOpenBid == null) {
            psProbLossOpenBid = session.prepare(PROB_LOSS_OPEN_BID);
            psProbLossOpenBid.setConsistencyLevel(write);
        }
        if (psDeleteOpenBid == null) {
            psDeleteOpenBid = session.prepare(DELETE_OPEN_BID);
            psDeleteOpenBid.setConsistencyLevel(write);
        }

        if (psInsertDedupClick == null) {
            psInsertDedupClick = session.prepare(INSERT_DEDUP_CLICK);
            psInsertDedupClick.setConsistencyLevel(write);
        }

        if (psSelectDedupClick == null) {
            psSelectDedupClick = session.prepare(SELECT_DEDUP_CLICK);
            psSelectDedupClick.setConsistencyLevel(read);
        }

        if (psReadTotalClicks == null) {
            psReadTotalClicks = session.prepare(READ_TOTAL_CLICKS);
            psReadTotalClicks.setConsistencyLevel(read);
        }

        if (psUpdateTotalClicks == null) {
            psUpdateTotalClicks = session.prepare(COND_UPDATE_TOTAL_CLICKS);
            psUpdateTotalClicks.setConsistencyLevel(write);
        }
    }

    private boolean updateBudget(long campaignId, AdserverBudget newVal, AdserverBudget current, String adserver) {
        boolean success = false;

        try {
            ResultSet rs = session.execute(psUpdate.bind(newVal.current, newVal.reserved, newVal.spent, newVal.clicks, newVal.cpcClicks, campaignId, adserver, current.current,
                    current.reserved, current.spent, current.clicks, current.cpcClicks));
            success = rs.wasApplied();
        } catch (QueryExecutionException qe) {
            errorsOccured.incrementAndGet();
            counterManager.incrementCounter("bm.updateBudget.error." + qe.getMessage());
        }

        if (!success) {
            counterManager.incrementCounter("bm.maxRetries.updateBudget." + campaignId);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Unable to update budget for " + currentAdserver + " and campaign: " + campaignId);
            }
        }

        return success;
    }

    public AdserverBudget readCurrent(long campaignId) {
        return readCurrent(campaignId, currentAdserver);
    }

    public AdserverBudget readCurrent(long campaignId, String adserver) {
        AdserverBudget retval = new AdserverBudget();

        try {
            // current, reserved, spent, click, cpcClicks
            ResultSet rs = session.execute(psRead.bind(campaignId, adserver));
            List<Row> rows = rs.all();
            if (!rows.isEmpty()) {
                Row row = rows.get(0);
                retval.current = row.getDecimal("current");
                retval.reserved = row.getDecimal("reserved");
                retval.spent = row.getDecimal("spent");
                retval.clicks = row.getDecimal("click");
                retval.cpcClicks = getLongOrNull(row, "cpcClicks");
            } else {
                counterManager.incrementCounter("bm.readCurrent.unknownAdserverCampaign." + campaignId);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Unable to retrieve budget for " + currentAdserver + " and campaign: " + campaignId);
                }
            }
        } catch (QueryExecutionException qe) {
            counterManager.incrementCounter("bm.readCurrent.error." + qe.getMessage());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Unable to retrieve budget for " + currentAdserver + " and campaign: " + campaignId + " qe: " + qe.getMessage());
            }
        }

        return retval;
    }

    private BigDecimal calcEffectiveValue(BudgetType budgetType, BigDecimal value) {
        BigDecimal amt = BigDecimal.ZERO;
        switch (budgetType) {
        case MONETARY:
        case CLICKS:
            amt = value.divide(THOUSAND).round(MC);
            break;
        case IMPRESSIONS:
            amt = BigDecimal.ONE;
            break;
        }

        return amt;
    }
    
    private Long calcEffectiveValueAsLong(BudgetType budgetType, BigDecimal value) {
        Long amt = 0L;
        
        switch (budgetType) {
        case CLICKS:
        case MONETARY:
            amt = value.multiply(THOUSAND).round(MC).longValue();
            break;
        case IMPRESSIONS:
            amt = 1L;
            break;
        }

        return amt;
    }

    public class CampaignBudget {
        private long campaignId;
        private String adserver;
        private BigDecimal reserved;
        private BigDecimal reservedLoss = BigDecimal.ZERO;
        private String reference;
        private BudgetType budgetType;
        private long timestamp = System.currentTimeMillis();

        public CampaignBudget() {
        }

        public CampaignBudget(String reference, CampaignDto campaign, BigDecimal value) {
            this.reference = reference;
            this.campaignId = campaign.getId();
            this.budgetType = campaign.getCurrentBid().getBudgetType();
            this.reserved = value;
            this.adserver = currentAdserver;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public long getCampaignId() {
            return campaignId;
        }

        public void setCampaignId(long campaignId) {
            this.campaignId = campaignId;
        }

        public BigDecimal getReserved() {
            return reserved;
        }

        public void setReserved(BigDecimal reserved) {
            this.reserved = reserved;
        }

        public BudgetType getBudgetType() {
            return budgetType;
        }

        public void setBudgetType(BudgetType budgetType) {
            this.budgetType = budgetType;
        }

        public String getAdserver() {
            return adserver;
        }

        public void setAdserver(String adserver) {
            this.adserver = adserver;
        }
    }

    @Override
    public boolean isRecentlyUnderfunded(CampaignDto campaign) {
        boolean retval = false;

        if (errorsOccured.get() > maxErrors) {
            retval = true;
        } else {
            Long t = recentUnderfunded.get().get(campaign.getId());
            if (t != null) {
                retval = t + underfundedTimeoutMs > System.currentTimeMillis();
                if (!retval) {
                    recentUnderfunded.get().remove(campaign.getId());
                }
            }
        }

        return retval;
    }

    public class AdserverBudget {
        public BigDecimal current = BigDecimal.ZERO;
        public BigDecimal reserved = BigDecimal.ZERO;
        public BigDecimal spent = BigDecimal.ZERO;
        public BigDecimal clicks = BigDecimal.ZERO;
        public Long cpcClicks = 0L;

        public AdserverBudget reserve(BigDecimal value) {
            this.current = this.current.subtract(value);
            this.reserved = this.reserved.add(value);
            return this;
        }

        public AdserverBudget copy() {
            AdserverBudget c = new AdserverBudget();
            c.current = current;
            c.reserved = reserved;
            c.spent = spent;
            c.clicks = clicks;
            c.cpcClicks = cpcClicks;
            return c;
        }

        public AdserverBudget spend(BigDecimal reserved, BigDecimal spent, BigDecimal reservedLoss) {
            BigDecimal totalBid = reserved.add(reservedLoss);
            this.reserved = this.reserved.subtract(reserved);
            this.spent = this.spent.add(spent);
            if (totalBid.subtract(spent).signum() > 0) {
                this.current = this.current.add(totalBid.subtract(spent).round(MC));
            } else if (reservedLoss.signum() > 0) {
                this.current = this.current.subtract(reservedLoss.round(MC));
            }

            return this;
        }

        public AdserverBudget refund(BigDecimal reserved) {
            this.current = this.current.add(reserved);
            this.reserved = this.reserved.subtract(reserved);
            return this;
        }

        public AdserverBudget addClick() {
            if (this.clicks == null) {
                this.clicks = BigDecimal.ONE;
            } else {
                this.clicks = this.clicks.add(BigDecimal.ONE);
            }

            if (this.cpcClicks == null) {
                this.cpcClicks = 1L;
            } else {
                this.cpcClicks = this.cpcClicks + 1;
            }
            return this;
        }
    }

    class TimeoutCleanupThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(errorTrackTimeframeMs);
                errorsOccured.set(0);

                while (true) {
                    try {
                        List<CampaignBudget> campaigns = selectAdserverOpenBids(currentAdserver);

                        for (CampaignBudget cb : campaigns) {
                            if (cb.timestamp < System.currentTimeMillis() - longBidTimeoutMs && cb.reservedLoss.signum() == 0) {
                                BigDecimal probLoss = cb.reserved.multiply(longBidLossProbability);
                                if (retryReleaseBudget(cb, probLoss) > 0) {
                                    counterManager.incrementCounter("bm.cleanup.probLoss");
                                    updateProbLossInOpenBid(cb, probLoss);
                                }
                            }
                            if (cb.timestamp < System.currentTimeMillis() - failBidTimeoutMs) {
                                if (retryReleaseBudget(cb, cb.reserved) > 0) {
                                    counterManager.incrementCounter("bm.cleanup.failedLoss");
                                    deleteOpenBid(cb.reference);
                                }
                            }
                        }

                    } catch (Throwable t) {
                        counterManager.incrementCounter("bm.cleanup.error." + t.getMessage());
                        LOG.warning("Unable to clean up reserved loss budget: " + t.getClass().getName() + ":" + t.getMessage());
                    }
                    Thread.sleep(errorTrackTimeframeMs);
                    errorsOccured.set(0);
                }

            } catch (InterruptedException e) {
                LOG.warning("Unable to wait" + e.getMessage());
            }
        }
    }

}