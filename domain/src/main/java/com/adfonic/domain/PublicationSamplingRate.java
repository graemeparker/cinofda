package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PUBLICATION_SAMPLINGRATE")
public class PublicationSamplingRate extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    
    @Column(name="PUBLICATION_ID", nullable=false)
    private Long publicationId;

    @Column(name = "SAMPLING_RATE", nullable=true)
    private Integer samplingRate;

    @Override
    public long getId() {
        return id;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publication) {
        this.publicationId = publication;
    }

    public Integer getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(Integer samplingRate) {
        this.samplingRate = samplingRate;
    }

}
