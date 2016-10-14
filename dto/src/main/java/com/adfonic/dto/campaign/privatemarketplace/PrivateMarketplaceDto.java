package com.adfonic.dto.campaign.privatemarketplace;

import java.io.Serializable;

import org.jdto.annotation.Source;

import com.adfonic.dto.publisher.PublisherDto;

public class PrivateMarketplaceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Source(value = "reference")
    private String dealId;

    @Source(value = "publisher")
    private PublisherDto publisher;

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }
}
