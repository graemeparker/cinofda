package com.adfonic.datacollector.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.domain.StopAction;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;

@Component
public class AccountingDao extends AbstractDao {

    private static final transient Logger LOG = Logger.getLogger(AccountingDao.class.getName());

    public static final class UpdateBudgetsResult {
        private final BigDecimal adjustedAdvertiserSpend;
        private Set<StopAction> stopActions;

        public UpdateBudgetsResult(BigDecimal adjustedAdvertiserSpend) {
            this.adjustedAdvertiserSpend = adjustedAdvertiserSpend;
        }

        public BigDecimal getAdjustedAdvertiserSpend() {
            return adjustedAdvertiserSpend;
        }

        public Set<StopAction> getStopActions() {
            return stopActions;
        }

        public void addStopAction(StopAction stopAction) {
            if (stopActions == null) {
                stopActions = new LinkedHashSet<StopAction>();
            }
            stopActions.add(stopAction);
        }

        @Override
        public String toString() {
            return "[adjustedAdvertiserSpend=" + adjustedAdvertiserSpend + ",stopActions=" + stopActions + "]";
        }
    }

    @Autowired
    public AccountingDao(@Qualifier("accountingDataSource") DataSource dataSource) {
        super(dataSource);
    }

    public UpdateBudgetsResult updateBudgets(CampaignDto campaign, PublisherDto publisher, BigDecimal advertiserSpend, BigDecimal publisherCreditMultiplier, int advertiserDateId,
            int impressionsCount, int clicksCount, int conversionsCount) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Campaign id=" + campaign.getId() + ", Publisher id=" + publisher.getId() + ", advertiserDateId=" + advertiserDateId + ", advertiserSpend=" + advertiserSpend
                    + ", publisherCreditMultiplier=" + publisherCreditMultiplier + ", impressionsCount=" + impressionsCount + ", clicksCount=" + clicksCount
                    + ", conversionsCount=" + conversionsCount);
        }

        CallableStatement cs = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(false); // use explicit transactions

            // Set up and call the stored proc
            cs = conn.prepareCall("{CALL UPDATE_BUDGETS_NEW(?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setLong(1, campaign.getId());
            cs.setLong(2, campaign.getAdvertiser().getId());
            cs.setLong(3, campaign.getAdvertiser().getAccountId());
            cs.setLong(4, publisher.getAccountId());
            cs.setInt(5, advertiserDateId);
            cs.setBigDecimal(6, advertiserSpend);
            cs.registerOutParameter(6, java.sql.Types.DECIMAL);
            cs.setBigDecimal(7, publisherCreditMultiplier);
            cs.registerOutParameter(8, java.sql.Types.VARCHAR);
            cs.setInt(9, impressionsCount);
            cs.setInt(10, clicksCount);
            cs.setInt(11, conversionsCount);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Calling UPDATE_BUDGETS_NEW(" + campaign.getId() + "," + campaign.getAdvertiser().getId() + "," + campaign.getAdvertiser().getAccountId() + ","
                        + publisher.getAccountId() + "," + advertiserDateId + "," + advertiserSpend + "," + publisherCreditMultiplier + "," + "?," + impressionsCount + ","
                        + clicksCount + "," + conversionsCount + ")");
            }
            cs.execute();

            conn.commit();

            // Grab the output values...snag the adjusted advertiserSpend, which
            // indicates the actual value by which we incremented the advertiser's
            // account balance.
            UpdateBudgetsResult result = new UpdateBudgetsResult(cs.getBigDecimal(6));

            // And snag the appropriate StopActions
            final String stopActionsStr = cs.getString(8);
            if (StringUtils.isNotEmpty(stopActionsStr)) {
                for (String name : StringUtils.split(stopActionsStr, ',')) {
                    result.addStopAction(StopAction.valueOf(name));
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For Campaign id=" + campaign.getId() + ", Publisher id=" + publisher.getId() + ", result=" + result);
            }
            return result;
        } finally {
            DbUtils.closeQuietly(conn, cs, null);
        }
    }

    /**
     * Increment a publisher's balance by a given amount
     */
    public void incrementPublisherBalance(PublisherDto publisher, BigDecimal amount) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Incrementing Publisher id=" + publisher.getId() + ", Account id=" + publisher.getAccountId() + ", amount=" + amount);
        }
        PreparedStatement ps = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(false); // use explicit transactions

            // We used to call a stored proc, here, but that was a wasted
            // abstraction, since all it did was this update...and it did
            // a subquery to boot when finding the company id.  Let's just
            // do one simple not-likely-to-deadlock update.
            ps = conn.prepareStatement("UPDATE ACCOUNT SET BALANCE=BALANCE + ? WHERE ID=?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, publisher.getAccountId());
            ps.executeUpdate();

            conn.commit();
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }
    }
}
