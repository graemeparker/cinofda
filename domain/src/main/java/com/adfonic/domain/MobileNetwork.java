package com.adfonic.domain;

import javax.persistence.*;

/** A MobileNetwork links MCC+MNC to an Operator */
@Entity
@Table(name="MOBILE_NETWORK")
public class MobileNetwork extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="MCC",length=3,nullable=false)
    private String mcc;
    @Column(name="MNC",length=5,nullable=false)
    private String mnc;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="OPERATOR_ID",nullable=false)
    private Operator operator;

    public long getId() { return id; };

    public String getMcc() {
        return mcc;
    }
    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }
    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}
