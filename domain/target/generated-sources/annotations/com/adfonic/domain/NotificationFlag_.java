package com.adfonic.domain;

import com.adfonic.domain.NotificationFlag.Type;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(NotificationFlag.class)
public abstract class NotificationFlag_ {

	public static volatile SingularAttribute<NotificationFlag, Company> company;
	public static volatile SingularAttribute<NotificationFlag, Long> id;
	public static volatile SingularAttribute<NotificationFlag, Type> type;
	public static volatile SingularAttribute<NotificationFlag, Date> createDate;
	public static volatile SingularAttribute<NotificationFlag, Date> expirationDate;

}

