package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface DestinationDao extends BusinessKeyDao<Destination> {

    Long countAll(DestinationFilter filter);
    List<Destination> getAll(DestinationFilter filter, FetchStrategy... fetchStrategy);
    List<Destination> getAll(DestinationFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Destination> getAll(DestinationFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, List<BeaconUrl> beacons);
    List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, FetchStrategy... fetchStrategy);
    List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy);
    List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, Sorting sort, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy);
    List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, Pagination page, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy);
}
