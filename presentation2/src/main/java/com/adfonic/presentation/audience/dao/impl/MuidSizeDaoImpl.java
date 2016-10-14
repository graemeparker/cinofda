package com.adfonic.presentation.audience.dao.impl;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.adfonic.presentation.audience.dao.MuidSizeDao;
import com.adfonic.presentation.audience.sql.procedures.MuidBuildSingleSegmentSizeStoredProcedure;
import com.adfonic.presentation.audience.sql.procedures.MuidGetSegmentSizeStoredProcedure;

public class MuidSizeDaoImpl extends JdbcDaoSupport implements MuidSizeDao {

    @Override
    public Long getMuidSegmentSize(Long segmentId) {
        return new MuidGetSegmentSizeStoredProcedure(getDataSource()).run(segmentId);
    }

    @Override
    public void buildSingleSegmentSize(Long segmentId) {
        new MuidBuildSingleSegmentSizeStoredProcedure(getDataSource()).run(segmentId);
    }

}
