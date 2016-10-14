package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.BidDeduction;
import com.byyd.middleware.campaign.filter.BidDeductionFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface BidDeductionDao extends BusinessKeyDao<BidDeduction> {
	
	List<BidDeduction> getAll(BidDeductionFilter filter, FetchStrategy... fetchStrategy);
	List<BidDeduction> getAll(BidDeductionFilter filter, Pagination page, FetchStrategy... fetchStrategy);
	List<BidDeduction> getAll(BidDeductionFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
}
