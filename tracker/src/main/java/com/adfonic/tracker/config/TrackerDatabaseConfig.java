package com.adfonic.tracker.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.adfonic.domain.Campaign;
import com.adfonic.util.ConfUtils;

@Configuration
@EnableTransactionManagement
public class TrackerDatabaseConfig {

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory emFactory, @Qualifier(ConfUtils.TOOLS_DS) DataSource dataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emFactory);
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier(ConfUtils.TOOLS_DS) DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName("adfonic-domain");
        bean.setPackagesToScan(Campaign.class.getPackage().getName());
        bean.setDataSource(dataSource);
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setGenerateDdl(false);
        bean.setJpaVendorAdapter(jpaVendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", org.hibernate.dialect.MySQL5InnoDBDialect.class.getName());
        jpaProperties.setProperty("hibernate.format_sql", "true");
        jpaProperties.setProperty("hibernate.generate_statistics", "false");
        jpaProperties.setProperty("hibernate.jdbc.fetch_size", "10");
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "10");
        bean.setJpaProperties(jpaProperties);
        return bean;
    }

    @Bean(name = ConfUtils.TOOLS_DS, destroyMethod = "close")
    public BasicDataSource cachedbDataSource(//
            @Value("${tools.jdbc.driverClassName}") String driverClassName, //
            @Value("${tools.jdbc.url}") String url, //
            @Value("${tools.jdbc.username}") String username,//
            @Value("${tools.jdbc.password}") String password,//
            @Value("${tools.dbcp.initialSize:0}") int initialSize,//
            @Value("${tools.dbcp.maxActive}") int maxActive,//
            @Value("${tools.dbcp.maxWait:-1}") long maxWait, //
            @Value("${tools.dbcp.minIdle:0}") int minIdle, //
            @Value("${tools.dbcp.maxIdle}") int maxIdle, // 
            @Value("${tools.dbcp.testOnBorrow:true}") boolean testOnBorrow,//
            @Value("${tools.dbcp.testOnReturn:false}") boolean testOnReturn,//
            @Value("${tools.dbcp.testWhileIdle:false}") boolean testWhileIdle, //
            @Value("${tools.dbcp.validationQuery}") String validationQuery,//
            @Value("${tools.dbcp.validationQueryTimeout:-1}") int validationQueryTimeout, //
            @Value("${tools.dbcp.timeBetweenEvictionRunsMillis:-1}") long timeBetweenEvictionRunsMillis, //
            @Value("${tools.dbcp.numTestsPerEvictionRun:3}") int numTestsPerEvictionRun, //
            @Value("${tools.dbcp.minEvictableIdleTimeMillis:1800000}") int minEvictableIdleTimeMillis, //
            @Value("${tools.dbcp.defaultTransactionIsolation:4}") int defaultTransactionIsolation,//
            @Value("${tools.dbcp.removeAbandoned:true}") boolean removeAbandoned, //
            @Value("${tools.dbcp.removeAbandonedTimeout:300}") int removeAbandonedTimeout,//
            @Value("${tools.dbcp.logAbandoned:true}") boolean logAbandoned) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setValidationQueryTimeout(validationQueryTimeout);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
        dataSource.setRemoveAbandoned(removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setLogAbandoned(logAbandoned);

        return dataSource;
    }

    @Bean(name = ConfUtils.TRACKER_DS, destroyMethod = "close")
    public BasicDataSource trackerDataSource(//
            @Value("${tracker.jdbc.driverClassName}") String driverClassName, //
            @Value("${tracker.jdbc.url}") String url, //
            @Value("${tracker.jdbc.username}") String username,//
            @Value("${tracker.jdbc.password}") String password,//
            @Value("${tracker.dbcp.initialSize:0}") int initialSize,//
            @Value("${tracker.dbcp.maxActive}") int maxActive,//
            @Value("${tracker.dbcp.maxWait:-1}") long maxWait, //
            @Value("${tracker.dbcp.minIdle:0}") int minIdle, //
            @Value("${tracker.dbcp.maxIdle}") int maxIdle, // 
            @Value("${tracker.dbcp.testOnBorrow:true}") boolean testOnBorrow,//
            @Value("${tracker.dbcp.testOnReturn:false}") boolean testOnReturn,//
            @Value("${tracker.dbcp.testWhileIdle:false}") boolean testWhileIdle, //
            @Value("${tracker.dbcp.validationQuery}") String validationQuery,//
            @Value("${tracker.dbcp.validationQueryTimeout:-1}") int validationQueryTimeout, //
            @Value("${tracker.dbcp.timeBetweenEvictionRunsMillis:-1}") long timeBetweenEvictionRunsMillis, //
            @Value("${tracker.dbcp.numTestsPerEvictionRun:3}") int numTestsPerEvictionRun, //
            @Value("${tracker.dbcp.minEvictableIdleTimeMillis:1800000}") int minEvictableIdleTimeMillis, //
            @Value("${tracker.dbcp.defaultTransactionIsolation:2}") int defaultTransactionIsolation,//
            @Value("${tracker.dbcp.removeAbandoned:true}") boolean removeAbandoned, //
            @Value("${tracker.dbcp.removeAbandonedTimeout:300}") int removeAbandonedTimeout,//
            @Value("${tracker.dbcp.logAbandoned:true}") boolean logAbandoned) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setValidationQueryTimeout(validationQueryTimeout);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
        dataSource.setRemoveAbandoned(removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setLogAbandoned(logAbandoned);

        return dataSource;
    }
}
