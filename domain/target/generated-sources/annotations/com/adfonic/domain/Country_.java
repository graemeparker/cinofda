package com.adfonic.domain;

import com.adfonic.domain.Country.TaxRegime;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Country.class)
public abstract class Country_ {

	public static volatile SingularAttribute<Country, Double> displayLatitude;
	public static volatile SingularAttribute<Country, Double> displayLongitude;
	public static volatile SingularAttribute<Country, Integer> displayMapZoom;
	public static volatile SingularAttribute<Country, Boolean> hidden;
	public static volatile SingularAttribute<Country, String> isoCode;
	public static volatile SetAttribute<Country, Operator> operators;
	public static volatile SingularAttribute<Country, TaxRegime> taxRegime;
	public static volatile SingularAttribute<Country, String> isoAlpha3;
	public static volatile SingularAttribute<Country, String> name;
	public static volatile SingularAttribute<Country, Long> id;
	public static volatile SingularAttribute<Country, String> dialPrefix;
	public static volatile SingularAttribute<Country, Region> region;

}

