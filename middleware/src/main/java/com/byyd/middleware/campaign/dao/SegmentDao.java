package com.byyd.middleware.campaign.dao;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Segment;
import com.byyd.middleware.iface.dao.BusinessKeyDao;

public interface SegmentDao extends BusinessKeyDao<Segment> {

    boolean isAdSpaceAlreadyAllocated(Segment segment, AdSpace adSpace);
    
    Long countBrowsersForSegment (Segment segment);
    Long countCategoriesForSegment (Segment segment);
    Long countExcludedCategoriesForSegment (Segment segment);
    Long countChannelsForSegment (Segment segment);    
    Long countCountriesForSegment (Segment segment);
    Long countGeotargetsForSegment (Segment segment);
    Long countIpAddressesForSegment (Segment segment);
    Long countModelsForSegment (Segment segment);     
    Long countExcludedModelsForSegment (Segment segment);     
    Long countOperatorsForSegment (Segment segment);
    Long countPlatformsForSegment (Segment segment);
    Long countPublishersForSegment (Segment segment);
    Long countVendorsForSegment (Segment segment);

}
