package com.byyd.middleware.domainlog.service.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AuditLog;
import com.byyd.middleware.domainlog.dao.AuditLogDao;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.domainlog.service.AuditLogManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;

@Service("domainLogManager")
public class AuditLogManagerJpaImpl extends BaseJpaManagerImpl implements AuditLogManager {
    @Autowired
    private AuditLogDao auditLogDao;

    public AuditLogManagerJpaImpl() {
        super();
    }

    @Override
    @Transactional(readOnly=false)
    public AuditLog create(AuditLog auditLog) {
        return auditLogDao.create(auditLog);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAll(AuditLogFilter filter) {
        return auditLogDao.countAll(filter);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<AuditLog> getAll(AuditLogFilter filter, FetchStrategy ... fetchStrategy) {
        return auditLogDao.getAll(filter, fetchStrategy);
    }
}
