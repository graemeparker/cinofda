package com.byyd.middleware.publication.dao;
import java.util.List;

import com.adfonic.domain.PublicationApprovalDashboardView;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.PublicationFilter;

public interface PublicationApprovalDashboardViewDao extends BusinessKeyDao<PublicationApprovalDashboardView> {

    Long countAll(PublicationFilter filter);
    List<PublicationApprovalDashboardView> getAll(PublicationFilter filter);
    List<PublicationApprovalDashboardView> getAll(PublicationFilter filter, Pagination page);
    List<PublicationApprovalDashboardView> getAll(PublicationFilter filter, Sorting sort);
}
