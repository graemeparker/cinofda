package com.byyd.middleware.auditlog.dao.jpa;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.auditlog.AuditLogEntry;
import com.adfonic.domain.auditlog.AuditLogEntry_;
import com.byyd.middleware.auditlog.dao.AuditLogEntryDao;
import com.byyd.middleware.auditlog.filter.AuditLogEntryFilter;

@Repository
public class AuditLogEntryDAOJpaImpl extends AbstracAuditLogDataSourceDaoJpaImpl<AuditLogEntry, AuditLogEntryFilter> 
                                  implements AuditLogEntryDao<AuditLogEntry, AuditLogEntryFilter> {
    
    @Override
    protected Predicate getPredicate(Root<AuditLogEntry> root, AuditLogEntryFilter filter){
        Predicate auditLogEntitiesExpression = null;
        Predicate auditLogEntryTypesExpression = null;
        
        if (CollectionUtils.isNotEmpty(filter.getAuditEntities())){
            auditLogEntitiesExpression = root.get(AuditLogEntry_.auditLogEntity).in(filter.getAuditEntities());
        }
        
        if (CollectionUtils.isNotEmpty(filter.getAuditLogEntryTypes())){
            auditLogEntryTypesExpression = root.get(AuditLogEntry_.auditLogEntryType).in(filter.getAuditLogEntryTypes());
        }
        
        return and(auditLogEntitiesExpression, auditLogEntryTypesExpression);
    }
}
