package com.byyd.middleware.iface.dao.jpa;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.BusinessKey;
import com.adfonic.domain.HasPrimaryKeyId;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyFactory;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.exception.BusinessKeyDaoException;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

/**
 * This class is the base class for all custom implementations of DAOs for objects extending BusinessKey. On top of all the
 * methods supported by GenericDaoJpaImpl, this base class offers Criteria API support, as well as convenience methods
 * to apply Fetch Strategies of queries.
 * @author pierre
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BusinessKeyDaoJpaImpl<T extends BusinessKey> extends GenericDaoJpaImpl<T,Long> implements BusinessKeyDao<T>{
    private static final transient Logger LOG = Logger.getLogger(BusinessKeyDaoJpaImpl.class.getName());
    
    private static volatile Boolean entityAuditorEnabled = true;
    
    @Autowired
    private FetchStrategyFactory fetchStrategyFactory;

    private EntityAuditor entityAuditor;
    
    @Autowired(required=false)
    private AuditLogJpaListener auditLogJpaListener;


    public BusinessKeyDaoJpaImpl() {
        super();
    }

    public BusinessKeyDaoJpaImpl(Class<T> type) {
        super(type);
    }

    private EntityAuditor getEntityAuditor() {
        if (entityAuditor != null) {
            return entityAuditor;
        } else if (!entityAuditorEnabled) {
            return null;
        }

        // Yeah, I know the double-locking pattern is frowned upon, but
        // the onus is on you to prove that this use of it is flawed...
        synchronized (this) {
            if (entityAuditor == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Establishing EntityAuditor");
                }
                try {
                    entityAuditor = AdfonicBeanDispatcher.getBean(EntityAuditor.class);
                } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                    // No EntityAuditor bean defined...no-op
                    synchronized (BusinessKeyDaoJpaImpl.class) {
                        if (entityAuditorEnabled) {
                            LOG.warning("No EntityAuditor bean defined, auditing capability disabled");
                            entityAuditorEnabled = false;
                        }
                    }
                    return null;
                }
            }
            return entityAuditor;
        }
    }


    // ----------------------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------------------
    /**
     * Get the PK of a given object to be used on AuditLog
     * @param o the object
     * @return the object's PK if instance of HasPrimaryKeyId
     * @throws IllegalArgumentException if the object is not an instance of HasPrimaryKeyId
     */
    public static Long getObjectPk(Object o) {
        Long objectPk;
        if (o instanceof HasPrimaryKeyId) {
            objectPk = ((HasPrimaryKeyId)o).getId();
        } else {
            throw new IllegalArgumentException("No way to derive PK for " + o.getClass().getName() + " (non HasPrimaryKeyId)");
        }
        return objectPk;
    }

    @Override
    public T create(T object) {
        this.persist(object);
        return object;
    }

    @Override
    public void delete(T object) {
        if(object != null) {
            super.remove(object);
        }
    }

    @Override
    public T update(T object) {
        EntityAuditor auditor = getEntityAuditor();
        if(auditor != null) {
            auditor.ensureBaselineDataPresence(object);
            auditor.expectOnPostUpdate(object);
        }
        preUpdateAuditLogActions(object);
        boolean mergeSucceeded = false;
        T returnValue;
        try {
            returnValue = this.merge(object);
            mergeSucceeded = true;
        } finally {
            if (mergeSucceeded){
                if(auditor != null) {
                    // Make sure all the expected onPostUpdate calls were made
                    auditor.invokePendingOnPostUpdates();
                }
                postUpdateAuditLogActions(object);
            }
        }
        return returnValue;
    }

    private void preUpdateAuditLogActions(T object) {
        if (auditLogJpaListener!=null){
            auditLogJpaListener.saveCurrentPersistedValues(object);
        }
    }
    
    private void postUpdateAuditLogActions(T object){
        if (auditLogJpaListener!=null){
            auditLogJpaListener.removePendientUpdate(object);
        }
    }

    public void delete(List<T> list) {
        if(list != null) {
            for(T t : list) {
                delete(t);
            }
        }
    }

    // ----------------------------------------------------------------------------
    // Convenience methods
    // ----------------------------------------------------------------------------
    /**
     * Convenience method to retrieve an list of objects by their internal IDs while applying a FetchStrategy to their fields.
     * @param ids the list of IDs to query with
     * @param fetchStrategy optional FetchStrategyImpl or FetchStrategyName
     * @return the object found or a NoResultException if none are found
     */
    @Override
    public List<T> getObjectsByIds(Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return this.getObjectsByIds(ids, null, null, fetchStrategy);
    }

    @Override
    public List<T> getObjectsByIds(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getObjectsByIds(ids, null, sort, fetchStrategy);
    }

    @Override
    public List<T> getObjectsByIds(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getObjectsByIds(ids, page, page.getSorting(), fetchStrategy);
    }

    protected List<T> getObjectsByIds(Collection<Long> ids, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager()
                .getCriteriaBuilder();
        
        Predicate predicate = root.get("id").in(ids);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<T> select = criteriaQuery.select(root);
        criteriaQuery.distinct(true);

        processOrderBy(criteriaBuilder, criteriaQuery, root, sort);

        return findAll(select, page);
    }

    /**
    * Convenience method to retrieve an object by its internal ID while applying a FetchStrategy to its fields.
    * @param id the ID to query with
    * @param fetchStrategyNames optional FetchStrategy name
    * @return the object found or null if none are found
    */
    @Override
    public T getById(Long id, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get("id"), id);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<T> select = criteriaQuery.select(root);

        return find(select);
    }

    /**
    * Convenience method to retrieve an object by its external ID while applying a FetchStrategy to its fields.
    * @param externalId the ID to query with
    * @param fetchStrategyNames optional FetchStrategy name
    * @return the object found or a NoResultException if none are found
    */
    @Override
    public T getByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get("externalID"), externalId);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<T> select = criteriaQuery.select(root);

        return find(select);
    }
    /**
    * Convenience method to retrieve a List<Object> by a given List<String> of external ID while applying a FetchStrategy to its fields.
    * @param externalsId the IDs to query with
    * @param fetchStrategyNames optional FetchStrategy name
    * @return the List of objects found or a NoResultException if none are found
    */
    @Override
    public List<T> getByExternalIds(List<String> externalsId, String propertyDate,Date startDate,Date endDate,FetchStrategy... fetchStrategy) {
        List<Predicate> predicates=new ArrayList<Predicate>(0);
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        Expression<T> exp = root.get("externalID");
        Predicate predicate = exp.in(externalsId);
        predicates.add(predicate);
        if(startDate!=null){
            Predicate minRange = criteriaBuilder.greaterThanOrEqualTo(
                    root.<Date>get(propertyDate), startDate);
            predicates.add(minRange);
        }
        if(endDate!=null){
            Predicate maxRange = criteriaBuilder.lessThanOrEqualTo(
                    root.<Date>get(propertyDate), endDate);
            predicates.add(maxRange);
        }
        Predicate[] predicateArray=new Predicate[predicates.size()];
        int k=0;
        for(Predicate p:predicates){
            predicateArray[k]=p;
            k++;
        }
        criteriaQuery = criteriaQuery.where(predicateArray);
        CriteriaQuery<T> select = criteriaQuery.select(root    );
        return findAll(select);
    }    

    /**
     * Convenience method to retrieve an object by its name while applying a FetchStrategy to its fields.
     * The test is case-sensitive.
     * @param name
     * @param fetchStrategyName
     * @return
     */
    @Override
    public T getByName(String name, FetchStrategy... fetchStrategy) {
        return getByName(name, true, fetchStrategy);
    }
    /**
    * Convenience method to retrieve an object by its name while applying a FetchStrategy to its fields.
    * The test can be case-sensitive or not.
    * @param name
    * @param caseSensitive whether or not the test is case sesnsitive
    * @param fetchStrategyName
    * @return
    */
    @Override
    @SuppressWarnings("rawtypes")
    public T getByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = null;
        if(caseSensitive) {
            predicate = criteriaBuilder.equal(root.get("name"), name);
        } else {
            predicate = criteriaBuilder.equal(criteriaBuilder.lower((Expression)root.get("name")), name.toLowerCase());
        }
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<T> select = criteriaQuery.select(root);

        return find(select);
     }
    /**
     * Convenience method to retrieve an object by its system name while applying a FetchStrategy to its fields.
     * The test will be case-sensitive.
     * @param name
      * @param fetchStrategyName
     * @return
     */
     @Override
    public T getBySystemName(String name, FetchStrategy... fetchStrategy) {
         return getBySystemName(name, true, fetchStrategy);
     }
    /**
     * Convenience method to retrieve an object by its system name while applying a FetchStrategy to its fields.
     * The test can be case-sensitive or not.
     * @param name
     * @param caseSensitive whether or not the test is case sesnsitive
     * @param fetchStrategyName
     * @return
     */
     @Override
    @SuppressWarnings("rawtypes")
     public T getBySystemName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
         CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
         CriteriaQuery<T> criteriaQuery = container.getQuery();
         Root<T> root = container.getRoot();
         CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
         Predicate predicate = null;
         if(caseSensitive) {
             predicate = criteriaBuilder.equal(root.get("systemName"), name);
         } else {
             predicate = criteriaBuilder.equal(criteriaBuilder.lower((Expression)root.get("systemName")), name.toLowerCase());
         }
         criteriaQuery = criteriaQuery.where(predicate);
         CriteriaQuery<T> select = criteriaQuery.select(root);

         return find(select);
     }
     //---------------------------------------------------------------------------------------------------------------------------------
    /**
    * Convenience method to retrieve all instances of T without any restriction
    * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
    */
    @Override
    public List<T> getAll(FetchStrategy... fetchStrategy) {
        return getAll(null, null, fetchStrategy);
    }
    /**
    * Convenience method to retrieve all instances of T without any restriction, and sorting them
     * @param sort the Sorting spec
     * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
     */
    @Override
    public List<T> getAll(Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(null, sort, fetchStrategy);
    }
    /**
    * Convenience method to retrieve a page of instances of T without any restriction, with possible sorting
     * @param page the page spec to return, possibly with a Sorting spec
     * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
     */
    @Override
    public List<T> getAll(Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(page, page.getSorting(), fetchStrategy);
    }
    /**
    * Protected method actually running the query
     * @param page the page spec to return, possibly with a Sorting spec
     * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
     */
    protected List<T> getAll(Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        criteriaQuery = criteriaQuery.select(root);
        criteriaQuery.distinct(true);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    /**
     * Convenience method to retrieve all instances of T without any restriction
     */
    @Override
    public Long countAll() {
        return super.countAll().longValue();
    }
    //------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------
    @Override
    public Long countAllForName(String name, LikeSpec like, boolean caseSensitive) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(this.getType());

        Predicate predicate = getNamePredicate(name, caseSensitive, like, root, criteriaBuilder);

        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);

    }


    @Override
    public List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return getAllForName(name, like, caseSensitive, null, null, fetchStrategy);
    }

    @Override
    public List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForName(name, like, caseSensitive, null, sort, fetchStrategy);
    }

    @Override
    public List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForName(name, like, caseSensitive, page, page.getSorting(), fetchStrategy);
    }

    protected List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getNamePredicate(name, caseSensitive, like, root, criteriaBuilder);

        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @SuppressWarnings("rawtypes")
    private Predicate getNamePredicate(String name, boolean caseSensitive, LikeSpec like, Root<T> root, CriteriaBuilder criteriaBuilder) {
        Predicate predicate = null;
        if(like == null) {
            // Default to equals
            if(caseSensitive) {
                predicate = criteriaBuilder.equal(root.get("name"), name);
            } else {
                predicate = criteriaBuilder.like(criteriaBuilder.lower((Expression)root.get("name")), name.toLowerCase());
            }
        } else {
            if(caseSensitive) {
                predicate = criteriaBuilder.like((Expression)root.get("name"), like.getPattern(name));
            } else {
                predicate = criteriaBuilder.like(criteriaBuilder.lower((Expression)root.get("name")), like.getPattern(name.toLowerCase()));
            }
        }
        return predicate;
    }

    //------------------------------------------------------------------------------------------
    /**
     *
     * @param predicates
     * @return
     */
    protected Predicate and(Predicate... predicates) {
        if(predicates == null || predicates.length == 0) {
            return null;
        }
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = null;
        for(int i = 0;i < predicates.length;i++) {
            if(predicates[i] != null) {
                if(predicate == null) {
                    predicate = predicates[i];
                } else {
                    predicate = criteriaBuilder.and(predicate, predicates[i]);
                }
            }
        }
        return predicate;
    }

    /**
     *
     * @param predicates
     * @return
     */
    protected Predicate or(Predicate... predicates) {
        if(predicates == null || predicates.length == 0) {
            return null;
        }
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = null;
        for(int i = 0;i < predicates.length;i++) {
            if(predicates[i] != null) {
                if(predicate == null) {
                    predicate = predicates[i];
                } else {
                    predicate = criteriaBuilder.or(predicate, predicates[i]);
                }
            }
        }
        return predicate;
    }
    // ----------------------------------------------------------------------------
    // Criteria API and FetchStrategy support
    // ----------------------------------------------------------------------------
    public void setFetchStrategyFactory(FetchStrategyFactory fetchStrategyFactory) {
        this.fetchStrategyFactory = fetchStrategyFactory;
    }
    
    protected String getFieldNameForType(Class<?> targetClass) {
        Field[] fields = this.getType().getDeclaredFields();
        for(Field field : fields) {
            if(field.getType().getCanonicalName().equals(targetClass.getCanonicalName())) {
                return field.getName();
            }
        }
        return null;
    }
    
    protected boolean isNullable(String fieldName) throws NoSuchFieldException {
        Field field = this.getType().getDeclaredField(fieldName);
        return isNullable(field);
    }
    
    protected boolean isNullable(Field field) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if(joinColumn != null) {
            return joinColumn.nullable();
        }
        Column column = field.getAnnotation(Column.class);
        if(column != null) {
            return column.nullable();
        }
         return true; // Will trigger a left join
    }

    protected CriteriaQuery<T> processOrderBy(CriteriaQuery<T> query, Root<T> root, Sorting sort) {
        return processOrderBy(getTransactionalEntityManager().getCriteriaBuilder(), query, root, sort);
    }
    
    @SuppressWarnings("rawtypes")
    protected Join createJoin(Attribute attribute, From currentFrom, JoinType joinType) {
        Join join = null;
        if (attribute instanceof SingularAttribute) {
            SingularAttribute a = (SingularAttribute)attribute;
            join = currentFrom.join(a, joinType);
        } else if (attribute instanceof SetAttribute) {
            SetAttribute a = (SetAttribute)attribute;
            join = currentFrom.join(a, joinType);
         } else if (attribute instanceof MapAttribute) {
             MapAttribute a = (MapAttribute)attribute;
             join = currentFrom.join(a, joinType);
         } else if (attribute instanceof ListAttribute) {
             ListAttribute a = (ListAttribute)attribute;
             join = currentFrom.join(a, joinType);
         } else if (attribute instanceof CollectionAttribute) {
             CollectionAttribute a = (CollectionAttribute)attribute;
             join = currentFrom.join(a, joinType);
         }
        return join;
    }

    protected CriteriaQuery<T> processOrderBy(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> root, Sorting sort) {
        return processOrderBy(criteriaBuilder, query, root, sort, null);
    }
    
    protected CriteriaQuery<T> processOrderBy(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> root, Sorting sort, Map<String, Join> joins) {
        try {
            if(sort != null) {
                List<SortOrder> sortOrders = sort.getSortOrders();
                if(sortOrders != null && !sortOrders.isEmpty()) {
                    // Store what <T> is
                    Class<?> rootClazz = root.getJavaType();
                    // Store the metamodel class for rootClazz
                    Class<?> rootMetamodelClazz = Class.forName(rootClazz.getCanonicalName() + "_", false, Thread.currentThread().getContextClassLoader());
                    // Create all Order objects
                    List<Order> orders = createOrderObjects(criteriaBuilder, root, sortOrders, rootClazz, rootMetamodelClazz, joins);
                    query.orderBy(orders.toArray(new Order[orders.size()]));
                }
            }
            return query;
        } catch(BusinessKeyDaoException bde) {
            throw bde;
        } catch(Exception e) {
            throw new BusinessKeyDaoException(e);
        }
    }

    private List<Order> createOrderObjects(CriteriaBuilder criteriaBuilder, 
                                           Root<T> root, List<SortOrder> sortOrders, 
                                           Class<?> rootClazz, 
                                           Class<?> rootMetamodelClazz,
                                           Map<String, Join> joins) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
        List<Order> orders = new ArrayList<Order>(sortOrders.size());
        
        for(SortOrder sortOrder : sortOrders) {
            String fieldName = sortOrder.getFieldName();
            Direction direction = sortOrder.getDirection();
            if(fieldName.indexOf('.') != -1) {
                try{
                    createOrderDottedNotation(orders, fieldName, direction, criteriaBuilder, root, rootClazz, rootMetamodelClazz);
                }catch(NoSuchFieldException nsfe){
                    if (!createOrderUsingAlias(orders, fieldName, direction, criteriaBuilder, root, joins)){
                        throw nsfe;
                    }
                }
            } else {
                createOrderNotDottedNotation(orders, sortOrder, fieldName, direction, criteriaBuilder, root, rootClazz, rootMetamodelClazz);
            }
        }
        return orders;
    }

    @SuppressWarnings("rawtypes")
    private boolean createOrderUsingAlias(List<Order> orders, String fieldName, Direction direction, CriteriaBuilder criteriaBuilder, Root<T> currentFrom, Map<String, Join> joins) {
        boolean orderCreated = false;

        if (joins!=null){
            String[] tokens = StringUtils.split(fieldName, '.');
            if(tokens.length == 2) { // Nonsensical input, we are expecting 2 tokens
                Join join = joins.get(tokens[0]);
                if (join!=null){
                    addOrder(orders, direction, criteriaBuilder, join, tokens[1]);
                    orderCreated = true;
                }
            }
        }
        
        return orderCreated;
    }

    @SuppressWarnings("rawtypes")
    private void createOrderDottedNotation(List<Order> orders, String fieldName, Direction direction, CriteriaBuilder criteriaBuilder, Root<T> root, Class<?> rootClazz, Class<?> rootMetamodelClazz) throws IllegalAccessException, ClassNotFoundException,NoSuchFieldException {
        // Dotted notation. The class is ignored.
        String[] tokens = StringUtils.split(fieldName, '.');
        if(tokens.length > 1) { // Nonsensical input, we are expecting at least 2 tokens
            // It is assumed that the first token is a field from the root class
            Class<?> currentMetamodelClazz = rootMetamodelClazz;
            Class<?> currentClazz = rootClazz;
            From currentFrom = root;
            for(int t = 0;t < tokens.length;t++) {
                String token = tokens[t];
                if(t == tokens.length - 1) {
                    // This is the last entry in the token list, meaning the actual field to
                    // sort by. Grab the last From recorded and create the Order object based on it
                    addOrder(orders, direction, criteriaBuilder, currentFrom, token);
                } else {
                    // This is one of the tokens in the list, figure out if we already have a Join
                    // for it and if not, create one, create the Order object, store it, 
                    // and init all the "current" objects for the next iteration. Note that we also
                    // are testing the join type. If we need a LEFT OUTER but find an INNER, a new join
                    // will be created. 
                    Field field = getClassField(currentClazz, token);
                    
                    // Get the type of the field and see if we have a join on that type hanging off of the last join created
                    Class<?> fieldType = field.getType();
                    boolean found = false;
                    Join targetJoin = null;
                    JoinType joinType = isNullable(field) ? JoinType.LEFT : JoinType.INNER;
                    // Attempt to locate a suitable join in the query thus far
                    Set<Join> joins = ((From) currentFrom).getJoins();
                    if(joins != null) {
                        for(Join join : joins) {
                            Class<?> joinFromClass = join.getParentPath().getJavaType();
                            Class<?> joinToClazz = join.getJavaType();
                            // See if this is the join we're looking for
                            if(joinFromClass.getCanonicalName().equals(currentClazz.getCanonicalName()) &&
                               joinToClazz.getCanonicalName().equals(fieldType.getCanonicalName()) &&
                               join.getJoinType().equals(joinType)) {
                                // Yup
                                targetJoin = join;
                                found = true;
                                break;
                            }
                        }
                    }
                    
                    if(!found) {
                        // We must create a new join
                        try{
                            Field metaModelField = currentMetamodelClazz.getDeclaredField(token);
                            Attribute attribute = (Attribute) metaModelField.get(rootMetamodelClazz);
                            if(attribute == null) {
                                LOG.fine("Could not get MetaModel field value");
                            } else {
                                targetJoin = createJoin(attribute, currentFrom, joinType);
                                found = (targetJoin != null);
                            }
                        } catch(NoSuchFieldException e){
                            LOG.fine("Could not get MetaModel field");
                        }
                    }
                    
                    if(found) {
                        // All good, init objects for the next iteration
                        currentFrom = targetJoin;
                        currentClazz = fieldType;
                        currentMetamodelClazz = Class.forName(currentClazz.getCanonicalName() + "_", false, Thread.currentThread().getContextClassLoader());
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine((joinType.equals(JoinType.INNER) ? "Inner" : "Left outer") + " join between \"" + targetJoin.getParentPath().getJavaType().getCanonicalName() + "\" and \"" + targetJoin.getJavaType().getCanonicalName() + "\" created");
                        }
                    } else {
                        throw new BusinessKeyDaoException("Could not find or create an " + (joinType.equals(JoinType.INNER) ? "inner" : "left outer") + " join for attribute \"" + fieldName + "\" on Class \"" + currentClazz +"\"");
                    }
                }
            }
        }
    }

    private Field getClassField(Class<?> clazz, String fieldName) throws NoSuchFieldException{
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch(NoSuchFieldException e) {
            throw new NoSuchFieldException("Could not retrieve field \"" + fieldName + "\" from Class \"" + clazz.getCanonicalName() + "\"");
        } catch(Exception e) {
            throw new BusinessKeyDaoException("Error retrieving field \"" + fieldName + "\" from Class \"" + clazz.getCanonicalName() + "\"", e);
        }
        return field;
    }

    @SuppressWarnings("rawtypes")
    private void addOrder(List<Order> orders, Direction direction, CriteriaBuilder criteriaBuilder, From root, String fieldName) {
        try {
            if(direction.equals(Direction.ASC)) {
                orders.add(criteriaBuilder.asc(root.get(fieldName)));
            } else {
                orders.add(criteriaBuilder.desc(root.get(fieldName)));
            }
        } catch(Exception e) {
            throw new BusinessKeyDaoException("Error specifying an order by field \"" + fieldName + "\" from class \"" + root.getJavaType().getCanonicalName() + "\"", e);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void createOrderNotDottedNotation(List<Order> orders, SortOrder sortOrder, String fieldName, Direction direction, CriteriaBuilder criteriaBuilder, Root<T> root, Class<?> rootClazz, Class<?> rootMetamodelClazz) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = sortOrder.getClazz();
        if(clazz == null || clazz.getCanonicalName().equals(rootClazz.getCanonicalName())) {
            // No class specified, or the class specified is <T>, use the Root object
            if(direction.equals(Direction.ASC)) {
                orders.add(criteriaBuilder.asc(root.get(fieldName)));
            } else {
                orders.add(criteriaBuilder.desc(root.get(fieldName)));
            }
        } else {
            // A class that is not <T> is specified. Find the corresponding Join object and use it
            boolean found = false;
            Join<T,?> targetJoin = null;
            String attributeName = getFieldNameForType(clazz);
            boolean isNullable = isNullable(attributeName);
            JoinType joinType = isNullable ? JoinType.LEFT : JoinType.INNER;
            Set<Join<T,?>> joins = root.getJoins();
            if(joins != null) {
                for(Join<T,?> join : joins) {
                    Class<?> joinClazz = join.getJavaType();
                    if(joinClazz.getCanonicalName().equals(clazz.getCanonicalName()) &&
                       join.getJoinType().equals(joinType)) {
                        targetJoin = join;
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                Field metaModelField = null;
                try{
                    metaModelField = rootMetamodelClazz.getDeclaredField(attributeName);
                }catch(NoSuchFieldException nse){
                    LOG.fine("Could not get MetaModel field");
                }
                if(metaModelField != null) {
                    Attribute attribute = (Attribute) metaModelField.get(rootMetamodelClazz);
                    if(attribute == null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Could not get MetaModel field value");
                        } 
                    } else {
                        targetJoin = createJoin(attribute, root, joinType);
                        found = (targetJoin != null);
                    }
                }
            }
     
            if(found) {
                if(direction.equals(Direction.ASC)) {
                    orders.add(criteriaBuilder.asc(targetJoin.get(fieldName)));
                } else {
                    orders.add(criteriaBuilder.desc(targetJoin.get(fieldName)));
                }
            } else {
                throw new BusinessKeyDaoException("Error: an ORDER BY based on class " + clazz.getCanonicalName() + " was requested, but no Join on an attribute of this type was found or could be created.");
            }
        }
    }

    // No generics. To use when returning lists other that List<T>. 
    @SuppressWarnings("rawtypes")
    protected CriteriaQuery processOrderByForObjects(CriteriaQuery query, Root root, Sorting sort) {
        return processOrderByForObjects(getTransactionalEntityManager().getCriteriaBuilder(), query, root, sort);
    }

    @SuppressWarnings("rawtypes")
    protected CriteriaQuery processOrderByForObjects(CriteriaBuilder criteriaBuilder, CriteriaQuery query, Root root, Sorting sort) {
        return processOrderBy(criteriaBuilder, query, root, sort);
    }

    protected Query getCriteriaApiQuery(CriteriaQuery<T> query) {
        Query q = null;
        try {
            q = getTransactionalEntityManager().createQuery(query);
            q = processCacheHints(q);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }

        return q;
    }

    protected T find(CriteriaQuery<T> query) {
        Query q = getCriteriaApiQuery(query);
        List<T> result = q.getResultList();
        if (CollectionUtils.isNotEmpty(result)){
            return result.get(0);
        }else{
            return null;
        }
    }

    public List<T> findAll(CriteriaQuery<T> query) {
        return findAll(query, 0, 0);
    }

    public List<T> findAll(CriteriaQuery<T> query, Pagination page) {
        if(page == null) {
            return findAll(query);
        } else {
            return findAll(query, page.getOffet(), page.getLimit());
        }
    }
    public List<T> findAll(CriteriaQuery<T> query, int offset, int limit) {
        Query q = getCriteriaApiQuery(query);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings("rawtypes")
    public List findAllObjects(CriteriaQuery query) {
        return findAll(query, 0, 0);
    }

    @SuppressWarnings("rawtypes")
    public List findAllObjects(CriteriaQuery query, Pagination page) {
        if(page == null) {
            return findAllObjects(query);
        } else {
            return findAllObjects(query, page.getOffet(), page.getLimit());
        }
    }

    @SuppressWarnings("rawtypes")
    public List findAllObjects(CriteriaQuery query, int offset, int limit) {
        return findAll(query, offset, limit);
    }



    protected Long executeLongAggregateFunction(CriteriaQuery<Long> query) {
        try {
            Query q = getTransactionalEntityManager().createQuery(query);
            return (Long) q.getSingleResult();
        } catch (NoResultException e) {
            return Long.valueOf(0);
        }
    }

    protected BigDecimal executeBigDecimalAggregateFunction(CriteriaQuery<BigDecimal> query) {
        try {
            Query q = getTransactionalEntityManager().createQuery(query);
            return (BigDecimal) q.getSingleResult();
        } catch (NoResultException e) {
            return BigDecimal.valueOf(0);
        }
    }

    // ----------------------------------------------------------------------------
    // FetchStrategy support
    // ----------------------------------------------------------------------------
    /**
     * This class is a convenience wrapper to return both a CriteriaQuery and its Root object.
     */
    public class CriteriaQueryContainer {
        private CriteriaQuery<T> query;
        private Root<T> root;

        public CriteriaQueryContainer(CriteriaQuery<T> query, Root<T> root) {
            super();
            this.root = root;
            this.query = query;
        }

        public CriteriaQuery<T> getQuery() {
            return query;
        }
        public Root<T> getRoot() {
            return root;
        }
    }

    /**
     * This method binds ONE field to be eagerly loaded to the FetchParent object defining class containing that field.
     * The join type is determined by looking for a specific entry for this field/parent combination, defaulting
     * to a LEFT join if none is found. The binding itself is done using attributes from the MetaModel class object for the parent class.
     * If the bind is successful (binds of simple types are refused, I really need to look for a way to identify these, for efficiency),
     * this method will invoke processEagerlyLoadedFields() for the type of the field that was just bound (using the FetchParent object
     * obtained when binding), so that its hydration can be directed from the same FetchStrategy object.
     *
     * @param fieldName the field to eagerly fetch
     * @param fetch the FetchParent object to bind the field to eagerly fetch to
     * @param containingClazz the Class object of the class containing the field to eagerly fetch
     * @param containingMetaModelClazz the MetaModel Class object of the class containing the field to eagerly fetch
     * @param strategy the FetchStrategy to use
     * @return the FetchParent object resulting from the bind
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    protected FetchParent<?,?> processEagerlyLoadedField(
            String fieldName,
            FetchParent<?,?> fetch,
            Class<?> containingClazz,
            Class<?> containingMetaModelClazz,
            FetchStrategyImpl strategy) throws IllegalAccessException, ClassNotFoundException {
        FetchParent<?,?> localFetch = fetch;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Processing eagerly loaded field \"" + fieldName + "\" for class " + containingClazz.getCanonicalName());
        }
        JoinType joinType = Fetches.getJoinType(strategy.getJoinType(containingClazz, fieldName));
        if(joinType == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No join type specified - defaulting to LEFT");
            }
            joinType = JoinType.LEFT;
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Join type: " + joinType.toString());
            }
        }
        
        Field metaModelField = null;
        try{
            metaModelField = containingMetaModelClazz.getDeclaredField(fieldName);
        }catch (NoSuchFieldException e){
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Could not get MetaModel field");
            }
            return localFetch;
        }
        Object attribute = metaModelField.get(containingMetaModelClazz);
        if(attribute == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Could not get MetaModel field value");
            }
            return localFetch;
        }

        Class<?> fieldClazz = null;
        if (attribute instanceof SingularAttribute) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Field is a SingularAttribute");
            }
            SingularAttribute sa = (SingularAttribute)attribute;
            fieldClazz = sa.getType().getJavaType();
            try {
                localFetch = localFetch.fetch(sa, joinType);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Bind accepted");
                }
            } catch(Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Bind refused. Returning");
                }
                return localFetch;
            }

        } else if (attribute instanceof PluralAttribute) {
            PluralAttribute pa = (PluralAttribute)attribute;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Field is a PluralAttribute, with collection type " + pa.getCollectionType().toString());
            }
            fieldClazz = pa.getElementType().getJavaType();
            try {
                localFetch = localFetch.fetch(pa, joinType);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Bind accepted");
                }
            } catch(Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Bind refused. Returning");
                }
                return localFetch;
            }
        }

        if(strategy.getRecursiveProcessing(containingClazz) && strategy.getRecursiveProcessing(containingClazz, fieldName)) {
            // Now that the field is binded, recursively call processEagerlyLoadedFields() to process the fields
            // that must be eagerly loaded on the field's type. In this case, you MUST use the FetchParent resulting
            // from the bind, as we are now one level deeper.
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Field type is " + fieldClazz.getCanonicalName() + ". Calling processor method");
            }
            localFetch = processEagerlyLoadedFields(localFetch, fieldClazz, strategy);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Done processing field " + fieldName + " for " + containingClazz.getCanonicalName());
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Field " + fieldName + " is not to be processed recursively");
            }
        }
        return localFetch;
    }

    /**
     * This method will bind all fields to be eagerly loaded for when constructing instances of a certain type during
     * the execution of a query.
     * @param fetch the FetchParent object defining the join between instances of the object whose hydration is being defined.
     * This can be the Root<T> object created as the top level node, or any of the FetchParent objects that result from any
     * of the bindings executed to drive eager loadings
     * @param clazz the Class object of the object whose hydration is being defined
     * @param strategy
     * @return the FetchParent object resulting from all the bindings
     * @throws Exception
     */
    protected FetchParent<?,?> processEagerlyLoadedFields(FetchParent<?,?> fetch, Class<?> clazz, FetchStrategyImpl strategy) throws IllegalAccessException, ClassNotFoundException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Processing eagerly loaded fields for field of type " + clazz.getCanonicalName());
        }
        List<String> eagerlyLoadedFields = strategy.getEagerlyLoadedFieldsForClass(clazz);
         if (eagerlyLoadedFields != null && !eagerlyLoadedFields.isEmpty()) {
             Class<?> metamodelClazz = Class.forName(clazz.getCanonicalName() + "_", false, Thread.currentThread().getContextClassLoader());
             for (String fieldName : eagerlyLoadedFields) {
                // In this section, all the fields to be set as eagerly loaded are fields belonging to the
                // same entity. This means they all must be binded to the same FetchParent. Therefore, DO NOT
                // reinitialize it with the result of each bind.
                if(fieldName.equals(FetchStrategyImpl.ALL_FIELDS_MARKER)) {
                    com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType joinType = strategy.getJoinType(clazz, fieldName);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Adding all fields");
                    }
                    Field[] fields = metamodelClazz.getFields();
                    for(Field field : fields) {
                        String name = field.getName();
                        if(joinType != null) {
                            // A global join type was specified. Add it to the strategy prior to processing the field
                            strategy.setJoinTypeForField(clazz, name, joinType);
                        }
                        processEagerlyLoadedField(name, fetch, clazz, metamodelClazz, strategy);
                    }
                    // Meeting a * terminates the loop, of course.
                    break;
                } else {
                    processEagerlyLoadedField(fieldName, fetch, clazz, metamodelClazz, strategy);
                }
            }
        } else {
             if (LOG.isLoggable(Level.FINE)) {
                 LOG.fine("No eagerly loaded fields found for field of type " + clazz.getCanonicalName());
             }
        }

        return fetch;
    }

    /**
     * This method creates a CriteriaQuery<T> and a Root<T> where T is the generics parameter of the actual DaoJpaImpl.
     * The Root<T> object returned will carry the object graph defining all eager fetching across the entire structures returned, by
     * processing the FetchStrategy passed, if any. This method should be the first thing called from any DAO methods returning List<T> or T.
     *
     * @param fetchStrategy the Fetch Strategy to apply
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected CriteriaQueryContainer createCriteriaQuery(FetchStrategy... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creating Criteria Query for type " + this.getType().getCanonicalName());
        }
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.getType());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Getting Root object for type " + this.getType().getCanonicalName());
        }
        Root<T> root = criteriaQuery.from(this.getType());

        if(fetchStrategy != null && fetchStrategy.length > 0) {
            FetchStrategy fs = fetchStrategy[0];
            if(fs != null) {
                if(fs instanceof FetchStrategyJpaImpl) {
                    try {
                        // Single level MetaModel-based strategy. Old code kicks in.
                        Fetches fetches = new Fetches(this.getType(), (FetchStrategyJpaImpl)fs);
                        root = (Root<T>)Fetches.bindFetchAttributes(root, fetches);
                    } catch(Exception e) {
                        throw new IllegalStateException("Failed to bind fetch attributes for " + this.getType().getName(), e);
                    }
                } else {
                    FetchStrategyImpl strategy = fetchStrategyFactory.getFetchStrategy(fs);
                    if(strategy != null) {
                        try {
                            processEagerlyLoadedFields(root, this.getType(), strategy);
                        } catch(Exception e) {
                            throw new IllegalStateException("Failed to process eagerly loaded fields for " + this.getType().getName(), e);
                        }
                    }
                }
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Returning");
        }
        return new CriteriaQueryContainer(criteriaQuery, root);
    }
    /**
     * This method creates a CriteriaQuery<T> and a Root<T> where T is the generics parameter of the actual DaoJpaImpl.
     * Use this method when no FetchStrategy is to be processed.
     *
     * @return
     */
    protected CriteriaQueryContainer createCriteriaQuery() {
        return createCriteriaQuery((FetchStrategy[])null);
    }
}
