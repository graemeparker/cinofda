package com.byyd.middleware.publication.dao;

import java.util.List;
import java.util.Map;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.AdSpaceFilter;

public interface AdSpaceDao extends BusinessKeyDao<AdSpace> {

    Long countAllForPublication(Publication publication);
    List<AdSpace> getAllForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    List<AdSpace> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    
    Long countUnverifiedAdSlotsForPublisher(Publisher publisher);
    Map<Publication,Long> getUnverifiedAdSlotsForPublisherCountMap(Publisher publisher);

    Long countHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, FetchStrategy... fetchStrategy);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Pagination page, FetchStrategy... fetchStrategy);

    Long countAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, FetchStrategy... fetchStrategy);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Pagination page, FetchStrategy... fetchStrategy);

    Long countUnallocatedAdSpaceForPublisher(Publisher publisher);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);

    Long countAll(AdSpaceFilter filter);
    List<AdSpace> getAll(AdSpaceFilter filter, FetchStrategy... fetchStrategy);
    List<AdSpace> getAll(AdSpaceFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAll(AdSpaceFilter filter, Pagination page, FetchStrategy... fetchStrategy);
}
