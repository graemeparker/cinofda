package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class MuidBuildSingleSegmentSizeStoredProcedure extends AbstractStoredProcedure {
	
	public MuidBuildSingleSegmentSizeStoredProcedure(DataSource ds) {
		this(ds, "proc_build_single_segment_size_cache");
	}
	
	public MuidBuildSingleSegmentSizeStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_segment_id", Types.NUMERIC));
		compile();
	}

	public void run(Long segmentId) {
        super.run(segmentId);
	}
}