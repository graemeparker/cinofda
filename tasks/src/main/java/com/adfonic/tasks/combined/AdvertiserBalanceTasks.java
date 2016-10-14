package com.adfonic.tasks.combined;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserNotificationFlag;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company_;
import com.adfonic.util.CurrencyUtils;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

/**
 * Various advertiser balance related tasks
 */
@Component
public class AdvertiserBalanceTasks extends TemplatedEmailSendingTask {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private static final FetchStrategy ADVERTISER_FS = new FetchStrategyBuilder().addInner(Advertiser_.company).addInner(Advertiser_.account).addInner(Company_.accountManager)
            .build();

    @Autowired
    private AdvertiserManager advertiserManager;

    private final String lowBalanceSubject;
    private final String lowBalanceTemplateText;
    private final String lowBalanceAgencyTemplateText;

    private final String zeroBalanceSubject;
    private final String zeroBalanceTemplateText;
    private final String zeroBalanceAgencyTemplateText;

    @Autowired
    public AdvertiserBalanceTasks(@Value("${AdvertiserBalanceNotifier.low.subject}") String lowBalanceSubject,
            @Value("${AdvertiserBalanceNotifier.zero.subject}") String zeroBalanceSubject) throws java.io.IOException {
        this.lowBalanceSubject = lowBalanceSubject;
        this.lowBalanceTemplateText = loadResource("/templates/advertiser_low_balance_notification.html");
        this.lowBalanceAgencyTemplateText = loadResource("/templates/advertiser_low_balance_notification_agency.html");
        this.zeroBalanceSubject = zeroBalanceSubject;
        this.zeroBalanceTemplateText = loadResource("/templates/advertiser_zero_balance_notification.html");
        this.zeroBalanceAgencyTemplateText = loadResource("/templates/advertiser_zero_balance_notification_agency.html");
    }

    /**
     * Notify advertisers for low balance
     */
    //@Scheduled(fixedRate=300100)
    public void doNotifyForLowBalance() {
        LOG.debug("Starting low balance notification");

        for (Advertiser advertiser : advertiserManager.getAdvertisersToNotifyForLowBalance(ADVERTISER_FS)) {
            LOG.info("Notifying for low balance on Advertiser id={}", advertiser.getId());

            Account account = advertiser.getAccount();

            Map<String, Object> templateParams = new HashMap<String, Object>();
            templateParams.put("account", account);
            templateParams.put("notifyLimit", CurrencyUtils.CURRENCY_FORMAT_USD.format(advertiser.getNotifyLimit()));
            templateParams.put("balance", CurrencyUtils.CURRENCY_FORMAT_USD.format(account.getBalance()));

            String templateText;
            if (advertiser.getCompany().isAccountType(AccountType.AGENCY)) {
                templateText = lowBalanceAgencyTemplateText;
            } else {
                templateText = lowBalanceTemplateText;
            }

            try {
                sendEmailToAdvertiser(advertiser, lowBalanceSubject, templateText, templateParams);
            } catch (com.adfonic.email.EmailException e) {
                LOG.error("Failed to send email to Advertiser id={} {}", advertiser.getId(), e);
                continue;
            }

            // This notification expires in 24 hours
            Date expirationDate = org.apache.commons.lang.time.DateUtils.addDays(new Date(), 1);
            LOG.debug("Creating low balance AdvertiserNotificationFlag for Advertiser id={}, expirationDate={}", advertiser.getId(), expirationDate);
            // This notification expires in 24 hours
            advertiserManager.newAdvertiserNotificationFlag(advertiser, AdvertiserNotificationFlag.Type.LOW_BALANCE, expirationDate);
        }

        LOG.debug("Finished low balance notification");
    }

    /**
     * Notify advertisers for zero balance
     */
    //@Scheduled(fixedRate=300200)
    public void doNotifyForZeroBalance() {
        LOG.debug("Starting zero balance notification");

        for (Advertiser advertiser : advertiserManager.getAdvertisersToNotifyForZeroBalance(ADVERTISER_FS)) {
            LOG.info("Notifying for zero balance on Advertiser id={}", advertiser.getId());

            Account account = advertiser.getAccount();

            Map<String, Object> templateParams = new HashMap<String, Object>();
            templateParams.put("account", account);
            templateParams.put("notifyLimit", CurrencyUtils.CURRENCY_FORMAT_USD.format(advertiser.getNotifyLimit()));
            //templateParams.put("balance", CurrencyUtils.CURRENCY_FORMAT_USD.format(account.getBalance()));

            String templateText;
            if (advertiser.getCompany().isAccountType(AccountType.AGENCY)) {
                templateText = zeroBalanceAgencyTemplateText;
            } else {
                templateText = zeroBalanceTemplateText;
            }
            try {
                sendEmailToAdvertiser(advertiser, zeroBalanceSubject, templateText, templateParams);
            } catch (com.adfonic.email.EmailException e) {
                LOG.error("Failed to send email to Advertiser id={} {}", advertiser.getId(), e);
                continue;
            }

            // This notification never expires (last arg is the expiration date).
            // It will be reset if the account balance goes above zero.
            LOG.debug("Creating zero balance AdvertiserNotificationFlag for Advertiser id={}, expirationDate=null", advertiser.getId());
            advertiserManager.newAdvertiserNotificationFlag(advertiser, AdvertiserNotificationFlag.Type.ZERO_BALANCE, null);
        }

        LOG.debug("Finished zero balance notification");
    }

    /**
     * Reset any advertiser notifications pertaining to low or zero balance
     */
    //@Scheduled(fixedRate=300300)
    public void doReset() {
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

        LOG.debug("Starting low/zero balance reset");

        try {
            advertiserManager.resetLowOrZeroBalanceNotificationFlagsAsApplicable();
        } catch (Exception e) {
            LOG.error("Failed to reset low/zero balance notification flags {}", e);
        }

        LOG.debug("Finished low/zero balance reset");
    }
}
