package com.adfonic.domain;

import javax.persistence.*;

/** A MobileIpAddressRange represents a range of IP addresses that has been
    identified as coming from a mobile operator.
*/
@Entity
@Table(name="IP_ADDRESS_RANGE")
public class IpAddressRange extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="START_POINT",nullable=false)
    private long startPoint;
    @Column(name="END_POINT",nullable=false)
    private long endPoint;
    
    IpAddressRange(){};
    
    public IpAddressRange(long startPoint,long endPoint){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public int compareTo(Object o) {
        MobileIpAddressRange other = (MobileIpAddressRange)o;
        // Sort lowest to highest start point
        if (this.getStartPoint() < other.getStartPoint()) {
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
