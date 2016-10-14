package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TargetPublisher.class)
public abstract class TargetPublisher_ {

	public static volatile SingularAttribute<TargetPublisher, Boolean> rtbSeatIdAvailable;
	public static volatile SingularAttribute<TargetPublisher, Boolean> rtb;
	public static volatile SingularAttribute<TargetPublisher, Boolean> hidden;
	public static volatile SingularAttribute<TargetPublisher, String> name;
	public static volatile SingularAttribute<TargetPublisher, Publisher> publisher;
	public static volatile SingularAttribute<TargetPublisher, Integer> displayPriority;
	public static volatile SingularAttribute<TargetPublisher, Long> id;
	public static volatile SingularAttribute<TargetPublisher, Boolean> pmpAvailable;

}

