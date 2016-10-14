package com.adfonic.domain;

import com.adfonic.domain.Publication.AdOpsStatus;
import com.adfonic.domain.Publication.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationHistory.class)
public abstract class PublicationHistory_ {

	public static volatile SingularAttribute<PublicationHistory, AdfonicUser> adfonicUser;
	public static volatile SingularAttribute<PublicationHistory, Publication> publication;
	public static volatile SingularAttribute<PublicationHistory, Date> eventTime;
	public static volatile SingularAttribute<PublicationHistory, AdOpsStatus> adOpsStatus;
	public static volatile SingularAttribute<PublicationHistory, String> comment;
	public static volatile SingularAttribute<PublicationHistory, Long> id;
	public static volatile SingularAttribute<PublicationHistory, AdfonicUser> assignedTo;
	public static volatile SingularAttribute<PublicationHistory, Status> status;

}

