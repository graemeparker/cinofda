package com.adfonic.presentation.learnings.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class RemoveCampaignFromLearningAlgorithmStoredProcedure extends AbstractStoredProcedure {

    public RemoveCampaignFromLearningAlgorithmStoredProcedure(DataSource dataSource) {
        super(dataSource, "proc_exclude_campaign");
        declareParameter(new SqlParameter("in_campaign_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_adfonic_user_id", Types.NUMERIC));
        compile();
    }

}
