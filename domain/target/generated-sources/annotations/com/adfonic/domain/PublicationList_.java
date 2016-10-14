package com.adfonic.domain;

import com.adfonic.domain.PublicationList.PublicationListLevel;
import java.util.Date;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationList.class)
public abstract class PublicationList_ {

	public static volatile SingularAttribute<PublicationList, Advertiser> advertiser;
	public static volatile SingularAttribute<PublicationList, String> name;
	public static volatile SingularAttribute<PublicationList, PublicationListLevel> publicationListLevel;
	public static volatile SingularAttribute<PublicationList, Company> company;
	public static volatile SingularAttribute<PublicationList, Boolean> whiteList;
	public static volatile SingularAttribute<PublicationList, Long> id;
	public static volatile SetAttribute<PublicationList, Publication> publications;
	public static volatile SingularAttribute<PublicationList, Date> snapshotDateTime;

}

