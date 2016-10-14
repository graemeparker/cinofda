package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(OptimisationReportCompanyPreferences.class)
public abstract class OptimisationReportCompanyPreferences_ {

	public static volatile SingularAttribute<OptimisationReportCompanyPreferences, Company> company;
	public static volatile SetAttribute<OptimisationReportCompanyPreferences, OptimisationReportFields> reportFields;
	public static volatile SingularAttribute<OptimisationReportCompanyPreferences, Long> id;

}

