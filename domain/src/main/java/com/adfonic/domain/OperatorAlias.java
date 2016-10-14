package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="OPERATOR_ALIAS")
public class OperatorAlias extends BusinessKey {
    private static final long serialVersionUID = 1L;

    public enum Type { MASSIVE, QUOVA }
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="OPERATOR_ID",nullable=false)
    private Operator operator;
    @Column(name="ALIAS",length=64,nullable=false)
    private String alias;
    @Column(name="TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private Type type;

    OperatorAlias() {}

    public OperatorAlias(Operator operator, String alias, Type type) {
        this.operator = operator;
        this.alias = alias;
        this.type = type;
    }

    public long getId() { return id; };

    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
}
