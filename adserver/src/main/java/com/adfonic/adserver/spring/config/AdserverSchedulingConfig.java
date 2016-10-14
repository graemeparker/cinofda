package com.adfonic.adserver.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.adfonic.data.cache.AdserverDataCacheManagerImpl;
import com.adfonic.ddr.deviceatlas.DdrDeviceAtlasS3ServiceImpl;

/**
 * 
 * @author mvanek
 * 
 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html
 */
@Configuration
@EnableScheduling
public class AdserverSchedulingConfig implements SchedulingConfigurer {


    @Autowired
    private AdserverDataCacheManagerImpl dataCacheManager;

    @Autowired
    private DdrDeviceAtlasS3ServiceImpl deviceAtlas;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Scheduled(cron = "${ddr.deviceatlas.s3.cronschedule:0 0/5 * * * ?}")
    public final void deviceAtlas() {
        deviceAtlas.loadData();
    }

    @Scheduled(fixedRate = 180000, initialDelay = 10000)
    public void dataCacheManagerReload() {
        dataCacheManager.process();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setDaemon(true);
        executor.setThreadNamePrefix("adsrv-schdlr-");
        executor.setPoolSize(10);
        return executor;
    }
}
