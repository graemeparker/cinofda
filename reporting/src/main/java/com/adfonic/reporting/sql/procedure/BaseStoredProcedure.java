package com.adfonic.reporting.sql.procedure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class BaseStoredProcedure extends StoredProcedure {

	protected Logger logger = Logger.getLogger(getClass().getName());

	private String name;
	public BaseStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		this.name = name;
	}

	@Override
	public Map<String, Object> execute(Object... inParams) {
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

	public Map<String, Object> execute(UUID requestId, DataSource dataSource, Object... inParams) throws SQLIntegrityConstraintViolationException {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		// explicitly setting the transaction name
		def.setName("ReportingRequestTx");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

		TransactionStatus status = transactionManager.getTransaction(def);
		
		String connectionFetchStatement = "SELECT CONNECTION_ID() AS QUERY_ID, @@SERVER_ID AS SERVER_ID";
		String connectionInsertStatement = "INSERT INTO REPORTING_CONNECTION VALUE(?,?,?);";
		String connectionDeleteStatement = "DELETE FROM REPORTING_CONNECTION WHERE REQUEST_ID = ? AND QUERY_ID = ? AND SERVER_ID = ?" ;
		
		int queryId = 0;
		int serverId = 0;
		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(this.getJdbcTemplate().getDataSource());
			con.setAutoCommit(true);
			
			// Fetch the connection information - connection id and server id
			PreparedStatement pstConnectionFetch = con.prepareStatement(connectionFetchStatement);
			ResultSet rs = pstConnectionFetch.executeQuery();
			rs.next();
			queryId = rs.getInt("QUERY_ID");
			serverId = rs.getInt("SERVER_ID");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Request made for [RequestId:" + requestId + "][QueryId:" + queryId + "][ServerId:"+serverId+"]");
			}
			// Insert the requestId, queryId and serverId in REPORTING_CONNECTION table
			PreparedStatement pstConnectionInsert = con.prepareStatement(connectionInsertStatement);
			pstConnectionInsert.setString(1, requestId.toString());
			pstConnectionInsert.setInt(2, queryId);
			pstConnectionInsert.setInt(3, serverId);
			pstConnectionInsert.execute();
			
			// Now execute the procedure
			return execute(inParams);
		} catch (SQLIntegrityConstraintViolationException micv) {
			logger.severe(micv.getMessage());
			throw micv;
		} catch (SQLException se){
			logger.severe(se.getMessage());
		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		} finally {
			// when the result is returned do a delete here from the REPORTING_CONNECTION table
			try {
				PreparedStatement pstConnectionDelete = con.prepareStatement(connectionDeleteStatement);
				pstConnectionDelete.setString(1, requestId.toString());
				pstConnectionDelete.setInt(2, queryId); 
				pstConnectionDelete.setInt(3, serverId);
				pstConnectionDelete.execute();
			} catch (SQLException e) {
				// log as there as an exception while deleting the connection from REPORTING_CONNECTION table
				logger.severe(e.getMessage());
			}
		}
		return null;
	}
}
