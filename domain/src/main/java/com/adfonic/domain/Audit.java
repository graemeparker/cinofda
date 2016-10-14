package com.adfonic.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="AUDIT")
public class Audit extends BusinessKey {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="TRANSACTION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;

    @Column(name="CLASS_NAME",length=255,nullable=false)
    private String className;
    
    @Lob 
    @Column(name="QUERY", nullable=false)
    private String query;
    

    public Audit(String className, String query, Date transactionTime) {
    	this.className = className;
    	this.query = query;
    	this.transactionTime = transactionTime;
	}
    
	public long getId() { return id; }
	public Date getTransactionTime() { return transactionTime; }
	public String getClassName() { return className; }
	public String getQuery() { return query; }
}
