package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.presentation.audience.sql.mappers.MuidSegmentSizeResultSetExtractor;
import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class MuidGetSegmentSizeStoredProcedure extends AbstractStoredProcedure {
	
	private static final Logger LOG = LoggerFactory.getLogger(MuidGetSegmentSizeStoredProcedure.class);

	public MuidGetSegmentSizeStoredProcedure(DataSource ds) {
		this(ds, "proc_get_segment_size");
	}
	
	public MuidGetSegmentSizeStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_segment_id", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result", new MuidSegmentSizeResultSetExtractor()));
		compile();
	}

	public Long run(Long segmentId) {
        Map<String, Object> data = super.run(segmentId);

        Long count = ((Number)data.get("result")).longValue();
        if(count < 0) {
        	LOG.error("proc_get_segment_size returned " + count + " for segmentId " + segmentId);
        	return 0L;
        }
        return count;
	}
}
