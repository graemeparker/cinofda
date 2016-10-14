package com.adfonic.reporting.sql.mapper.gen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.dto.gen.OrderedStatistics;


public interface OrderedstatisticsMapper extends RowMapper<OrderedStatistics>{

    public OrderedStatistics mapRow(ResultSet rs, int rowNum) throws SQLException;

}