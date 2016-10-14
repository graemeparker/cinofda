package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AffiliateProgram.class)
public abstract class AffiliateProgram_ {

	public static volatile SingularAttribute<AffiliateProgram, String> name;
	public static volatile SingularAttribute<AffiliateProgram, UploadedContent> logo;
	public static volatile SingularAttribute<AffiliateProgram, String> description;
	public static volatile SingularAttribute<AffiliateProgram, Long> id;
	public static volatile SingularAttribute<AffiliateProgram, BigDecimal> depositBonus;
	public static volatile SingularAttribute<AffiliateProgram, String> affiliateId;

}

