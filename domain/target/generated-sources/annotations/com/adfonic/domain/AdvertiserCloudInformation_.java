package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdvertiserCloudInformation.class)
public abstract class AdvertiserCloudInformation_ {

	public static volatile SingularAttribute<AdvertiserCloudInformation, Advertiser> advertiser;
	public static volatile SingularAttribute<AdvertiserCloudInformation, String> path;
	public static volatile SingularAttribute<AdvertiserCloudInformation, String> secretKey;
	public static volatile SingularAttribute<AdvertiserCloudInformation, String> accessKey;
	public static volatile SingularAttribute<AdvertiserCloudInformation, String> arn;

}

