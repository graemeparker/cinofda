package com.adfonic.presentation.learnings.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class RemoveAdSpaceLearningsStoredProcedure extends AbstractStoredProcedure {

    public RemoveAdSpaceLearningsStoredProcedure(DataSource dataSource) {
        super(dataSource, "proc_remove_ad_space_learnings");
        declareParameter(new SqlParameter("in_ad_space_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_adfonic_user_id", Types.NUMERIC));
        compile();
    }

}
