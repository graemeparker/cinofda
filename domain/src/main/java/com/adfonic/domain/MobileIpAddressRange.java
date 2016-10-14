package com.adfonic.domain;

import javax.persistence.*;

/** A MobileIpAddressRange represents a range of IP addresses that has been
    identified as coming from a mobile operator.
*/
@Entity
@Table(name="MOBILE_IP_ADDRESS_RANGE")
public class MobileIpAddressRange extends BusinessKey {
    private static final long serialVersionUID = 1L;

    public enum Source { ADFONIC, MASSIVE }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="START_POINT",nullable=false)
    private long startPoint;
    @Column(name="END_POINT",nullable=false)
    private long endPoint;
    @Column(name="CARRIER",length=64,nullable=false)
    private String carrier;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COUNTRY_ID",nullable=false)
    private Country country;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="OPERATOR_ID",nullable=true)
    private Operator operator; // nullable
    @Column(name="SOURCE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Source source;
    @Column(name="PRIORITY",nullable=false)
    private int priority = 0;

    public long getId() { return id; };
    
    public long getStartPoint() { return startPoint; }
    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getEndPoint() { return endPoint; }
    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

    public boolean isInRange(long value) {
        return startPoint <= value && endPoint >= value;
    }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Country getCountry() { return country; }
    public void setCountry(Country country) {
        this.country = country;
    }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Source getSource() { return source; }
    public void setSource(Source source) {
        this.source = source;
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(Object o) {
        MobileIpAddressRange other = (MobileIpAddressRange)o;
        // Sort highest priority to lowest priority
        if (this.getPriority() > other.getPriority()) {
            return -1;
        }
        else if (this.getPriority() < other.getPriority()) {
            return 1;
        }
        // Sort lowest to highest start point
        else if (this.getStartPoint() < other.getStartPoint()) {
            return -1;
        }
        else if (this.getStartPoint() > other.getStartPoint()) {
            return 1;
        }
        // Sort lowest to highest end point
        else if (this.getEndPoint() < other.getEndPoint()) {
            return -1;
        }
        else if (this.getEndPoint() > other.getEndPoint()) {
            return 1;
        }
        else {
            // If all of the above are equal, then we must have duplicate
            // data in the table.  That's what happened on test.  Tie goes
            // to whatever.  Same prio, same range, they're the same as
            // far as the sort is concerned.
            return 0;
        }
    }
}
