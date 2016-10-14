package com.adfonic.presentation.optimisation.sql.procedures;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Base class for the mapping of the strored procedures involved in the optimisation interface
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceBaseStoredProcedure extends StoredProcedure {
	
	protected Logger logger = Logger.getLogger(getClass().getName());
	
	private String name;

	public OptimisationUserInterfaceBaseStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		this.name = name;
	}

	public static int getSortNumberValue(String sortColumn) {
		
		if(sortColumn != null) {
			switch(sortColumn) {
			case "publication": 			return 1;
			case "publicationType": 		return 2;
			case "publicationExternalId": 	return 3;
			case "iabCategory": 			return 4;
			case "inventorySource": 		return 5;
			case "bids": 					return 6;
			case "impressions": 			return 7;
			case "winRate":					return 8;
			case "clicks": 					return 9;
			case "ctr": 					return 10;
			case "conversions": 			return 11;
			case "cvr": 					return 12;
			case "spend": 					return 13;
			case "ecpm": 					return 14;
			case "ecpc": 					return 15;
			case "ecpa": 					return 16;
			case "publicationBundle":       return 17;
			case "creativeName":			return 18;
			}
		}
		return 1;
	}
	
	public Map<String, Object> run(Object... inParams) {
		//if (logger.isLoggable(Level.FINE)) {
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
		//}
		org.apache.commons.lang.time.StopWatch stopWatch = null;
		if (logger.isLoggable(Level.FINE)) {
			stopWatch = new org.apache.commons.lang.time.StopWatch();
			stopWatch.start();
		}
		Map<String, Object> results = super.execute(inParams);
		if (logger.isLoggable(Level.FINE)) {
			stopWatch.stop();
			logger.fine("Stored procedure \"" + this.name + "\" running time: " + stopWatch.toString());
		}
		return results;
	}


}
