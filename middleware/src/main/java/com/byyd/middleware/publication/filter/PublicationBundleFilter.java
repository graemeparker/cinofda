package com.byyd.middleware.publication.filter;

import java.util.List;

import com.adfonic.domain.Publication;
import com.byyd.middleware.iface.dao.LikeSpec;

public class PublicationBundleFilter {
    
    private List<Long> ids;
    private Long publicationId;
    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive = false;
    private List<Publication> publications;
    
    /**
     * true: Apply filter for disclosed publications
     * false: Apply filter for undisclosed publications
     * null: do not apply any filter
     */
    private Boolean disclosedPublications;
    
    /**
     * true: Apply filter for approved publications
     * false: Apply filter for unapproved publications
     * null: do not apply any filter
     */
    private Boolean approvedPublications;
    
    public List<Long> getIds() {
        return ids;
    }
    public PublicationBundleFilter setIds(List<Long> ids) {
        this.ids = ids;
        return this;
    }
    public Long getPublicationId() {
        return publicationId;
    }
    public PublicationBundleFilter setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
        return this;
    }
    public String getName() {
        return name;
    }
    public PublicationBundleFilter setName(String name) {
        this.name = name;
        return this;
    }
    public LikeSpec getNameLikeSpec() {
        return nameLikeSpec;
    }
    public PublicationBundleFilter setNameLikeSpec(LikeSpec nameLikeSpec) {
        this.nameLikeSpec = nameLikeSpec;
        return this;
    }
    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public void setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
    }
    public List<Publication> getPublications() {
        return publications;
    }
    public PublicationBundleFilter setPublications(List<Publication> publications) {
        this.publications = publications;
        return this;
    }
    public Boolean getDisclosedPublications() {
        return disclosedPublications;
    }
    public void setDisclosedPublications(Boolean disclosedPublications) {
        this.disclosedPublications = disclosedPublications;
    }
    public Boolean getApprovedPublications() {
        return approvedPublications;
    }
    public void setApprovedPublications(Boolean approvedPublications) {
        this.approvedPublications = approvedPublications;
    }
}
