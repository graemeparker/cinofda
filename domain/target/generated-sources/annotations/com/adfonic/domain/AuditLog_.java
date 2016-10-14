package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AuditLog.class)
public abstract class AuditLog_ {

	public static volatile SingularAttribute<AuditLog, String> newValue;
	public static volatile SingularAttribute<AuditLog, String> systemId;
	public static volatile SingularAttribute<AuditLog, AdfonicUser> adfonicUser;
	public static volatile SingularAttribute<AuditLog, String> field;
	public static volatile SingularAttribute<AuditLog, Long> id;
	public static volatile SingularAttribute<AuditLog, String> oldValue;
	public static volatile SingularAttribute<AuditLog, User> user;
	public static volatile SingularAttribute<AuditLog, String> objectId;
	public static volatile SingularAttribute<AuditLog, Date> timestamp;

}

