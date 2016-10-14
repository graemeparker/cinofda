package com.adfonic.domain;

import com.adfonic.domain.PublicationProvidedInfo.InfoType;
import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationProvidedInfo.class)
public abstract class PublicationProvidedInfo_ {

	public static volatile SingularAttribute<PublicationProvidedInfo, String> stringValue;
	public static volatile SingularAttribute<PublicationProvidedInfo, InfoType> infoType;
	public static volatile SingularAttribute<PublicationProvidedInfo, Publication> publication;
	public static volatile SingularAttribute<PublicationProvidedInfo, BigDecimal> decimalValue;
	public static volatile SingularAttribute<PublicationProvidedInfo, Integer> integerValue;
	public static volatile SingularAttribute<PublicationProvidedInfo, Long> id;

}

