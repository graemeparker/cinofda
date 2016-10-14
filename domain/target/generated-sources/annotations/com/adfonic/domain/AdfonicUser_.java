package com.adfonic.domain;

import com.adfonic.domain.AdfonicUser.Status;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdfonicUser.class)
public abstract class AdfonicUser_ {

	public static volatile SingularAttribute<AdfonicUser, String> firstName;
	public static volatile SingularAttribute<AdfonicUser, String> lastName;
	public static volatile SingularAttribute<AdfonicUser, String> password;
	public static volatile SingularAttribute<AdfonicUser, String> salt;
	public static volatile SingularAttribute<AdfonicUser, String> loginName;
	public static volatile SetAttribute<AdfonicUser, AdminRole> roles;
	public static volatile SingularAttribute<AdfonicUser, Long> id;
	public static volatile SingularAttribute<AdfonicUser, String> email;
	public static volatile SetAttribute<AdfonicUser, User> users;
	public static volatile SingularAttribute<AdfonicUser, Status> status;

}

