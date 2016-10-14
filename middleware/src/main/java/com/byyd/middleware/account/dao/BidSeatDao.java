package com.byyd.middleware.account.dao;

import java.util.Collection;
import java.util.List;

import com.adfonic.domain.BidSeat;
import com.byyd.middleware.account.filter.BidSeatFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface BidSeatDao extends BusinessKeyDao<BidSeat> {
    
    void deleteAll(Collection<BidSeat> bidSeats);
    
    Long countAll(BidSeatFilter filter);
    List<BidSeat> getAll(BidSeatFilter filter, FetchStrategy... fetchStrategy);
    List<BidSeat> getAll(BidSeatFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<BidSeat> getAll(BidSeatFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    List<BidSeat> getAll(BidSeatFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
}
