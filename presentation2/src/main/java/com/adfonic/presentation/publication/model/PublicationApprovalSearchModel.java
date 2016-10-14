package com.adfonic.presentation.publication.model;

public class PublicationApprovalSearchModel extends PublicationApprovalModel {

    private static final long serialVersionUID = 1L;
    
    private Integer sortFieldIndex;

    public Integer getSortFieldIndex() {
        return sortFieldIndex;
    }

    public void setSortFieldIndex(Integer sortFieldIndex) {
        this.sortFieldIndex = sortFieldIndex;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationApprovalSearchModel [sortFieldIndex=").append(sortFieldIndex).append("]");
        return builder.append("\n").append(super.toString()).toString();
    }

}
