package com.adfonic.util.status;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

/**
 * 
 * @author mvanek
 *
 */
public class DataSourceCheck<ID extends Serializable> extends BaseResourceCheck<ID> {

    private final DataSource dataSource;
    private final String testSql;

    public DataSourceCheck(DataSource dataSource, String testSql) {
        if (dataSource == null) {
            throw new IllegalArgumentException("Null DataSource");
        }
        this.dataSource = dataSource;
        if (testSql == null) {
            throw new IllegalArgumentException("Null testSql");
        }
        this.testSql = testSql;
    }

    @Override
    public String doCheck(ResourceId<ID> resource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(testSql)) {
                if (statement.execute()) {
                    statement.getResultSet().close();
                }
            }
        }
        return null;
    }
}
