package com.adfonic.domain.auditlog;

import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AuditLogEntry.class)
public abstract class AuditLogEntry_ {

	public static volatile SingularAttribute<AuditLogEntry, Date> oldValueDate;
	public static volatile SingularAttribute<AuditLogEntry, Long> newValueInt;
	public static volatile SingularAttribute<AuditLogEntry, BigDecimal> newValueDecimal;
	public static volatile SingularAttribute<AuditLogEntry, AuditLogEntryType> auditLogEntryType;
	public static volatile SingularAttribute<AuditLogEntry, Date> newValueDate;
	public static volatile SingularAttribute<AuditLogEntry, Boolean> newValueBoolean;
	public static volatile SingularAttribute<AuditLogEntry, Long> oldValueInt;
	public static volatile SingularAttribute<AuditLogEntry, AuditLogEntity> auditLogEntity;
	public static volatile SingularAttribute<AuditLogEntry, BigDecimal> oldValueDecimal;
	public static volatile SingularAttribute<AuditLogEntry, String> oldValueVarchar;
	public static volatile SingularAttribute<AuditLogEntry, String> newValueBlob;
	public static volatile SingularAttribute<AuditLogEntry, String> name;
	public static volatile SingularAttribute<AuditLogEntry, Long> id;
	public static volatile SingularAttribute<AuditLogEntry, Boolean> oldValueBoolean;
	public static volatile SingularAttribute<AuditLogEntry, String> newValueVarchar;
	public static volatile SingularAttribute<AuditLogEntry, Date> timestamp;
	public static volatile SingularAttribute<AuditLogEntry, String> oldValueBlob;

}

