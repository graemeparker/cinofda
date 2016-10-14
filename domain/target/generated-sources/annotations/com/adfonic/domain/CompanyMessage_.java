package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CompanyMessage.class)
public abstract class CompanyMessage_ {

	public static volatile SingularAttribute<CompanyMessage, Advertiser> advertiser;
	public static volatile SingularAttribute<CompanyMessage, String> arg3;
	public static volatile SingularAttribute<CompanyMessage, String> arg2;
	public static volatile SingularAttribute<CompanyMessage, String> arg4;
	public static volatile SingularAttribute<CompanyMessage, Date> creationTime;
	public static volatile SingularAttribute<CompanyMessage, String> systemName;
	public static volatile SingularAttribute<CompanyMessage, String> arg1;
	public static volatile SingularAttribute<CompanyMessage, String> arg0;
	public static volatile SingularAttribute<CompanyMessage, Publisher> publisher;
	public static volatile SingularAttribute<CompanyMessage, Company> company;
	public static volatile SingularAttribute<CompanyMessage, Long> id;

}

