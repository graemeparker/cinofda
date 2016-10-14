package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.presentation.audience.sql.mappers.MuidSegmentIdResultSetExtractor;
import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class MuidDeleteSegmentStoredProcedure extends AbstractStoredProcedure {

	public MuidDeleteSegmentStoredProcedure(DataSource ds) {
		this(ds, "proc_delete_segment");
	}
	
	public MuidDeleteSegmentStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_segment_id", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result", new MuidSegmentIdResultSetExtractor()));
		compile();
	}

	public Long run(Long segmentId) {
        Map<String, Object> data = super.run(segmentId);

        Long count = (Long)data.get("result");
        return count;
	}
}
