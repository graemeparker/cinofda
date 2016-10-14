package com.adfonic.domain.cache.dto.datacollector.publication;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublicationDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private PublisherDto publisher;

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "PublicationDto {" + getId() + ", publisher=" + publisher + "}";
    }

}
