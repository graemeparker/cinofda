package com.adfonic.domain;

import com.adfonic.domain.Publication.AdOpsStatus;
import com.adfonic.domain.Publication.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationApprovalDashboardView.class)
public abstract class PublicationApprovalDashboardView_ {

	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> accountManagerLastName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, Date> creationTime;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> assignedToLastName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> companyName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> externalID;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> accountManagerFirstName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, Date> submissionTime;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, Boolean> publisherIsKey;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> publicationTypeName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> assignedToFirstName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> name;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, AdOpsStatus> adOpsStatus;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> accountManagerEmail;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, Long> id;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, String> friendlyName;
	public static volatile SingularAttribute<PublicationApprovalDashboardView, Status> status;

}

