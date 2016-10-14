package com.adfonic.adserver.controller.dbg.dto;

import java.util.Date;

/**
 * 
 * @author mvanek
 *
 */
public class DbgStoppageDto {

    private Date timestampAt;

    private Date reactivateAt;

    DbgStoppageDto() {
        // marshall
    }

    public DbgStoppageDto(Date timestampAt, Date reactivateAt) {
        this.timestampAt = timestampAt;
        this.reactivateAt = reactivateAt;
    }

    public Date getTimestampAt() {
        return timestampAt;
    }

    public void setTimestampAt(Date timestampAt) {
        this.timestampAt = timestampAt;
    }

    public Date getReactivateAt() {
        return reactivateAt;
    }

    public void setReactivateAt(Date reactivateAt) {
        this.reactivateAt = reactivateAt;
    }

}
