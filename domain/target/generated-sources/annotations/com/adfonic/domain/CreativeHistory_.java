package com.adfonic.domain;

import com.adfonic.domain.Creative.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CreativeHistory.class)
public abstract class CreativeHistory_ {

	public static volatile SingularAttribute<CreativeHistory, AdfonicUser> adfonicUser;
	public static volatile SingularAttribute<CreativeHistory, Date> eventTime;
	public static volatile SingularAttribute<CreativeHistory, String> comment;
	public static volatile SingularAttribute<CreativeHistory, Long> id;
	public static volatile SingularAttribute<CreativeHistory, Creative> creative;
	public static volatile SingularAttribute<CreativeHistory, AdfonicUser> assignedTo;
	public static volatile SingularAttribute<CreativeHistory, Status> status;

}

