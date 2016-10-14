package com.adfonic.presentation.audience.dao.impl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.adfonic.presentation.audience.dao.MuidNotificationDao;
import com.adfonic.presentation.audience.model.MuidSessionModel;
import com.adfonic.presentation.audience.sql.procedures.MuidInboundCheckProgressStoredProcedure;
import com.adfonic.presentation.util.GenericDaoImpl;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for MUID notification API.
 */
@Repository
public class MuidNotificationDaoImpl extends GenericDaoImpl implements MuidNotificationDao {

    private static final transient Logger LOG = Logger.getLogger(MuidNotificationDaoImpl.class.getName());

    @Autowired(required = false)
    @Qualifier("muidDataSource")
    private DataSource dataSource;

    // MUID Notification

    @Override
    public MuidSessionModel inboundCheckProgress(BigDecimal sessionId) {

        MuidInboundCheckProgressStoredProcedure procedure = new MuidInboundCheckProgressStoredProcedure(dataSource, "proc_inbound_check_progress");
        Utils.logWithTitle(LOG, Level.FINE, "MUID session id check progress proc call", procCallWithOneParam(procedure.getSql(), sessionId));

        MuidSessionModel result = null;
        Map<String, Object> data;
        try {
            data = procedure.execute(sessionId);
            result = (MuidSessionModel) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "MUID Session model", result);
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;

    }

}
