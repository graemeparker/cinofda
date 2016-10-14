package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

/**
 * Persistent messages that may be displayed in the user interface.
 * Messages can be localized by the UI, usually through a properties file.
 */
@Entity
@Table(name="COMPANY_MESSAGE")
public class CompanyMessage extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=true)
    private Advertiser advertiser;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID",nullable=true)
    private Publisher publisher;
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(name="SYSTEM_NAME",length=128,nullable=false)
    private String systemName;
    @Column(name="ARG0",length=255,nullable=true)
    private String arg0;
    @Column(name="ARG1",length=255,nullable=true)
    private String arg1;
    @Column(name="ARG2",length=255,nullable=true)
    private String arg2;
    @Column(name="ARG3",length=255,nullable=true)
    private String arg3;
    @Column(name="ARG4",length=255,nullable=true)
    private String arg4;

    {
        this.creationTime = new Date();
    }
    
    CompanyMessage() {}

    /*
     * I think we can live without this...always funnel campaign or
     * creative related message through the campaign-based constructor
     * so that house ads are treated properly.
    public CompanyMessage(Advertiser advertiser, String systemName) {
        this(advertiser.getCompany(), advertiser, null, systemName);
    }
    */

    public CompanyMessage(Publisher publisher, String systemName) {
        this(publisher.getCompany(), null, publisher, systemName);
    }

    /**
     * Special constructor that "does the right thing" for house ads.
     * If the campaign is a house ad, it will set both the advertiser
     * and the publisher on this object.  Otherwise, only the advertiser
     * gets set.
     */
    public CompanyMessage(Campaign campaign, String systemName) {
        this.company = campaign.getAdvertiser().getCompany();
        this.advertiser = campaign.getAdvertiser();
        this.systemName = systemName;
        if (campaign.isHouseAd()) {
            this.publisher = company.getPublisher();
        }
    }
    
    // Androgynous support (i.e. may come in handy for house ads)
    public CompanyMessage(Company company, Advertiser advertiser, Publisher publisher, String systemName) {
        this.company = company;
        this.advertiser = advertiser;
        this.publisher = publisher;
        this.systemName = systemName;
    }

    public long getId() { return id; };
    
    public Company getCompany() { return company; }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public String getSystemName() { return systemName; }

    public Date getCreationTime() { return creationTime; }

    public String getArg0() { return arg0; }
    public void setArg0(String arg0) { this.arg0 = arg0; }

    public String getArg1() { return arg1; }
    public void setArg1(String arg1) { this.arg1 = arg1; }

    public String getArg2() { return arg2; }
    public void setArg2(String arg2) { this.arg2 = arg2; }

    public String getArg3() { return arg3; }
    public void setArg3(String arg3) { this.arg3 = arg3; }

    public String getArg4() { return arg4; }
    public void setArg4(String arg4) { this.arg4 = arg4; }
}
