package com.byyd.middleware.audit.service.jpa;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Audit;
import com.byyd.middleware.audit.dao.AuditDao;
import com.byyd.middleware.audit.service.AuditManager;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;

@Service("auditManager")
public class AuditManagerJpaImpl extends BaseJpaManagerImpl implements AuditManager {
    
    @Autowired(required=false)
    private AuditDao auditDao;

    @Override
    @Transactional(readOnly=false)
    public Audit newAudit(String className, String query) {
        Audit audit = new Audit(className, query, new Date());
        return auditDao.create(audit);
    }
}
