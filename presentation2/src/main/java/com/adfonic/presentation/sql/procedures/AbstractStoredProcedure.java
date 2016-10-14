package com.adfonic.presentation.sql.procedures;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

public class AbstractStoredProcedure extends StoredProcedure {

	protected Logger LOG = Logger.getLogger(getClass().getName());
	
	private String name;

	public AbstractStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		this.name = name;
	}

	public Map<String, Object> run(Object... inParams) {
		if (LOG.isLoggable(Level.INFO)) {
			StringBuilder params = new StringBuilder();
			int i = 0;
			for (SqlParameter sqlParameter : getDeclaredParameters()) {
				if (sqlParameter.isInputValueProvided()) {
					if (i < inParams.length) {
						params.append(inParams[i++]).append(" ");
					}
				}
			}
			logger.info(String.format("Procedure Call [%s: %s ]", name, params));
		}
		
		StopWatch stopWatch = null;
		if (LOG.isLoggable(Level.FINE)) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}
		
		Map<String, Object> results = super.execute(inParams);
		
		if (LOG.isLoggable(Level.FINE)) {
			stopWatch.stop();
			LOG.fine("Stored procedure \"" + this.name + "\" running time: " + stopWatch.toString());
		}
		
		return results;
	}


}
