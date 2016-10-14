package com.byyd.middleware.auditlog.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntity_;
import com.byyd.middleware.auditlog.dao.AuditLogEntityDao;
import com.byyd.middleware.auditlog.filter.AuditEntityInformation;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;

@Repository
public class AuditLogEntityDAOJpaImpl extends AbstracAuditLogDataSourceDaoJpaImpl<AuditLogEntity, AuditLogEntityFilter> implements AuditLogEntityDao<AuditLogEntity, AuditLogEntityFilter>{
    
    @Override
    protected Predicate getPredicate(Root<AuditLogEntity> root, AuditLogEntityFilter filter){
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate auditLogEntitiesInformationExpression = null;
        Predicate sourceExpression = null;
        Predicate userTypeExpression = null;
        Predicate userIdExpression = null;
        Predicate userNameExpression = null;
        Predicate userEmailExpression = null;
        Predicate transactionIdExpression = null;
        Predicate fromDateExpression = null;
        Predicate toDateExpression = null;
        
        if (CollectionUtils.isNotEmpty(filter.getAuditEntitiesInformation())){
            Predicate currentExpression;
            for (AuditEntityInformation auditEntityInformation : filter.getAuditEntitiesInformation()){
                currentExpression = and(criteriaBuilder.equal(root.get(AuditLogEntity_.entityName), auditEntityInformation.getEntityName()), 
                                        criteriaBuilder.equal(root.get(AuditLogEntity_.entityId), auditEntityInformation.getEntityId()));
                if (auditLogEntitiesInformationExpression==null){
                    auditLogEntitiesInformationExpression = currentExpression;
                }else{
                    auditLogEntitiesInformationExpression = or(auditLogEntitiesInformationExpression, currentExpression);
                }
            }
        }
        
        if (filter.getSource() != null){
            sourceExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.source), filter.getSource());
        }
        
        if (filter.getUserType() != null){
            userTypeExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.userType), filter.getUserType());
        }
        
        if (filter.getUserId() != null){
            userIdExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.userId), filter.getUserId());
        }
        
        if (filter.getUserName() != null){
            userNameExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.userName), filter.getUserName());
        }
        
        if (filter.getUserEmail() != null){
            userEmailExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.userEmail), filter.getUserEmail());
        }
        
        if (filter.getTransactionId() != null){
            transactionIdExpression = criteriaBuilder.equal(root.get(AuditLogEntity_.transactionId), filter.getTransactionId());
        }
        
        if(filter.getFromDate() != null) {
            fromDateExpression = criteriaBuilder.greaterThanOrEqualTo(root.get(AuditLogEntity_.timestamp), filter.getFromDate());
        }
        
        if(filter.getToDate() != null) {
            toDateExpression = criteriaBuilder.lessThanOrEqualTo(root.get(AuditLogEntity_.timestamp), filter.getToDate());
        }
        
        return and(auditLogEntitiesInformationExpression, sourceExpression, userTypeExpression, userIdExpression, 
                   userNameExpression, userEmailExpression, transactionIdExpression, fromDateExpression, toDateExpression);
    }

}
