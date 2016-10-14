package com.adfonic.adserver.spring.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.data.cache.AdserverDataCacheManagerImpl;
import com.adfonic.data.cache.ecpm.loader.EcpmDataCacheLoader;
import com.adfonic.data.cache.loaders.DataCacheCurrencyLoader;
import com.adfonic.data.cache.util.PropertiesFactory;
import com.adfonic.domain.cache.AdserverDomainCacheManager;

/**
 * 
 * @author mvanek
 *
 */
@Configuration
public class AdserverCacheDbSpringConfig {

    public static final String CACHEDB_DS = "cachedbDataSource";
    public static final String CACHEDB_JDBC_TEMPLATE = "cachedbJdbcTemplate";
    public static final String ECPM_DS = "ecpmDataSource";

    // CacheDb is only MySQL database AdServer is using. But in strictly asynchronous mode. Never reads from it during bid request or notification 

    @Bean(name = CACHEDB_DS, destroyMethod = "close")
    public BasicDataSource cachedbDataSource(@Value("${cachedb.jdbc.driverClassName}") String driverClassName, @Value("${cachedb.jdbc.url}") String url,
            @Value("${cachedb.jdbc.username}") String username, @Value("${cachedb.jdbc.password}") String password, @Value("${cachedb.jdbc.maxActive}") int maxActive,
            @Value("${cachedb.jdbc.maxWait}") long maxWait, @Value("${cachedb.jdbc.minIdle}") int minIdle, @Value("${cachedb.jdbc.maxIdle}") int maxIdle,
            @Value("${cachedb.jdbc.validationQuery}") String validationQuery, @Value("${cachedb.jdbc.timeBetweenEvictionRunsMillis}") long timeBetweenEvictionRunsMillis,
            @Value("${cachedb.jdbc.testWhileIdle}") boolean testWhileIdle, @Value("${cachedb.jdbc.numTestsPerEvictionRun}") int numTestsPerEvictionRun) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        return dataSource;
    }

    /**
     * Yes, it is really same datasource. Why oh why? 
     */
    @Bean(name = ECPM_DS, destroyMethod = "close")
    public BasicDataSource ecpmDataSource(@Qualifier(CACHEDB_DS) BasicDataSource dataSource) {
        return dataSource;
    }

    @Bean(name = CACHEDB_JDBC_TEMPLATE)
    public JdbcTemplate cachedbjdbcTemplate(@Qualifier(CACHEDB_DS) BasicDataSource cacheDbDataSource) {
        return new JdbcTemplate(cacheDbDataSource);
    }

    @Bean
    public AdserverDataCacheManager adserverDataCacheManager(AdserverDomainCacheManager adserverDomainCacheManager, @Qualifier(CACHEDB_DS) BasicDataSource cacheDbDataSource,
            @Qualifier(ECPM_DS) BasicDataSource ecpmDataSource) {

        DataCacheCurrencyLoader dataCacheCurrencyLoader = new DataCacheCurrencyLoader(cacheDbDataSource);
        EcpmDataCacheLoader ecpmDataCacheLoader = new EcpmDataCacheLoader(ecpmDataSource);
        PropertiesFactory propertiesFactory = new PropertiesFactory(cacheDbDataSource);
        return new AdserverDataCacheManagerImpl(adserverDomainCacheManager, ecpmDataCacheLoader, dataCacheCurrencyLoader, propertiesFactory);
    }
}
