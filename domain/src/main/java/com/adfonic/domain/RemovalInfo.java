package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="REMOVAL_INFO")
public class RemovalInfo extends BusinessKey {
    private static final long serialVersionUID = 3L;

    public enum RemovalType { SYSTEM, AD_OPS, OTHER, UNREMOVED, AUTO, USER }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="REMOVAL_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private RemovalType removalType;
    @Column(name="REMOVAL_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removalTime;

    public RemovalInfo(RemovalType removalType) {
        this.removalType = removalType;
        this.removalTime = new Date();
    }

    public RemovalInfo(RemovalInfo copyFrom) {
        this.removalType = copyFrom.removalType;
        this.removalTime = copyFrom.removalTime;
    }

    public RemovalInfo() {
        this.removalType = RemovalType.OTHER;
        this.removalTime = new Date();
    }

    public long getId() {
        return id;
    }

    public RemovalType getRemovalType() {
        return removalType;
    }
    public void setRemovalType(RemovalType removalType) {
        this.removalType = removalType;
    }

    public Date getRemovalTime() {
        return removalTime;
    }
    public void setRemovalTime(Date removalTime) {
        this.removalTime = removalTime;
    }

    public boolean isUnremoval() {
        return removalType == RemovalType.UNREMOVED;
    }
}
