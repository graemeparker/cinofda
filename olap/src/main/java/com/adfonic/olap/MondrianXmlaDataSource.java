package com.adfonic.olap;

import java.util.logging.Level;
import java.util.logging.Logger;
import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Util;
import mondrian.spi.CatalogLocator;

public class MondrianXmlaDataSource extends MondrianDataSource {
    private static final transient Logger LOGGER = Logger.getLogger(MondrianXmlaDataSource.class.getName());
    private String catalogLocation;
    private String dataSourceName;

    private final CatalogLocator catalogLocator = new CatalogLocator() {
        // For some reason this gets called in what looks like
        // a recursive fashion, so just bail out if the value
        // already looks like a URL
        public String locate(String catalogPath) {
            LOGGER.info("Locating: " + catalogPath);
            if (catalogPath.indexOf(':') != -1) {
                return catalogPath;
            }
            return getClass().getClassLoader().getResource(catalogPath).toString();
            }
        };

    public void setCatalogLocation(String catalogLocation) {
        this.catalogLocation = catalogLocation;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public Connection getConnection() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Getting connection, catalogLocation=" + catalogLocation);
        }
        final Util.PropertyList list = new Util.PropertyList();
        list.put("Provider", "Mondrian");
        list.put("DataSource", dataSourceName);
        list.put("Catalog", catalogLocation);
        return DriverManager.getConnection(list, catalogLocator);
    }
}
