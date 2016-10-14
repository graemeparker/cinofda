package com.adfonic.presentation.learnings.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class IncludeCampaignToLearningAlgorithmStoredProcedure extends AbstractStoredProcedure {

    public IncludeCampaignToLearningAlgorithmStoredProcedure(DataSource dataSource) {
        super(dataSource, "proc_include_campaign");
        declareParameter(new SqlParameter("in_campaign_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_adfonic_user_id", Types.NUMERIC));
        compile();
    }
    
}
