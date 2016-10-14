package com.adfonic.dto.publication.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.publisher.PublisherDto;

/***
 * Object to get the campaigns from the database according to the parameters.
 * */
public class PublicationSearchDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    
    private String name;

    private PublisherDto publisher;

    private String status;
    
    private Collection<PublicationTypeAheadDto> publications = new ArrayList<PublicationTypeAheadDto>(0);

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public Collection<PublicationTypeAheadDto> getPublications() {
        return publications;
    }

    public void setPublications(Collection<PublicationTypeAheadDto> publications) {
        this.publications = publications;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationSearchDto [publications=");
        builder.append(publications);
        builder.append(", advertiser=");
        builder.append(publisher);
        builder.append(", name=");
        builder.append(name);
        builder.append(", status=");
        builder.append(status);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

}
