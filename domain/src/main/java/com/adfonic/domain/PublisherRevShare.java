package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="PUBLISHER_REV_SHARE")
public class PublisherRevShare extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID", insertable=false, updatable=false, nullable=true)
    private Publisher publisher;

    @Column(name="REV_SHARE",nullable=false)
    private BigDecimal revShare;

    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    PublisherRevShare() {}

    // Lifecycle managed by Publisher, so package-scope constructor
    PublisherRevShare(Publisher publisher, BigDecimal revShare, Date startDate) {
        this.publisher = publisher;
        this.revShare = revShare;
        this.startDate = startDate;
    }

    public long getId() { return id; };

    public Publisher getPublisher() {
        return publisher;
    }

    public BigDecimal getRevShare() {
        return revShare;
    }
    public void setRevShare(BigDecimal revShare) {
        this.revShare = revShare;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
