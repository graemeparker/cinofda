package com.adfonic.dto.advertiser;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class AdvertiserCloudInformationDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value="advertiser")
    private AdvertiserDto advertiser;
    
    @Source(value="arn")
    private String arn;
    
    @Source(value="accessKey")
    private String accessKey;
    
    @Source(value="secretKey")
    private String secretKey;
    
    @Source(value="path")
    private String path;

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
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

}
