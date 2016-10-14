package com.adfonic.dto.publication;

import java.math.BigDecimal;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.publication.adspace.AdSpaceDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publisher.PublisherDto;

public class PublicationDto extends PublicationInfoDto {

    private static final long serialVersionUID = 2L;

    @DTOCascade
    @Source(value = "publisher")
    private PublisherDto publisher;

    @Source(value = "urlString")
    private String urlString;

    @Source(value = "description")
    private String description;

    @Source(value = "backfillEnabled")
    private boolean backfillEnabled;

    @Source(value = "autoApproval")
    private boolean autoApproval;

    @Source(value = "minAge")
    private int minAge;

    @Source(value = "maxAge")
    private int maxAge;

    @Source(value = "genderMix")
    private BigDecimal genderMix;

    @DTOCascade
    @Source(value = "publicationType")
    private PublicationtypeDto publicationType;

    @DTOCascade
    @Source(value = "adSpaces")
    private List<AdSpaceDto> adSpaces;

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBackfillEnabled() {
        return backfillEnabled;
    }

    public void setBackfillEnabled(boolean backfillEnabled) {
        this.backfillEnabled = backfillEnabled;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public BigDecimal getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
    }

    public boolean isAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public PublicationtypeDto getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(PublicationtypeDto publicationType) {
        this.publicationType = publicationType;
    }

    public List<AdSpaceDto> getAdSpaces() {
        return adSpaces;
    }

    public void setAdSpaces(List<AdSpaceDto> adSpaces) {
        this.adSpaces = adSpaces;
    }
}
