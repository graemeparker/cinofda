package com.adfonic.tools.beans.transaction;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CampaignTransactionDto;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.CurrencyUtils;

@Component
@Scope("view")
public class TransactionDailyBudgetMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<Campaign.Status> CAMPAIGN_STATUSES = Arrays.asList(new Campaign.Status[] { Status.ACTIVE, Status.PENDING,
            Status.PAUSED });

    @Autowired
    private TransactionService transactionService;

    private BigDecimal totalCampaignBudgets;
    public static final BigDecimal MIN_DAILY_MAX = new BigDecimal(10.00);

    private AdvertiserAccountingDto advertiser;

    /** Enabled. */
    private boolean enabled;

    /** Amount. */
    private BigDecimal amount;

    public TransactionDailyBudgetMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        BigDecimal total = BigDecimal.ZERO;
        advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        if (advertiser != null) {
            for (CampaignTransactionDto c : transactionService.getCampaignsForAdvertiser(getUser().getAdvertiserDto(), false,
                    CAMPAIGN_STATUSES)) {
                BigDecimal db = c.getDailyBudget();
                if (db != null) {
                    total = total.add(db);
                }
            }
            totalCampaignBudgets = total;
            amount = advertiser.getDailyBudget();
        }
        enabled = amount != null;
    }

    public void doSave() {
        if (!enabled) {
            amount = null;
        } else { // enabled
            if (amount == null || BigDecimal.ZERO.equals(amount)) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "amount", null, "error.dailybudget.amount");
                return;
            } else if (amount.compareTo(MIN_DAILY_MAX) < 0) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "amount", null, "error.dailybudget.minimum",
                        CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_DAILY_MAX));
                return;
            }
        }

        // update
        advertiser.setDailyBudget(amount);
        advertiser = transactionService.updateAdvertiser(advertiser);
        RequestContext.getCurrentInstance().execute("dailyBudgetDialog.hide();");

        // if the request is in campaignBid pages
        if (getCampaignBidMBean().getCampaignDto() != null) {
            if (amount != null) {
                getCampaignBidMBean().setCampaignDailyBudget(amount.doubleValue());
            } else {
                getCampaignBidMBean().setCampaignDailyBudget(null);
            }
            RequestContext.getCurrentInstance().update("account-budget");

        }
    }

    public void doCancel(ActionEvent event) throws Exception {
        init();
    }

    public BigDecimal getTotalCampaignBudgets() {
        return totalCampaignBudgets;
    }

    public void setTotalCampaignBudgets(BigDecimal totalCampaignBudgets) {
        this.totalCampaignBudgets = totalCampaignBudgets;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
