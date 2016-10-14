package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="TRANSACTION_NOTIFICATION")
public class TransactionNotification extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID",nullable=false)
    private User user;
    
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;
    
    @Column(name="REFERENCE",length=255,nullable=true)
    private String reference;
    
    @Column(name="TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    {
        this.timestamp = new Date();
    }
    
    TransactionNotification() {}
    
    public TransactionNotification(Advertiser advertiser, User user, BigDecimal amount, String reference) {
        this.advertiser = advertiser;
        this.user = user;
        this.amount = amount;
        this.reference = reference;
    }
    
    public long getId() { 
        return id; 
    };
    
    public Advertiser getAdvertiser() { 
        return advertiser; 
    }
    
    public User getUser() {
        return user;
    }

    public BigDecimal getAmount() { 
        return amount; 
    }
    
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }

    public String getReference() { 
        return reference; 
    }
    
    public void setReference(String reference) { 
        this.reference = reference; 
    }

    public Date getTimestamp() {
        return timestamp;
    }
    
}
