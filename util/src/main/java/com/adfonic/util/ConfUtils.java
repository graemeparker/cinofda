package com.adfonic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

public class ConfUtils {

    public static final String TOOLS_DS = "toolsDataSource";
    public static final String TRACKER_DS = "trackerDataSource";
    public static final String MUID_DS = "muidDataSource";
    public static final String ADM_REPORTING_DS = "admReportingDataSource"; // lon3reportdb03
    public static final String OPT_DS = "optDataSource"; // optdb.lon3.adf.local
    public static final String OPTOUT_DS = "optoutDataSource"; // muiddb.lon3.adf.local

    public static final String TOOLS_JDBC_TEMPLATE = "toolsJdbcTemplate";
    public static final String TRACKER_JDBC_TEMPLATE = "trackerJdbcTemplate";

    public static final String CONFIG_DIR_PROPERTY = "adfonic.config.home";
    public static final String CONFIG_DIR_DEFAULT = "/usr/local/adfonic/config";
    public static final String CONFIG_DIR_CONFIG = "${" + CONFIG_DIR_PROPERTY + ":" + CONFIG_DIR_DEFAULT + "}";

    public static final String CACHE_DIR_PROPERTY = "adfonic.cache.home";
    public static final String CACHE_DIR_DEFAULT = "/usr/local/adfonic/cache";
    public static final String CACHE_DIR_CONFIG = "${" + CACHE_DIR_PROPERTY + ":" + CACHE_DIR_DEFAULT + "}";

    public static Properties checkAppProperties(String appName) {
        return checkSysPropFile(CONFIG_DIR_PROPERTY, CONFIG_DIR_DEFAULT, "adfonic-" + appName + ".properties");
    }

    public static Properties checkSysPropFile(String sysPropNameForDirectory, String defaultDirectory, String fileName) {
        File directory = checkSysPropDirectory(sysPropNameForDirectory, defaultDirectory);
        File file = new File(directory, fileName);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("File '" + fileName + "' in '" + directory + "' does not exist or is not readable");
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException iox) {
            throw new IllegalArgumentException("Failed to read " + file.getAbsolutePath());
        }
        return properties;
    }

    public static File checkSysPropDirectory(String sysPropName, String defaultDirectory) {
        String sysPropValue = System.getProperty(sysPropName);
        if (sysPropValue == null) {
            File directory = new File(defaultDirectory);
            if (directory.exists() || directory.isDirectory() || directory.canRead()) {
                return directory;
            } else {
                throw new IllegalStateException("System property '" + sysPropName + "' not found and default " + defaultDirectory
                        + " does not exist or is not readable. Start JVM with parameter -D" + sysPropName + "=/existing/directory");
            }
        } else {
            File directory = new File(sysPropValue);
            if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
                throw new IllegalArgumentException("System property '" + sysPropName + "' defined directory: " + sysPropValue + " does not exist or is not readable directory");
            }
            return directory;
        }
    }

    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(String appName) {
        String configHome = System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT);
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setFileEncoding("utf-8");
        configurer.setLocation(new FileSystemResource(configHome + "/adfonic-" + appName + ".properties"));
        return configurer;
    }

    public static BasicDataSource dbcpDataSource(String prefix, Environment springEnv) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(springEnv.getProperty(prefix + ".jdbc.driverClassName"));
        dataSource.setUrl(springEnv.getProperty(prefix + ".jdbc.url"));
        dataSource.setUsername(springEnv.getProperty(prefix + ".jdbc.username"));
        dataSource.setPassword(springEnv.getProperty(prefix + ".jdbc.password"));

        dataSource.setInitialSize(springEnv.getProperty(prefix + ".dbcp.initialSize", Integer.class, 0));
        dataSource.setMaxActive(springEnv.getProperty(prefix + ".dbcp.maxActive", Integer.class));
        dataSource.setMaxWait(springEnv.getProperty(prefix + ".dbcp.maxWait", Long.class, -1l));
        dataSource.setMinIdle(springEnv.getProperty(prefix + ".dbcp.minIdle", Integer.class, 0));
        dataSource.setMaxIdle(springEnv.getProperty(prefix + ".dbcp.maxIdle", Integer.class));

        dataSource.setTestOnReturn(springEnv.getProperty(prefix + ".dbcp.testOnReturn", Boolean.class, false));
        dataSource.setTestWhileIdle(springEnv.getProperty(prefix + ".dbcp.testWhileIdle", Boolean.class, false));
        dataSource.setTestOnBorrow(springEnv.getProperty(prefix + ".dbcp.testOnBorrow", Boolean.class, true));
        dataSource.setValidationQuery(springEnv.getProperty(prefix + ".dbcp.validationQuery", "SELECT 1"));
        dataSource.setValidationQueryTimeout(springEnv.getProperty(prefix + ".dbcp.validationQueryTimeout", Integer.class, -1));
        dataSource.setTimeBetweenEvictionRunsMillis(springEnv.getProperty(prefix + ".dbcp.timeBetweenEvictionRunsMillis", Long.class, -1l));
        dataSource.setNumTestsPerEvictionRun(springEnv.getProperty(prefix + ".dbcp.numTestsPerEvictionRun", Integer.class, 3));
        dataSource.setMinEvictableIdleTimeMillis(springEnv.getProperty(prefix + ".dbcp.minEvictableIdleTimeMillis", Long.class, 1800000l));
        /**
         * REPEATABLE_READ = 4 = MySQL default isolation level
         * java.sql.Connection#getTransactionIsolation
         * 1 = READ_UNCOMMITED, 2 = READ_COMMITED, 4 = REPEATABLE_READ, 8 = SERIALIZABLE
         */
        dataSource.setDefaultTransactionIsolation(springEnv.getProperty(prefix + ".dbcp.defaultTransactionIsolation", Integer.class, 4));
        dataSource.setRemoveAbandoned(springEnv.getProperty(prefix + ".dbcp.removeAbandoned", Boolean.class, true));
        dataSource.setRemoveAbandonedTimeout(springEnv.getProperty(prefix + ".dbcp.removeAbandonedTimeout", Integer.class, 300)); // Seconds
        dataSource.setLogAbandoned(springEnv.getProperty(prefix + ".dbcp.logAbandoned", Boolean.class, true));
        return dataSource;
    }

}
