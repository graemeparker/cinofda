package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UploadedContent.class)
public abstract class UploadedContent_ {

	public static volatile SingularAttribute<UploadedContent, byte[]> data;
	public static volatile SingularAttribute<UploadedContent, String> externalID;
	public static volatile SingularAttribute<UploadedContent, Long> id;
	public static volatile SingularAttribute<UploadedContent, ContentType> contentType;

}

