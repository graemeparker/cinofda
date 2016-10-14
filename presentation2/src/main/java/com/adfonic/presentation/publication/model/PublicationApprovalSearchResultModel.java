package com.adfonic.presentation.publication.model;

import java.math.BigDecimal;
import java.util.List;

public class PublicationApprovalSearchResultModel {
    
    private List<PublicationApprovalModel> resultList;
    private BigDecimal resultCount;

    public PublicationApprovalSearchResultModel(List<PublicationApprovalModel> resultList, BigDecimal resultCount) {
        super();
        this.resultList = resultList;
        this.resultCount = resultCount;
    }

    public List<PublicationApprovalModel> getResultList() {
        return resultList;
    }

    public void setResultList(List<PublicationApprovalModel> resultList) {
        this.resultList = resultList;
    }

    public BigDecimal getResultCount() {
        return resultCount;
    }

    public void setResultCount(BigDecimal resultCount) {
        this.resultCount = resultCount;
    }

}
