package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ADVERTISER_CLOUD_INFORMATION")
public class AdvertiserCloudInformation extends BusinessKey{
    private static final long serialVersionUID = 1L;
    
    @Id
    @JoinColumn(name = "ADVERTISER_ID", nullable = false)
    @ManyToOne(targetEntity = Advertiser.class)
    private Advertiser advertiser;
    
    @Column(name="ARN",length=100,nullable=false)
    private String arn;
    
    @Column(name="ACCESS_KEY",length=20,nullable=false)
    private String accessKey;
    
    @Column(name="SECRET_KEY",length=40,nullable=false)
    private String secretKey;
    
    @Column(name="PATH",length=512,nullable=false)
    private String path;
    
    public AdvertiserCloudInformation() {
    }
    
    public AdvertiserCloudInformation(Advertiser advertiser, String arn, String accessKey, String secretKey, String path) {
        this.advertiser = advertiser;
        this.arn = arn;
    	this.accessKey = accessKey;
    	this.secretKey = secretKey;
    	this.path = path;
    }

    @Override
    public long getId() {
        long id = 0L;
        if(advertiser!=null){
            id = advertiser.getId();
        }
        return id;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString(){
        return "[adveriserId=" + (this.advertiser==null?null:this.advertiser.getId()) + "] " + 
               "[arn=" + this.arn + "] " + 
               "[accesskey=" + this.accessKey + "] " + 
               "[secretkey=" + this.secretKey + "] " + 
               "[bucket=" + this.path + "] ";
    }
}
