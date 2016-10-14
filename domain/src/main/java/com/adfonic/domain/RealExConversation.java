package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An exchange with RealEx will yield the creation of an AccountDetail entry if the transaction is successful.
 * To that end, the Account to use is passed to the manager, and stored in the conversation object, to be
 * retrieved upon confirmation processing. The Advertiser object is passed for information, in anticipation
 * of future needs where the Account object might come from another entity (Publisher, for example). In other words,
 * the Advertiser should not be used to determine what Account to use, it's only there for reference.
 *
 * @author pierre
 *
 */
@Entity
@Table(name="REAL_EX_CONVERSATION")
public class RealExConversation extends BusinessKey {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private RealExConversationStatus status;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID",nullable=false)
    private User user;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADFONIC_USER_ID",nullable=true)
    private AdfonicUser adfonicUser;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=true)
    private Advertiser advertiser;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_ID",nullable=false)
    private Account account;
    @Column(name="TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(name="URL_INVOKED",length=255,nullable=false)
    private String urlInvoked;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;
    @Column(name="CURRENCY",length=10,nullable=false)
    private String currency;
    @Column(name="ORDER_ID",length=64,nullable=false)
    private String orderId;
    @Lob
    @Column(name="REQUEST_DETAILS", nullable=false)
    private String requestDetails;
    @Lob
    @Column(name="RESPONSE_DETAILS", nullable=false)
    private String responseDetails;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_DETAIL_ID",nullable=true)
    private AccountDetail accountDetail;


    {
        this.timestamp = new Date();
        this.requestDetails = "";
        this.responseDetails = "";
    }

    RealExConversation() { }

    public RealExConversation(User user, AdfonicUser adfonicUser, Account account, Advertiser advertiser, BigDecimal amount, String currency, String orderId, String urlInvoked) {
        this.user = user;
        this.adfonicUser = adfonicUser;
        this.account = account;
        this.advertiser = advertiser;
        this.urlInvoked = urlInvoked;
        this.amount = amount;
        this.currency = currency;
        this.orderId = orderId;
        this.status = RealExConversationStatus.STATUS_PENDING;
    }

    public void setRequestDetails(String requestDetails) {
        this.requestDetails = requestDetails;
    }

    public void setResponseDetails(String responseDetails) {
        this.responseDetails = responseDetails;
    }

    public long getId() { return id; };

    public User getUser() {
        return user;
    }

    public AdfonicUser getAdfonicUser() {
        return adfonicUser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getRequestDetails() {
        return requestDetails;
    }

    public String getResponseDetails() {
        return responseDetails;
    }

    public String getUrlInvoked() {
        return urlInvoked;
    }

    public AccountDetail getAccountDetail() {
        return accountDetail;
    }

    public void setAccountDetail(AccountDetail accountDetail) {
        this.accountDetail = accountDetail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCurrency() {
        return currency;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
    }

    public RealExConversationStatus getStatus() {
        return status;
    }

    public void setStatus(RealExConversationStatus status) {
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }


}
