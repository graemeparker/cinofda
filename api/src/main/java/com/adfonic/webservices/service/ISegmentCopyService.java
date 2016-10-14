package com.adfonic.webservices.service;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Segment;
import com.adfonic.webservices.dto.SegmentDTO;

public interface ISegmentCopyService {

    public void copyToSegment(SegmentDTO segmentDTO, Segment segment, Campaign.Status campaignStatus);
}
