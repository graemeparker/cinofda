package com.byyd.middleware.audit.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Audit;
import com.byyd.middleware.audit.dao.AuditDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

/**
 * @author Anuj.Saboo
 * Invoicing Audit Log for logging the queries ran at the end of each month for
 * Publisher and Advertiser invoicing.
 */
@Repository
public class AuditDaoJpaImpl extends BusinessKeyDaoJpaImpl<Audit> implements AuditDao {

}
