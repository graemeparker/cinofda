package com.byyd.elasticsearch.model;

public class Pagination {
    
    private Integer from;
    private Integer size;

    public Pagination(Integer from, Integer size) {
        super();
        this.from = from;
        this.size = size;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getSize() {
        return size;
    }
    
    @Override
    public String toString(){
        return "Pagination from " + this.from + " and returning " + this.size;
    }
}
