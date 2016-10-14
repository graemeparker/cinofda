package com.adfonic.tasks.config.combined;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.util.StringUtils;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.IconManager;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.impl.AdResponseLogicImpl;
import com.adfonic.adserver.impl.AdXAdMarkupGenerator;
import com.adfonic.adserver.impl.AppNexusMarkupGenerator;
import com.adfonic.adserver.impl.DisplayTypeUtilsImpl;
import com.adfonic.adserver.impl.IconManagerImpl;
import com.adfonic.adserver.impl.MarkupGeneratorImpl;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.tasks.combined.consumers.PublisherCreativeHandler;
import com.adfonic.tasks.xaudit.ApprovalServiceManager;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;
import com.adfonic.tasks.xaudit.adx.AdXCreativeApiManager;
import com.adfonic.tasks.xaudit.appnxs.AppNexusApiClient;
import com.adfonic.tasks.xaudit.appnxs.AppNexusAuditService;
import com.adfonic.tasks.xaudit.appnxs.AppNexusCreativeSystem;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.tasks.xaudit.impl.AuditCreativeRenderer;
import com.adfonic.tasks.xaudit.impl.AuditVhostManager;
import com.adfonic.tasks.xaudit.impl.CreativeAuditStatusTask;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.stats.CounterManager;
import com.byyd.middleware.account.dao.PublisherDao;
import com.byyd.middleware.creative.dao.CreativeDao;

/**
 * Replacement for scattered external creative audit part of combined task. 
 * Depends on adfonic-toolsdb-context.xml (toolsDataSource) , adfonic-tasks-context.xml (PublisherManager, CreativeManager) 
 *
 */
@Configuration
@ComponentScan(basePackageClasses = { CreativeDao.class, PublisherDao.class, ApprovalServiceManager.class })
public class ExternalAuditSpringConfig {

    private static final String ADX_P12_FILENAME = "Byyd-AdX-Creative-Approval-64f556239bb8.p12";

    /**
     * Comsume JMS from domain serializer
     */
    @Bean
    PublisherCreativeHandler publisherCreativeHandler(AppNexusAuditService anxService, AdXAuditService adxService) {
        return new PublisherCreativeHandler(anxService, adxService);
    }

    /**
     * Scheduled periodic task 
     */
    @Bean
    CreativeAuditStatusTask creativeAuditStatusTask(AdXAuditService axdService, @Qualifier(ConfUtils.TOOLS_JDBC_TEMPLATE) JdbcTemplate jdbcTools) {
        return new CreativeAuditStatusTask(axdService, jdbcTools);
    }

    @Bean
    AppNexusAuditService appNexusAuditService(@Value("${appnxs.publisherid}") String appNexusPublisherIds) {
        Set<Long> anxPublisherIds = com.adfonic.util.StringUtils.toSetOfLongs(appNexusPublisherIds, ",");
        return new AppNexusAuditService(anxPublisherIds);
    }

    @Bean
    AppNexusApiClient appNexusApiClient(@Value("${appnxs.creative.service.url:dummynottoblockothrstf}") String creativeServiceUrl,
            @Value("${appnxs.auth.url:dummynottoblockothrstf}") String authenticateUrl, //
            @Value("${appnxs.auth.username:dummyusername}") String username, @Value("${appnxs.auth.password:dummypassword}") String password,//
            @Value("${appnxs.creative.service.memberid:2560}") int memberId,//
            @Value("${appnxs.creative.service.connTtlMs:2000}") int connTtlMs,//
            @Value("${appnxs.creative.service.maxTotalConnection:20}") int maxTotal, //
            @Value("${appnxs.creative.service.defaultMaxPerRoute:10}") int defaultMaxPerRoute,//
            @Value("${appnxs.creative.service.connect.timeout.ms:2000}") int connectTimeout, //
            @Value("${appnxs.creative.service.socket.timeout.ms:2000}") int socketTimeout) {
        return new AppNexusApiClient(creativeServiceUrl, authenticateUrl, username, password, memberId, connTtlMs, maxTotal, defaultMaxPerRoute, connectTimeout, socketTimeout);
    }

    @Bean
    AppNexusCreativeSystem appNexusCreativeSystem(@Value("${appnxs.publisherid}") String appNexusPublisherIds, @Value("${appnxs.creative.service.memberid:2560}") int memberId,
            @Value("${asset.service.baseurl}") String assetBaseUrl, ApprovalServiceManager<AppNexusCreativeRecord> appNexusManager) {
        Set<Long> publisherIds = com.adfonic.util.StringUtils.toSetOfLongs(appNexusPublisherIds, ",");
        return new AppNexusCreativeSystem(publisherIds, memberId, assetBaseUrl, appNexusManager);
    }

    @Bean
    AdXAuditService adXAuditService(@Value("${adx.publisherid}") long adxPublisherId, @Value("${adx.audit.displaysizes}") String creativeDisplaySizes,
            @Value("${adx.check.before.submit}") boolean checkCreativeBeforeNewSubmit, @Value("${adx.creative.expirygap.minutes:28800000}") long adXPendingGap) {
        Set<String> sizesSet = StringUtils.commaDelimitedListToSet(creativeDisplaySizes.trim().toLowerCase());
        return new AdXAuditService(adxPublisherId, sizesSet, checkCreativeBeforeNewSubmit, adXPendingGap);
    }

    @Bean
    AdXCreativeApiManager adXCreativeApiManager(@Value("${adx.p12key.path:/usr/local/adfonic/tasks/bin/}") String pathToP12KeyFile,
            @Value("${adx.service.account.email}") String accountEmail, @Value("${adx.creative.account.id}") Integer accountId) throws IOException, GeneralSecurityException {
        File p12file = new File(pathToP12KeyFile + ADX_P12_FILENAME);
        InputStream stream = null;
        if (p12file.exists()) {
            stream = new FileInputStream(p12file);
        } else {
            // try classpath resource fallback (for test)
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                stream = classLoader.getResourceAsStream(ADX_P12_FILENAME);
            } else {
                stream = getClass().getResourceAsStream(ADX_P12_FILENAME);
            }
            if (stream == null) {
                throw new IllegalStateException("Neither file nor resource found for AdX P12 private key file: " + ADX_P12_FILENAME);
            }
        }
        AdXCreativeApiManager apiManager = new AdXCreativeApiManager(stream, accountId, accountEmail);
        if (stream != null) {
            IOUtils.closeQuietly(stream);
        }
        return apiManager;
    }

    @Bean
    AuditCreativeRenderer AuditCreativeRenderer(AdMarkupRenderer renderer) {
        return new AuditCreativeRenderer(renderer);
    }

    /*
     * Beans from adfonic-adresponse
     */

    @Bean
    DisplayTypeUtils displayTypeUtils() {
        return new DisplayTypeUtilsImpl();
    }

    @Bean
    IconManager iconManager() {
        return new IconManagerImpl();
    }

    @Bean
    AdResponseLogic adResponseLogic(DisplayTypeUtils displayTypeUtils, IconManager iconMgr, VhostManager vhostManager, DynamicProperties dProperties) {
        return new AdResponseLogicImpl(displayTypeUtils, iconMgr, vhostManager, dProperties);
    }

    @Bean(name = AdXAdMarkupGenerator.BEAN_NAME)
    AdXAdMarkupGenerator adxMarkupGenarator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dProperties) {
        return new AdXAdMarkupGenerator(velocityEngine, displayTypeUtils, dProperties);
    }

    @Bean(name = AppNexusMarkupGenerator.BEAN_NAME)
    AppNexusMarkupGenerator appNexusMarkupGenerator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dProperties) {
        return new AppNexusMarkupGenerator(velocityEngine, displayTypeUtils, dProperties);
    }

    @Bean(name = MarkupGeneratorImpl.BEAN_NAME)
    MarkupGenerator standardMarkupGenerator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dProperties) {
        return new MarkupGeneratorImpl(velocityEngine, displayTypeUtils, dProperties);
    }

    @Bean
    AdMarkupRenderer adMarkupRenderer(AdResponseLogic adResponseLogic, DisplayTypeUtils displayTypeUtils) {
        return new AdMarkupRenderer(adResponseLogic, displayTypeUtils, new CounterManager(Collections.EMPTY_SET));
    }

    @Bean
    public VelocityEngine velocityEngine() throws VelocityException, IOException {
        VelocityEngineFactory factory = new VelocityEngineFactory();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.put("class.resource.loader.resourceLoaderPath", "/velocity");
        factory.setVelocityPropertiesMap(properties);
        return factory.createVelocityEngine();
    }

    /*    
        @Bean
        RenderingService renderingService(AdResponseLogicXaudit adResponseLogic, @Qualifier("xauditMarkupGenerator" MarkupGenerator markupGenerator) ) {
            return new RenderingServiceImpl(adResponseLogic, markupGenerator);
        }

        @Bean
        AdResponseLogicXaudit adResponseLogicOld() {
            return new AdResponseLogicXauditImpl();
        }

        @Bean(name="xauditMarkupGenerator")
        MarkupGenerator markupGeneratorOld(DisplayTypeUtils displayTypeUtils) throws VelocityException, IOException {
            VelocityEngine velocityEngine = velocityEngine();

            return new MarkupGeneratorXauditImpl(velocityEngine, displayTypeUtils);
        }
    */

    @Bean
    VhostManager vhostManager(@Value("${audit.adserver.base.url}") String adserverAuditBaseUrl) {
        return new AuditVhostManager(adserverAuditBaseUrl);
    }

    /**
     * Stubs of dependencies or dummy configuration values as they are not necessary for creative rendering but still needs to be injected
     
     
    @Bean
    VhostManager vhostManager(@Value("${asset.base.url}") String assetUrl, @Value("${appnexus.base.url}") String adserverBaseUrl) {
        AppNexusVhostManager appNexusVhostManager = new AppNexusVhostManager();
        appNexusVhostManager.setAssetUrl(assetUrl);
        appNexusVhostManager.setAdserverBaseUrl(adserverBaseUrl);
        
        return appNexusVhostManager;
    }
    
    @Bean
    DisplayTypeUtils displayTypeUtilsOld() {
        return new DisplayTypeUtilsXauditImpl();
    }

    @Bean
    IconManager iconManager() {
        return new IconManagerXauditImpl();
    }
    
    @Bean
    java.util.Set<String> weveAdvertisers(@Value("${weve.company_ids:}") String weveCompanyIds) {
        return StringUtils.commaDelimitedListToSet(weveCompanyIds);
    }

    @Bean
    String weveBeaconUrl(@Value("${weve.beacon.url}") String weveBeaconUrl) {
        return weveBeaconUrl;
    }

    @Bean
    String trusteWevePid() {
        return "";
    }

    @Bean
    String trusteWeveWebAid() {
        return "";
    }

    @Bean
    String trusteWeveAppAid() {
        return "";
    }

    @Bean
    String trusteDefaultAeskey() {
        return "";
    }

    @Bean
    String trusteDefaultWebAid() {
        return "";
    }

    @Bean
    String trusteDefaultAppAid() {
        return "";
    }
    */
}
