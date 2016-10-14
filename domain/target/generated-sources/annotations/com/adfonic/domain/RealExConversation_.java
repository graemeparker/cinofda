package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RealExConversation.class)
public abstract class RealExConversation_ {

	public static volatile SingularAttribute<RealExConversation, Advertiser> advertiser;
	public static volatile SingularAttribute<RealExConversation, BigDecimal> amount;
	public static volatile SingularAttribute<RealExConversation, String> orderId;
	public static volatile SingularAttribute<RealExConversation, String> requestDetails;
	public static volatile SingularAttribute<RealExConversation, String> responseDetails;
	public static volatile SingularAttribute<RealExConversation, String> urlInvoked;
	public static volatile SingularAttribute<RealExConversation, AdfonicUser> adfonicUser;
	public static volatile SingularAttribute<RealExConversation, AccountDetail> accountDetail;
	public static volatile SingularAttribute<RealExConversation, String> currency;
	public static volatile SingularAttribute<RealExConversation, Long> id;
	public static volatile SingularAttribute<RealExConversation, User> user;
	public static volatile SingularAttribute<RealExConversation, Account> account;
	public static volatile SingularAttribute<RealExConversation, RealExConversationStatus> status;
	public static volatile SingularAttribute<RealExConversation, Date> timestamp;

}

