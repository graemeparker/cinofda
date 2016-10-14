package com.byyd.middleware.account.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.IpAddressRange;
import com.byyd.middleware.account.dao.IpAddressRangeDao;
import com.byyd.middleware.account.filter.IpAddressRangeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class IpAddressRangeDaoJpaImpl extends BusinessKeyDaoJpaImpl<IpAddressRange> implements IpAddressRangeDao {
    
    protected StringBuilder getAutoApprovalCreativesForPublicationQuery() {
        // for auto approval campaigns we are retrieving "Approved" creatives
        return new StringBuilder("SELECT DISTINCT(ID)")
            .append(" FROM IP_ADDRESS_RANGE  IpAddressRange")
            .append(" INNER JOIN COMPANY_WHITELIST_IP_ADDRESS_RANGE cwiar ON IpAddressRange.ID = cwiar.IP_ADDRESS_RANGE_ID")
            .append(" WHERE COMPANY_ID = ?");
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<IpAddressRange> getAll(IpAddressRangeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder query = getAutoApprovalCreativesForPublicationQuery();
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(filter.getCompany().getId()));
        if(sort != null) {
            query.append(" ORDER BY " + sort.toString(true));
        }
        List<Long> ids = this.findByNativeQueryPositionalParameters(query.toString(), page, list);
        if(ids != null && !ids.isEmpty()) {
            return this.getObjectsByIds(ids, sort, fetchStrategy);
        }
        return  new ArrayList<IpAddressRange>();
    }
    
}
