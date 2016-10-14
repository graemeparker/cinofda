package com.adfonic.domain;

import com.adfonic.domain.User.Status;
import java.util.Date;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(User.class)
public abstract class User_ {

	public static volatile SingularAttribute<User, Date> lastLogin;
	public static volatile SingularAttribute<User, String> lastName;
	public static volatile SingularAttribute<User, Country> country;
	public static volatile SingularAttribute<User, String> referralTypeOther;
	public static volatile SingularAttribute<User, String> preferences;
	public static volatile SingularAttribute<User, String> salt;
	public static volatile SingularAttribute<User, Date> creationTime;
	public static volatile SingularAttribute<User, String> securityQuestion;
	public static volatile SingularAttribute<User, String> securityAnswer;
	public static volatile SetAttribute<User, Role> roles;
	public static volatile SetAttribute<User, Advertiser> advertisers;
	public static volatile SingularAttribute<User, String> timeZone;
	public static volatile SingularAttribute<User, Boolean> emailOptIn;
	public static volatile SingularAttribute<User, String> firstName;
	public static volatile SingularAttribute<User, String> password;
	public static volatile SingularAttribute<User, String> referralType;
	public static volatile SingularAttribute<User, String> phoneNumber;
	public static volatile SingularAttribute<User, String> alias;
	public static volatile SingularAttribute<User, Company> company;
	public static volatile SingularAttribute<User, Long> id;
	public static volatile SingularAttribute<User, String> developerKey;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SingularAttribute<User, Status> status;

}

