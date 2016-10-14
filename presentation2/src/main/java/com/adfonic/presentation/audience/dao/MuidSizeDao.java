package com.adfonic.presentation.audience.dao;

public interface MuidSizeDao {

    Long getMuidSegmentSize(Long segmentId);

    void buildSingleSegmentSize(Long segmentId);
}
