package com.adfonic.domain.cache.ext.util;

import java.sql.ResultSet;
import java.util.Date;

public class DbUtil {

    public static Long nullableLong(ResultSet rs, int idx) throws java.sql.SQLException {
        if (rs.getObject(idx) == null) {
            return null;
        } else {
            return rs.getLong(idx);
        }
    }

    public static Long nullableLong(ResultSet rs, String columnName) throws java.sql.SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        } else {
            return rs.getLong(columnName);
        }
    }

    public static Integer nullableInt(ResultSet rs, int idx) throws java.sql.SQLException {
        if (rs.getObject(idx) == null) {
            return null;
        } else {
            return rs.getInt(idx);
        }
    }

    public static Integer nullableInt(ResultSet rs, String columnName) throws java.sql.SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        } else {
            return rs.getInt(columnName);
        }
    }

    public static Boolean nullableBoolean(ResultSet rs, int idx) throws java.sql.SQLException {
        if (rs.getObject(idx) == null) {
            return null;
        } else {
            return rs.getBoolean(idx);
        }
    }

    public static Boolean nullableBoolean(ResultSet rs, String columnName) throws java.sql.SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        } else {
            return rs.getBoolean(columnName);
        }
    }

    public static Double nullableDouble(ResultSet rs, int idx) throws java.sql.SQLException {
        if (rs.getObject(idx) == null) {
            return null;
        } else {
            return rs.getDouble(idx);
        }
    }

    public static Double nullableDouble(ResultSet rs, String columnName) throws java.sql.SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        } else {
            return rs.getDouble(columnName);
        }
    }

    public static Date nullableDate(ResultSet rs, int idx) throws java.sql.SQLException {
        if (rs.getObject(idx) == null) {
            return null;
        } else {
            return rs.getDate(idx);
        }
    }

    public static Date nullableDate(ResultSet rs, String columnName) throws java.sql.SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        } else {
            return rs.getDate(columnName);
        }
    }
}
