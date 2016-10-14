package com.adfonic.domain;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="CREATIVE_HISTORY")
public class CreativeHistory extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=false)
    private Creative creative;

    @Column(name="EVENT_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Creative.Status status;

    @Lob
    @Column(name="COMMENT",nullable=true)
    private String comment;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADFONIC_USER_ID",nullable=true)
    private AdfonicUser adfonicUser;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ASSIGNED_TO_ADFONIC_USER_ID",nullable=true)
    private AdfonicUser assignedTo;

    CreativeHistory() {}
    
    public CreativeHistory(Creative creative) {
        this.eventTime = new Date();
        this.creative = creative;
        this.status = creative.getStatus();
        this.assignedTo = creative.getAssignedTo();
    }

    public long getId() {
        return id;
    }

    public Creative getCreative() {
        return creative;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public Creative.Status getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public AdfonicUser getAdfonicUser() {
        return adfonicUser;
    }
    public void setAdfonicUser(AdfonicUser adfonicUser) {
        this.adfonicUser = adfonicUser;
    }

    public AdfonicUser getAssignedTo() {
        return assignedTo;
    }
}
