package com.adfonic.weve.dao;


public interface OptOutServiceDao {

    public Integer saveOptOut(String deviceIds, int optout);
    
    public Integer saveOptOutEsk(String weveIds, int optout);
    
    public Long findWeveId(String deviceId, int deviceIdType);
    
}
