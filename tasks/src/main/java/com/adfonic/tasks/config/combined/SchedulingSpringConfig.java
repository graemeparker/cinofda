package com.adfonic.tasks.config.combined;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.adfonic.tasks.combined.AdSpaceDormantizer;
import com.adfonic.tasks.combined.AdvertiserBalanceTasks;
import com.adfonic.tasks.combined.BlacklistPublicationsTask;
import com.adfonic.tasks.combined.CampaignBudgetTasks;
import com.adfonic.tasks.combined.CleanupCompletedCampaigns;
import com.adfonic.tasks.combined.DynaPropertiesReloader;
import com.adfonic.tasks.combined.ExchangeRatesAutoFeedTask;
import com.adfonic.tasks.combined.ExpiredNotificationDeleter;
import com.adfonic.tasks.combined.FxRateTasks;
import com.adfonic.tasks.combined.MetamarketsLookupTableTask;
import com.adfonic.tasks.combined.ScheduledCampaignTasks;
import com.adfonic.tasks.combined.StatusChangeProcessor;
import com.adfonic.tasks.combined.Unstopper;
import com.adfonic.tasks.combined.tracker.ConversionRetry;
import com.adfonic.tasks.combined.tracker.InstallRetry;
import com.adfonic.tasks.combined.tracker.VideoViewRetry;
import com.adfonic.tasks.combined.truste.TrusteSyncTasks;
import com.adfonic.tasks.xaudit.impl.CreativeAuditStatusTask;

/**
 * @author mvanek
 * 
 * I prefer to have all scheduled services configured in one place rather than have them scattered across codebase
 * 
 * Note that all scheduled tasks are combined so make sure they are NOT started in simple tasks context
 * 
 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html
 */
@Configuration
@EnableScheduling
public class SchedulingSpringConfig implements SchedulingConfigurer {

    private static final String INITIAL_DELAY = "${tasks.initial.delay:5000}";

    private static final int SECOND = 1000;
    private static final int FIVE_SECONDS = 5 * SECOND; // 5000
    private static final int MINUTE = 60 * SECOND; // 60000
    private static final int FIVE_MINUTES = 5 * MINUTE; // 300000
    private static final int FIFTEEN_MINUTES = 15 * MINUTE; // 900000
    private static final int ONE_HOUR = 60 * MINUTE; //3600000

    @Autowired
    TrusteSyncTasks trusteSyncTasks;

    @Autowired
    VideoViewRetry videoViewRetry;

    @Autowired
    InstallRetry installRetry;

    @Autowired
    ConversionRetry conversionRetry;

    @Autowired
    FxRateTasks fxRateTasks;

    @Autowired
    ExchangeRatesAutoFeedTask exchangeRatesAutoFeedTask;

    @Autowired
    Unstopper unstopper;

    @Autowired
    StatusChangeProcessor statusChangeProcessor;

    @Autowired
    ScheduledCampaignTasks scheduledCampaignTasks;

    @Autowired
    MetamarketsLookupTableTask metamarketsLookupTableTask;

    @Autowired
    ExpiredNotificationDeleter expiredNotificationDeleter;

    @Autowired
    CleanupCompletedCampaigns cleanupCompletedCampaigns;

    @Autowired
    CampaignBudgetTasks campaignBudgetTasks;

    @Autowired
    AdvertiserBalanceTasks advertiserBalanceTasks;

    @Autowired
    AdSpaceDormantizer adSpaceDormantizer;

    @Autowired
    DynaPropertiesReloader dynaPropertiesReloader;

    @Autowired
    BlacklistPublicationsTask blacklistTask;

    @Autowired
    CreativeAuditStatusTask creativeAuditStatusTask;

    ScheduledTaskRegistrar taskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        this.taskRegistrar = taskRegistrar;
    }

    public ScheduledTaskRegistrar getTaskRegistrar() {
        return taskRegistrar;
    }

    @Scheduled(fixedDelay = ONE_HOUR + 500, initialDelayString = INITIAL_DELAY)
    public void trusteSyncTasks() {
        trusteSyncTasks.process();
    }

    @Scheduled(fixedRate = FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void processPendingVideoViews() {
        videoViewRetry.processPendingVideoViews();
    }

    @Scheduled(fixedRate = FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void processPendingInstalls() {
        installRetry.processPendingInstalls();
    }

    @Scheduled(fixedRate = FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void processPendingAuthenticatedInstalls() {
        installRetry.processPendingAuthenticatedInstalls();
    }

    @Scheduled(fixedRate = FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void processPendingConversions() {
        conversionRetry.processPendingConversions();
    }

    @Scheduled(fixedRate = 2 * FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void handleUnstoppages() {
        unstopper.handleUnstoppages();
    }

    @Scheduled(fixedRate = 2 * FIVE_SECONDS, initialDelayString = INITIAL_DELAY)
    public void statusChangeProcessor() {
        statusChangeProcessor.runPeriodically();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 800, initialDelayString = INITIAL_DELAY)
    public void doCompleteScheduledCampaigns() {
        scheduledCampaignTasks.doCompleteScheduledCampaigns();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 900, initialDelayString = INITIAL_DELAY)
    public void doNotifyLiveCampaigns() {
        scheduledCampaignTasks.doNotifyLiveCampaigns();
    }

    @Scheduled(fixedRate = ONE_HOUR, initialDelayString = INITIAL_DELAY)
    public void metamarketsLookupTableTask() {
        metamarketsLookupTableTask.runPeriodically();
    }

    @Scheduled(fixedRate = ONE_HOUR + 500, initialDelayString = INITIAL_DELAY)
    public void doFetchFxRate() {
        fxRateTasks.doFetchRate();
    }

    @Scheduled(cron = "${exchangeratesautofeed.cronschedule:0 0 0/1 * * ?}")
    public void exchangeRatesAutoFeedTask() {
        exchangeRatesAutoFeedTask.doTask();
    }

    @Scheduled(fixedRate = ONE_HOUR + 100, initialDelayString = INITIAL_DELAY)
    public void deleteExpiredNotifications() {
        expiredNotificationDeleter.deleteExpiredNotifications();
    }

    @Scheduled(fixedRate = ONE_HOUR, initialDelayString = INITIAL_DELAY)
    public void cleanupCompletedCampaigns() {
        cleanupCompletedCampaigns.runPeriodically();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 700, initialDelayString = INITIAL_DELAY)
    public void doOverallBudgetReset() {
        campaignBudgetTasks.doOverallBudgetReset();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 500, initialDelayString = INITIAL_DELAY)
    public void doNotifyForOverallBudget() {
        campaignBudgetTasks.doNotifyForOverallBudget();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 400, initialDelayString = INITIAL_DELAY)
    public void doNotifyForDailyBudget() {
        campaignBudgetTasks.doNotifyForDailyBudget();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 600, initialDelayString = INITIAL_DELAY)
    public void doDailyBudgetReset() {
        campaignBudgetTasks.doDailyBudgetReset();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 100, initialDelayString = INITIAL_DELAY)
    public void doNotifyForLowBalance() {
        advertiserBalanceTasks.doNotifyForLowBalance();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 200, initialDelayString = INITIAL_DELAY)
    public void doNotifyForZeroBalance() {
        advertiserBalanceTasks.doNotifyForZeroBalance();
    }

    @Scheduled(fixedRate = FIVE_MINUTES + 300, initialDelayString = INITIAL_DELAY)
    public void doResetLowOrZeroBalanceFlags() {
        advertiserBalanceTasks.doReset();
    }

    @Scheduled(fixedRate = 3 * ONE_HOUR, initialDelayString = INITIAL_DELAY)
    public void adSpaceDormantizer() {
        adSpaceDormantizer.runPeriodically();
    }

    @Scheduled(fixedRate = FIVE_MINUTES, initialDelayString = INITIAL_DELAY)
    public void dynaPropertiesReload() {
        dynaPropertiesReloader.reload();
    }

    @Scheduled(fixedRate = 8 * ONE_HOUR, initialDelayString = INITIAL_DELAY)
    public void publicationBlacklist() {
        blacklistTask.execute();
    }

    @Scheduled(fixedRate = FIFTEEN_MINUTES, initialDelayString = INITIAL_DELAY)
    public void creativeAuditStatusTask() {
        creativeAuditStatusTask.onScheduled();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setDaemon(true);
        executor.setThreadNamePrefix("tasks-schdlr-");
        executor.setPoolSize(20);
        return executor;
    }
}
