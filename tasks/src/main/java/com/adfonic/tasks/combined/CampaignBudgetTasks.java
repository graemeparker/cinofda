package com.adfonic.tasks.combined;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.BudgetSpend;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.CampaignOverallSpend;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.util.CurrencyUtils;
import com.adfonic.util.DateUtils;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

/**
 * Various campaign budget related tasks
 */
@Component
public class CampaignBudgetTasks extends TemplatedEmailSendingTask {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private static final FetchStrategy CAMPAIGN_FS = new FetchStrategyBuilder().addInner(Campaign_.advertiser).addInner(Campaign_.overallSpend).addInner(Advertiser_.company)
            .addInner(Company_.accountManager).build();

    @Autowired
    private CampaignManager campaignManager;

    @Autowired
    private BiddingManager biddingManager;

    private final String dailyBudgetSubject;
    private final String dailyBudgetTemplateText;
    private final String dailyBudgetAgencyTemplateText;

    private final String overallBudgetSubject;
    private final String overallBudgetTemplateText;
    private final String overallBudgetAgencyTemplateText;

    @Autowired
    public CampaignBudgetTasks(@Value("${CampaignBudgetNotifier.daily.subject}") String dailyBudgetSubject,
            @Value("${CampaignBudgetNotifier.overall.subject}") String overallBudgetSubject) throws java.io.IOException {
        this.dailyBudgetSubject = dailyBudgetSubject;
        this.dailyBudgetTemplateText = loadResource("/templates/campaign_daily_budget_notification.html");
        this.dailyBudgetAgencyTemplateText = loadResource("/templates/campaign_daily_budget_notification_agency.html");
        this.overallBudgetSubject = overallBudgetSubject;
        this.overallBudgetTemplateText = loadResource("/templates/campaign_overall_budget_notification.html");
        this.overallBudgetAgencyTemplateText = loadResource("/templates/campaign_overall_budget_notification_agency.html");
    }

    /**
     * Notify advertisers about campaigns which have reached their daily spending limit
     */
    //@Scheduled(fixedRate=300400)
    public void doNotifyForDailyBudget() {
        LOG.debug("Starting daily budget notification");

        for (Campaign campaign : biddingManager.getCampaignsToNotifyForDailyBudget(CAMPAIGN_FS)) {
            Advertiser advertiser = campaign.getAdvertiser();
            LOG.info("Notifying Advertiser id={} for daily budget on Campaign id={}", advertiser.getId(), campaign.getId());

            Company company = advertiser.getCompany();
            Calendar calendar = Calendar.getInstance(company.getDefaultTimeZone());
            Date now = calendar.getTime();
            BudgetSpend spent = campaignManager.getBudgetSpendForCampaign(campaign, now);
            //Account account = advertiser.getAccount();
            BigDecimal remaining = spent.getBudget().subtract(spent.getAmount());

            Map<String, Object> templateParams = new HashMap<String, Object>();
            templateParams.put("campaign", campaign);
            //templateParams.put("threshold", "80%");
            templateParams.put("spent", CurrencyUtils.CURRENCY_FORMAT_USD.format(spent.getAmount()));
            templateParams.put("remaining", CurrencyUtils.CURRENCY_FORMAT_USD.format(remaining));
            //templateParams.put("account", account);

            String templateText;
            if (company.isAccountType(AccountType.AGENCY)) {
                templateText = dailyBudgetAgencyTemplateText;
            } else {
                templateText = dailyBudgetTemplateText;
            }

            try {
                sendEmailToAdvertiser(advertiser, dailyBudgetSubject, templateText, templateParams);
            } catch (com.adfonic.email.EmailException e) {
                LOG.error("Failed to send email to Advertiser id={} for Campaign id={} {}", advertiser.getId(), campaign.getId(), e);
                continue;
            }

            // The notification expires at midnight in the advertiser's time zone
            Date expirationDate = DateUtils.getStartOfDayTomorrow(new Date(), company.getDefaultTimeZone());
            LOG.debug("Creating daily budget CampaignNotificationFlag for Campaign id={}, Company id={}, expirationDate={}", campaign.getId(), company.getId(), expirationDate);
            campaignManager.newCampaignNotificationFlag(campaign, CampaignNotificationFlag.Type.DAILY_BUDGET, expirationDate);
        }

        LOG.debug("Finished daily budget notification");
    }

    /**
     * Notify advertisers about campaigns which have reached their overall spending limit
     */
    //@Scheduled(fixedRate=300500)
    public void doNotifyForOverallBudget() {
        LOG.debug("Starting overall campaign budget notification");

        for (Campaign campaign : biddingManager.getCampaignsToNotifyForOverallBudget(CAMPAIGN_FS)) {
            Advertiser advertiser = campaign.getAdvertiser();
            LOG.info("Notifying Advertiser id={} for overall budget on Campaign id={}", advertiser.getId(), campaign.getId());

            CampaignOverallSpend overallSpend = campaign.getOverallSpendNoSeriouslyGetOverallSpend();
            //Account account = advertiser.getAccount();
            BigDecimal remaining = overallSpend.getBudget().subtract(overallSpend.getAmount());

            Map<String, Object> templateParams = new HashMap<String, Object>();
            templateParams.put("campaign", campaign);
            //templateParams.put("threshold", "80%");
            templateParams.put("spent", CurrencyUtils.CURRENCY_FORMAT_USD.format(overallSpend.getAmount()));
            templateParams.put("remaining", CurrencyUtils.CURRENCY_FORMAT_USD.format(remaining));
            //templateParams.put("account", account);

            String templateText;
            if (advertiser.getCompany().isAccountType(AccountType.AGENCY)) {
                templateText = overallBudgetAgencyTemplateText;
            } else {
                templateText = overallBudgetTemplateText;
            }

            try {
                sendEmailToAdvertiser(advertiser, overallBudgetSubject, templateText, templateParams);
            } catch (com.adfonic.email.EmailException e) {
                LOG.error("Failed to send email to Advertiser id={} for Campaign id={} {}", advertiser.getId(), campaign.getId(), e);
                continue;
            }

            // This notification never expires.  It will be reset if the campaign's overall budget is ever increased.
            LOG.debug("Creating overall budget NotificationFlag for Campaign id={}, expirationDate=null", campaign.getId());
            campaignManager.newCampaignNotificationFlag(campaign, CampaignNotificationFlag.Type.OVERALL_BUDGET, null);
        }

        LOG.debug("Finished overall campaign budget notification");
    }

    /**
     * Reset daily budget notifications that no longer apply
     */
    //@Scheduled(fixedRate=300600)
    public void doDailyBudgetReset() {
        // This helps avoid deadlocks when combined starts up.  There are
        // at least 6 separate tasks that manipulate NOTIFICATION_FLAG,
        // and they can all contend if they run simultaneously.  Once
        // combined has been running a while, the sub-second fudge factors
        // on the fixedRate=... kick in, but this helps at startup when
        // everything runs all at once.  We can remove this hack once this
        // Spring enhancement goes in, and we use fixedDelay:
        // https://jira.springsource.org/browse/SPR-7022
        try {
            Thread.sleep(new Random().nextInt(2000));
        } catch (InterruptedException e) {
        }

        LOG.debug("Starting daily campaign budget reset");

        try {
            biddingManager.resetDailyBudgetNotificationFlagsAsApplicable();
        } catch (Exception e) {
            LOG.error("Failed to delete daily campaign budget notification flags {}", e);
        }

        LOG.debug("Finished daily campaign budget reset");
    }

    /**
     * Reset overall budget notifications that no longer apply
     */
    //@Scheduled(fixedRate=300700)
    public void doOverallBudgetReset() {
        // This helps avoid deadlocks when combined starts up.  There are
        // at least 6 separate tasks that manipulate NOTIFICATION_FLAG,
        // and they can all contend if they run simultaneously.  Once
        // combined has been running a while, the sub-second fudge factors
        // on the fixedRate=... kick in, but this helps at startup when
        // everything runs all at once.  We can remove this hack once this
        // Spring enhancement goes in, and we use fixedDelay:
        // https://jira.springsource.org/browse/SPR-7022
        try {
            Thread.sleep(new Random().nextInt(2000));
        } catch (InterruptedException e) {
        }

        LOG.debug("Starting overall campaign budget reset");

        try {
            biddingManager.resetOverallBudgetNotificationFlagsAsApplicable();
        } catch (Exception e) {
            LOG.error("Failed to delete overall campaign budget notification flags {}", e);
        }

        LOG.debug("Finished overall campaign budget reset");
    }
}
