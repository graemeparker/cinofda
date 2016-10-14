package com.byyd.elasticsearch.model;


public class SortingInfo {
    
    public enum SortingOrder {
        ASC, 
        DESC
    }
    
    private String field;
    private SortingOrder order = SortingOrder.ASC;
    
    public SortingInfo(String field, SortingOrder order) {
        super();
        this.field = field;
        this.order = order;
    }
    
    public String getField() {
        return field;
    }
    
    public SortingOrder getOrder() {
        return order;
    }
    
    @Override
    public String toString(){
        return "Sorting " + this.order + " by " + this.field;
    }
}
