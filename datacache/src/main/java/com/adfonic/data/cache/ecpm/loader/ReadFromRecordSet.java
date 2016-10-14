package com.adfonic.data.cache.ecpm.loader;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ReadFromRecordSet {
    boolean read(ResultSet rs) throws SQLException; //return true if the read is successful
}
