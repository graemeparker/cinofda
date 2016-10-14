package com.adfonic.domain;

import com.adfonic.domain.Creative.Status;
import java.util.Date;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Creative.class)
public abstract class Creative_ {

	public static volatile SingularAttribute<Creative, Date> creationTime;
	public static volatile SingularAttribute<Creative, Date> endDate;
	public static volatile SingularAttribute<Creative, Destination> destination;
	public static volatile SingularAttribute<Creative, String> externalID;
	public static volatile SingularAttribute<Creative, Language> language;
	public static volatile SingularAttribute<Creative, ExtendedCreativeType> extendedCreativeType;
	public static volatile SingularAttribute<Creative, Date> submissionTime;
	public static volatile SingularAttribute<Creative, AdfonicUser> assignedTo;
	public static volatile SingularAttribute<Creative, Date> lastUpdated;
	public static volatile MapAttribute<Creative, Publication, RemovalInfo> removedPublications;
	public static volatile SingularAttribute<Creative, Segment> segment;
	public static volatile SingularAttribute<Creative, Long> id;
	public static volatile SingularAttribute<Creative, Boolean> pluginBased;
	public static volatile SingularAttribute<Creative, String> englishTranslation;
	public static volatile MapAttribute<Creative, DisplayType, AssetBundle> assetBundleMap;
	public static volatile CollectionAttribute<Creative, PublisherAuditedCreative> publishersAuditedCreative;
	public static volatile SingularAttribute<Creative, Format> format;
	public static volatile ListAttribute<Creative, CreativeHistory> history;
	public static volatile SingularAttribute<Creative, Integer> priority;
	public static volatile SingularAttribute<Creative, Date> approvedDate;
	public static volatile SetAttribute<Creative, ExtendedCreativeTemplate> extendedCreativeTemplates;
	public static volatile SingularAttribute<Creative, Boolean> allowExternalAudit;
	public static volatile SingularAttribute<Creative, Boolean> sslCompliant;
	public static volatile SingularAttribute<Creative, String> name;
	public static volatile SingularAttribute<Creative, Campaign> campaign;
	public static volatile SingularAttribute<Creative, Boolean> closedMode;
	public static volatile SetAttribute<Creative, CreativeAttribute> creativeAttributes;
	public static volatile SingularAttribute<Creative, Status> status;
	public static volatile MapAttribute<Creative, String, String> extendedData;

}

