package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TargettedPublisher.class)
public abstract class TargettedPublisher_ {

	public static volatile SingularAttribute<TargettedPublisher, Boolean> rtb;
	public static volatile SingularAttribute<TargettedPublisher, String> displayName;
	public static volatile SingularAttribute<TargettedPublisher, Publisher> publisher;
	public static volatile SingularAttribute<TargettedPublisher, Integer> displayPriority;
	public static volatile SingularAttribute<TargettedPublisher, Long> id;

}

