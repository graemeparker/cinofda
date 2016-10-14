package com.adfonic.domain.cache.dto.adserver.creative;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PublicationListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long publicationListId;
    private boolean whiteList;
    private Date snapshotDateTime;
    private Set<Long> publicationIds;

    public PublicationListDto() {
        publicationIds = new HashSet<Long>();
    }

    public Long getPublicationListId() {
        return publicationListId;
    }

    public void setPublicationListId(Long publicationListId) {
        this.publicationListId = publicationListId;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    public Date getSnapshotDateTime() {
        return snapshotDateTime;
    }

    public void setSnapshotDateTime(Date snapshotDateTime) {
        this.snapshotDateTime = snapshotDateTime;
    }

    public Set<Long> getPublicationIds() {
        return publicationIds;
    }

    public void setPublicationIds(Set<Long> publicationIds) {
        this.publicationIds = publicationIds;
    }

    @Override
    public String toString() {
        return "PublicationListDto {" + publicationListId + ", whiteList=" + whiteList + ", snapshotDateTime=" + snapshotDateTime + ", publicationIds=" + publicationIds + "}";
    }

}
