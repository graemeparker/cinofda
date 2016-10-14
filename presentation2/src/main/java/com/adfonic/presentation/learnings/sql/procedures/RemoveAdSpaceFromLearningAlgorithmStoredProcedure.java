package com.adfonic.presentation.learnings.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class RemoveAdSpaceFromLearningAlgorithmStoredProcedure extends AbstractStoredProcedure {

    public RemoveAdSpaceFromLearningAlgorithmStoredProcedure(DataSource dataSource) {
        super(dataSource, "proc_exclude_ad_space");
        declareParameter(new SqlParameter("in_ad_space_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_adfonic_user_id", Types.NUMERIC));
        compile();
    }

}
