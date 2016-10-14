package com.adfonic.domain;

import com.adfonic.domain.VerificationCode.CodeType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(VerificationCode.class)
public abstract class VerificationCode_ {

	public static volatile SingularAttribute<VerificationCode, String> code;
	public static volatile SingularAttribute<VerificationCode, CodeType> codeType;
	public static volatile SingularAttribute<VerificationCode, Long> id;
	public static volatile SingularAttribute<VerificationCode, User> user;

}

