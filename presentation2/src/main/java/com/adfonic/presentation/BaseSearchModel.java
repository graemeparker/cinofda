package com.adfonic.presentation;

import java.io.Serializable;

public class BaseSearchModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    protected Integer first = 0;
    protected Integer pageSize = 0;
    protected String sortField;
    protected Boolean ascending;

    public BaseSearchModel() {

    }

    public BaseSearchModel(Integer first, Integer pageSize, String sortField, Boolean ascending) {
        super();
        this.first = first;
        this.pageSize = pageSize;
        this.sortField = sortField;
        this.ascending = ascending;
    }

    public Integer getFirst() {
        return first;
    }

    public void setFirst(Integer first) {
        this.first = first;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseSearchDto [first=").append(first).append(", pageSize=").append(pageSize).append(", sortField=").append(sortField).append(", ascending=")
                .append(ascending).append("]");
        return builder.toString();
    }

}
