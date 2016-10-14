package com.adfonic.reporting.service.advertiser.procedure;

import javax.sql.DataSource;

import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;
/**
 * WARNING
 * This is procedure for testing purposes is ONLY.  
 * This calls an infinite loop procedure.
 */
public class InfiniteProc extends BaseStoredProcedure {
	
	public InfiniteProc(DataSource ds, String name) {
		super(ds, name);
		compile();
	}
}
