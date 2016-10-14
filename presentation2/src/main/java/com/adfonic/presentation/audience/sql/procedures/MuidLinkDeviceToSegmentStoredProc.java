package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.presentation.audience.sql.mappers.MuidRowsInsertedResultSetExtractor;
import com.adfonic.presentation.sql.procedures.AbstractStoredProcedure;

public class MuidLinkDeviceToSegmentStoredProc extends AbstractStoredProcedure {
	
	public static final Integer MAX_DEVICE_IDS_STRING_LENGTH = 16384;

	public MuidLinkDeviceToSegmentStoredProc(DataSource ds) {
		this(ds, "proc_link_device_to_segment");
	}
	
	public MuidLinkDeviceToSegmentStoredProc(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_segment_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_device_ids", Types.VARCHAR));
		declareParameter(new SqlReturnResultSet("result", new MuidRowsInsertedResultSetExtractor()));
		compile();
	}

	public Long run(Long firstPartyAudienceId, String deviceIds) {
        Map<String, Object> data = super.run(firstPartyAudienceId, deviceIds);

        Long count = (Long)data.get("result");
        return count;
	}
}
