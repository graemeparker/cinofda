package com.adfonic.domain;

import com.adfonic.domain.AdvertiserStoppage.Reason;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdvertiserStoppage.class)
public abstract class AdvertiserStoppage_ {

	public static volatile SingularAttribute<AdvertiserStoppage, Advertiser> advertiser;
	public static volatile SingularAttribute<AdvertiserStoppage, Reason> reason;
	public static volatile SingularAttribute<AdvertiserStoppage, Date> reactivateDate;
	public static volatile SingularAttribute<AdvertiserStoppage, Long> id;
	public static volatile SingularAttribute<AdvertiserStoppage, Date> timestamp;

}

