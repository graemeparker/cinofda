package com.adfonic.tasks.config.db;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.adfonic.domain.Campaign;
import com.adfonic.util.ConfUtils;

/**
 * @author mvanek
 * 
 * Replacement for adfonic-toolsdb-context.xml
 *
 */
@Configuration
@EnableTransactionManagement
public class ToolsDbSpringConfig {

    public static final String APPNAME = "tasks";

    @Autowired
    Environment springEnv;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(ToolsDbSpringConfig.APPNAME);
    }

    @Bean(name = ConfUtils.TOOLS_DS, destroyMethod = "close")
    public DataSource toolsDataSource() {
        return ConfUtils.dbcpDataSource("tools", springEnv);
    }

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

}
