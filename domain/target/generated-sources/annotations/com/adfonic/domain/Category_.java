package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Category.class)
public abstract class Category_ {

	public static volatile SingularAttribute<Category, String> iabId;
	public static volatile SingularAttribute<Category, Category> parent;
	public static volatile SingularAttribute<Category, String> name;
	public static volatile SingularAttribute<Category, Channel> channel;
	public static volatile SingularAttribute<Category, Long> id;

}

