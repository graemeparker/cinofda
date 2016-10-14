package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

/**
 * A notification flag indicates that a given condition has triggered
 * a user notification, so the same condition should not generate further
 * events (at least not until the expiration time comes around).
 */
@Entity
@Table(name="NOTIFICATION_FLAG")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR",length=16)
public abstract class NotificationFlag extends BusinessKey {

	private static final long serialVersionUID = 1L;
	
    // TODO: fork this out into subclasses when we get the hell off DataNucleus
    public enum Type { DAILY_BUDGET, OVERALL_BUDGET, LOW_BALANCE, ZERO_BALANCE, WENT_LIVE, UNSTOP, OPTIMIZE  }
        
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="CREATE_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column(name="EXPIRATION_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;
    @Column(name="TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Type type;

    {
        this.createDate = new Date();
    }
    
    NotificationFlag() {}
    
    public NotificationFlag(Company company, Type type, int ttlSeconds) {
        this(company, type, new Date(System.currentTimeMillis() + ttlSeconds * 1000L));
    }

    public NotificationFlag(Company company, Type type, Date expirationDate) {
        this.company = company;
        this.type = type;
        this.expirationDate = expirationDate;
    }

    public long getId() { return id; };
    
    public Company getCompany() {
        return company;
    }

    public Date getCreateDate() {
        return createDate;
    }
    
    public Date getExpirationDate() {
        return expirationDate;
    }

    public Type getType() {
        return type;
    }
}
