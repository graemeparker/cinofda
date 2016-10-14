package com.adfonic.domain.auditlog;

import com.adfonic.domain.auditlog.AuditLogEntity.AuditOperation;
import com.adfonic.domain.auditlog.AuditLogEntity.UserType;
import java.util.Date;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AuditLogEntity.class)
public abstract class AuditLogEntity_ {

	public static volatile SingularAttribute<AuditLogEntity, String> entityName;
	public static volatile SingularAttribute<AuditLogEntity, Long> entityId;
	public static volatile SingularAttribute<AuditLogEntity, String> userEmail;
	public static volatile SingularAttribute<AuditLogEntity, Long> id;
	public static volatile SingularAttribute<AuditLogEntity, String> source;
	public static volatile SingularAttribute<AuditLogEntity, UserType> userType;
	public static volatile SingularAttribute<AuditLogEntity, String> userName;
	public static volatile SingularAttribute<AuditLogEntity, AuditOperation> auditOperation;
	public static volatile SingularAttribute<AuditLogEntity, Long> userId;
	public static volatile ListAttribute<AuditLogEntity, AuditLogEntry> auditLogEntries;
	public static volatile SingularAttribute<AuditLogEntity, String> transactionId;
	public static volatile SingularAttribute<AuditLogEntity, Date> timestamp;

}

