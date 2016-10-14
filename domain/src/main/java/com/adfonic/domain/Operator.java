package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="OPERATOR")
public class Operator extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COUNTRY_ID",nullable=false)
    private Country country;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="OPERATOR_GROUP_ID",nullable=true)
    private OperatorGroup group; // nullable
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="OPERATOR_ALIAS",joinColumns=@JoinColumn(name="OPERATOR_ID",referencedColumnName="ID"))
    @Column(name="ALIAS",length=64,nullable=false)
    private Set<String> aliases;
    @OneToMany(mappedBy="operator",fetch=FetchType.LAZY)
    private Set<OperatorAlias> operatorAliases;
    @Column(name="MOBILE_OPERATOR",nullable=false)
    private boolean mobileOperator;

    {
        this.aliases = new HashSet<String>();
        this.operatorAliases = new HashSet<OperatorAlias>();
    }

    Operator() {}

    public Operator(String name, Country country, OperatorGroup group, boolean mobileOperator) {
	this.name = name;
	this.country = country;
	this.group = group;
	this.mobileOperator = mobileOperator;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) {
	this.country = country;
    }

    public OperatorGroup getGroup() { return group; }
    public void setGroup(OperatorGroup group) { this.group = group; }

    public Set<String> getAliases() { return aliases; }

    public Set<OperatorAlias> getOperatorAliases() { return operatorAliases; }

    public boolean isMobileOperator() {
        return mobileOperator;
    }

    public void setMobileOperator(boolean mobileOperator) {
        this.mobileOperator = mobileOperator;
    }
}
