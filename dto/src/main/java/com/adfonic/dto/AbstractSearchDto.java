package com.adfonic.dto;

public class AbstractSearchDto {

    private Integer first = 0;
    private Integer pageSize = 0;
    private String sortField;
    private Boolean ascending;
    
    public AbstractSearchDto(){
        
    }
    
    public AbstractSearchDto(Integer first, Integer pageSize, String sortField, Boolean ascending) {
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
}
