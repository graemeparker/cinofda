package com.adfonic.dto.company;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class IpAddressRangeDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source("id")
    private long id;
    @Source("startPoint")
    private long startPoint;
    @Source("endPoint")
    private long endPoint;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

}
