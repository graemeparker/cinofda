package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CreativeDao extends BusinessKeyDao<Creative> {

    Long countApprovedCreativesForPublication(Publication publication);
    List<Creative> getApprovedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<Creative> getApprovedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getApprovedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    List<Integer> getAllApprovedCreativesForPublication(Publication publication);

    Long countDeniedCreativesForPublication(Publication publication);
    List<Creative> getDeniedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<Creative> getDeniedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getDeniedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    List<Integer> getAllDeniedCreativesForPublication(Publication publication);
    
    boolean approveCreativeForPublication(Publication publication, Creative creative);
    boolean denyCreativeForPublication(Publication publication, Creative creative);

    Long countCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Pagination page, FetchStrategy... fetchStrategy);

    Long countAll(CreativeFilter filter);
    List<Creative> getAll(CreativeFilter filter, FetchStrategy... fetchStrategy);
    List<Creative> getAll(CreativeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getAll(CreativeFilter filter, Pagination page, FetchStrategy... fetchStrategy);

}
