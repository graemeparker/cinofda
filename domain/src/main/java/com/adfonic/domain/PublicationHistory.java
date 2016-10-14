package com.adfonic.domain;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="PUBLICATION_HISTORY")
public class PublicationHistory extends BusinessKey {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_ID",nullable=false)
    private Publication publication;

    @Column(name="EVENT_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Publication.Status status;

    @Column(name="AD_OPS_STATUS",length=32,nullable=true)
    @Enumerated(EnumType.STRING)
    private Publication.AdOpsStatus adOpsStatus;

    @Lob
    @Column(name="COMMENT",nullable=true)
    private String comment;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADFONIC_USER_ID",nullable=true)
    private AdfonicUser adfonicUser;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ASSIGNED_TO_ADFONIC_USER_ID",nullable=true)
    private AdfonicUser assignedTo;

    PublicationHistory() {}
    
    public PublicationHistory(Publication publication) {
        this.eventTime = new Date();
        this.publication = publication;
        this.status = publication.getStatus();
        this.adOpsStatus = publication.getAdOpsStatus();
        this.assignedTo = publication.getAssignedTo();
    }

    public long getId() {
        return id;
    }

    public Publication getPublication() {
        return publication;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public Publication.Status getStatus() {
        return status;
    }

    public Publication.AdOpsStatus getAdOpsStatus() {
        return adOpsStatus;
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
