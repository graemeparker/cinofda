package com.adfonic.olap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Util;
import mondrian.spi.CatalogLocator;

public class MondrianSqlDataSource extends MondrianDataSource {
    private static final Logger LOGGER = Logger.getLogger(MondrianSqlDataSource.class.getName());
    private DataSource dataSource;
    private Util.PropertyList list;

    private final CatalogLocator catalogLocator = new CatalogLocator() {
        // For some reason this gets called in what looks like
        // a recursive fashion, so just bail out if the value
        // already looks like a URL
        public String locate(String catalogPath) {
        if (catalogPath.indexOf(':') != -1) {
            return catalogPath;
        }
        return getClass().getClassLoader()
            .getResource(catalogPath).toString();
        }
    };

    public void setCatalogLocation(String catalogLocation) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("catalogLocation set to " + catalogLocation);
        }
    list = new Util.PropertyList();
    list.put("Provider", "mondrian");
    list.put("Catalog", catalogLocation);
    }

    @Override
    public Connection getConnection() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Getting connection");
        }
    return DriverManager.getConnection(list, catalogLocator, dataSource);
    }

    public void setReportingDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This method allows this object to be used as a message delegate for the
     * JMS topic announcing the need to flush the cache.  We need to have a
     * method that takes a "message" as an argument so that the message
     * listener can call it when it receives a message on the JMS topic.
     */
    public void onCacheFlush(Object unusedMessage) {
        flushSchemaCache();
    }

    public synchronized void flushSchemaCache() {
        LOGGER.info("Flushing schema cache");
        Connection conn = getConnection();
        try {
            conn.getCacheControl(null).flushSchemaCache();
        }finally {
            try {
                conn.close();
            }catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error closing Mondrian connection", e);
            }
        }
    }
}
