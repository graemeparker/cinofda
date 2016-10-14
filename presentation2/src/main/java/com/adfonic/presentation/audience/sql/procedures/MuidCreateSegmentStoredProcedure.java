package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.presentation.audience.sql.mappers.MuidSegmentIdResultSetExtractor;
import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class MuidCreateSegmentStoredProcedure extends AbstractStoredProcedure {

	public MuidCreateSegmentStoredProcedure(DataSource ds) {
		this(ds, "proc_create_segment");
	}
	
	public MuidCreateSegmentStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_segment_reference", Types.VARCHAR));
		declareParameter(new SqlParameter("in_remote_segment_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_source_name", Types.VARCHAR));
		declareParameter(new SqlParameter("in_segment_type_name", Types.VARCHAR));
		declareParameter(new SqlReturnResultSet("result", new MuidSegmentIdResultSetExtractor()));
		compile();
	}

	public Long run(Long segmentReference, String segmentType) {
        Map<String, Object> data = super.run(segmentReference.toString(), null, "adfonic", segmentType.toLowerCase());

        Long id = (Long)data.get("result");
        return id;
	}
}
