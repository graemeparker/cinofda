package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PostalAddress.class)
public abstract class PostalAddress_ {

	public static volatile SingularAttribute<PostalAddress, String> firstName;
	public static volatile SingularAttribute<PostalAddress, String> lastName;
	public static volatile SingularAttribute<PostalAddress, Country> country;
	public static volatile SingularAttribute<PostalAddress, String> address2;
	public static volatile SingularAttribute<PostalAddress, String> city;
	public static volatile SingularAttribute<PostalAddress, String> address1;
	public static volatile SingularAttribute<PostalAddress, String> postcode;
	public static volatile SingularAttribute<PostalAddress, Long> id;
	public static volatile SingularAttribute<PostalAddress, String> state;

}

