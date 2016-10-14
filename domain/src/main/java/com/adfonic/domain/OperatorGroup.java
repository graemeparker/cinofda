package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * Provides a means of grouping related operators, e.g. Vodafone Group
 * or T-Mobile International.  The groupings will be used primarily
 * for interface purposes.
 */
@Entity
@Table(name="OPERATOR_GROUP")
public class OperatorGroup extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @OneToMany(mappedBy="group",fetch=FetchType.LAZY)
    private Set<Operator> operators;

    {
	this.operators = new HashSet<Operator>();
    }

    OperatorGroup() {}

    public OperatorGroup(String name) {
	this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Operator> getOperators() { return operators; }
}
