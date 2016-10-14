package com.byyd.middleware.publication.dao;

import java.util.List;
import java.util.Map;

import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.PublicationFilter;

/**
 * For publication queries, it was determined that we needed to be able to use a different "read-only" datasource, but
 * while still using Spring's @Autowired annotations. The use of such annotations require each bean definition to be
 * of a unique type. So, to avoid code duplication, a base interface and base implementation have been inserted to extend
 * BusinessKeyDao and BusinessKeyDaoJpaImpl. These base classes contain all the code, and any addition of anything should be
 * made to them.
 * 
 * What this allows are 2 separate DAOs autorwired in XXXXXManagerJpaImpl, and methods required to run using the read-only
 * datasource are them copied under a different name and told to use the alternate DAO wired in. The application can then
 * specifically call the "regular" method or opt to call the new "read-only" method.
 * 
 * Things to pay attention to:
 * 
 * - because the generics resolution happen in the base class, the final classes (PublicationDaoJpaImpl and 
 *   PublicationReadOnlyDaoJpaImpl) cannot infer the type like the other DAOs do. The base implementation must 
 *   explicitely set the type as part of its construction, like so:
 *   
 *       public BasePublicationDaoJpaImpl() {
 *            super();
 *            this.setType(Publication.class);
 *        }
 *
 * - to force the read-only DAO to use the alternate datasource, the alternate EntityManagerFactory must be wired into a local
 *   instance, and that local instance used in an override of getEntityManagerFactory(), like so:
 *   
 *       @Autowired(required=false)
 *        @Qualifier("readOnlyEntityManagerFactory")
 *            private EntityManagerFactory entityManagerFactory;
 *            
 *            public EntityManagerFactory getEntityManagerFactory() {
 *                return entityManagerFactory;
 *            }
 *
 *  
 * @author pierre
 *
 */

public interface BasePublicationDao extends BusinessKeyDao<Publication> {

    Publication getByName(String name, Publisher publisher, FetchStrategy... fetchStrategy);

    Publication getByPublisherAndRtbId(Publisher publisher, String rtbId, FetchStrategy ... fetchStrategy);

    Long countForStatus(Publisher publisher, List<Publication.Status> statuses);
    List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, FetchStrategy... fetchStrategy);
    List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, Pagination page, FetchStrategy... fetchStrategy);
    List<Publication> getAllLike(String name, Pagination page, FetchStrategy... fetchStrategy);

    Long countAll(PublicationFilter filter);
    List<Publication> getAll(PublicationFilter filter, FetchStrategy ... fetchStrategy);
    List<Publication> getAll(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<Publication> getAll(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
    Map<Publication,Long> getPublicationsWithPendingAdsMapForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);

    Long countPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Pagination page, FetchStrategy... fetchStrategy);

    Long countPublicationsForPublicationList(PublicationList publicationList);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy);
}
