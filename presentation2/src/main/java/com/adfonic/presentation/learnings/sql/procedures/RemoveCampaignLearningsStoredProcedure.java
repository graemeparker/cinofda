package com.adfonic.presentation.learnings.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class RemoveCampaignLearningsStoredProcedure extends AbstractStoredProcedure {

    public RemoveCampaignLearningsStoredProcedure(DataSource dataSource) {
        super(dataSource, "proc_remove_campaign_learnings");
        declareParameter(new SqlParameter("in_campaign_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_adfonic_user_id", Types.NUMERIC));
        compile();
    }

}
