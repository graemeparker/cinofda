package com.byyd.middleware.auditlog.service.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.byyd.middleware.auditlog.dao.AuditLogEntityDao;
import com.byyd.middleware.auditlog.dao.AuditLogEntryDao;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;
import com.byyd.middleware.auditlog.filter.AuditLogEntryFilter;
import com.byyd.middleware.auditlog.service.AuditLogManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;

@Service("auditLogManager")
public class AuditLogManagerJpaImpl extends BaseJpaManagerImpl implements AuditLogManager {
    
    @Autowired
    private AuditLogEntityDao<AuditLogEntity, AuditLogEntityFilter> auditLogEntityDao;
    @Autowired
    private AuditLogEntryDao<AuditLogEntry, AuditLogEntryFilter> auditLogEntryDao;
    
    //------ Business logic method
    @Override
    @Transactional(readOnly=false)
    public AuditLogEntry log(AuditLogEntity auditLogEntity, AuditLogEntry auditLogEntry){
        AuditLogEntity localAuditLogEntity = auditLogEntity;
        
        // Crate AuditEntity if it is not persisted
        if (localAuditLogEntity.getId() == 0L){
            localAuditLogEntity = create(localAuditLogEntity);
        }
        
        // Creating AuditLogEntry
        auditLogEntry.setAuditLogEntity(localAuditLogEntity);
        return create(auditLogEntry);
    }

    //------ AuditLogEntity entity methods
    @Override
    @Transactional(readOnly=false)
    public AuditLogEntity create(AuditLogEntity auditLogEntity){
        return auditLogEntityDao.create(auditLogEntity);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAll(AuditLogEntityFilter filter){
        return auditLogEntityDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntity> getAll(AuditLogEntityFilter filter, FetchStrategy ... fetchStrategy){
        return auditLogEntityDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntity> getAll(AuditLogEntityFilter filter, Pagination page, FetchStrategy ... fetchStrategy){
        return auditLogEntityDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntity> getAll(AuditLogEntityFilter filter, Sorting sort, FetchStrategy ... fetchStrategy){
        return auditLogEntityDao.getAll(filter, sort, fetchStrategy);
    }
    
    //------ AuditLogEntry entity methods
    @Override
    @Transactional(readOnly=false)
    public AuditLogEntry create(AuditLogEntry auditLogEntry){
        return this.auditLogEntryDao.create(auditLogEntry);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Long countAll(AuditLogEntryFilter filter){
        return this.auditLogEntryDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntry> getAll(AuditLogEntryFilter filter, FetchStrategy ... fetchStrategy){
        return this.auditLogEntryDao.getAll(filter, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntry> getAll(AuditLogEntryFilter filter, Pagination page, FetchStrategy ... fetchStrategy){
        return this.auditLogEntryDao.getAll(filter, page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<AuditLogEntry> getAll(AuditLogEntryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy){
        return this.auditLogEntryDao.getAll(filter, sort, fetchStrategy);
    }
}
