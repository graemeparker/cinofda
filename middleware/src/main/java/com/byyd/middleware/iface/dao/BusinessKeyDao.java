package com.byyd.middleware.iface.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.adfonic.domain.BusinessKey;

/**
 * Base class for all DaoImpls for objects of the Domain that extend BusinessKey
 * @author pierre
 *
 * @param <T>
 */
public interface BusinessKeyDao<T extends BusinessKey> {

    /**
    * @param object
    */
    T create(T object);

    /**
    * @param object
    * @return
    */
    T update(T object);

    /**
    * @param object
    */
    void delete(T object);

    /**
    * Convenience method to retrieve an object by its internal ID while applying a FetchStrategy to its fields.
    * @param id the ID to query with
    * @param fetchStrategy optional FetchStrategyImpl or FetchStrategyName
    * @return the object found or a NoResultException if none are found
    */
    T getById(Long id, FetchStrategy... fetchStrategy);

    /**
     * Convenience method to retrieve an list of objects by their internal IDs while applying a FetchStrategy to their fields.
     * @param ids the list of IDs to query with
     * @param fetchStrategy optional FetchStrategyImpl or FetchStrategyName
     * @return the object found or a NoResultException if none are found
     */
    List<T> getObjectsByIds(Collection<Long> ids, FetchStrategy... fetchStrategy);
    List<T> getObjectsByIds(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    List<T> getObjectsByIds(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);

    /**
    * Convenience method to retrieve an object by its external ID while applying a FetchStrategy to its fields.
    * @param externalId the ID to query with
    * @param fetchStrategy optional FetchStrategyImpl or FetchStrategyName
    * @return the object found or a NoResultException if none are found
    */
    T getByExternalId(String externalId, FetchStrategy... fetchStrategy);
    /**
    * Convenience method to retrieve a List of object by a given List of external ID while applying a FetchStrategy to its fields.
    * @param externalsId the IDs to query with
    * @param fetchStrategy optional FetchStrategyImpl or FetchStrategyName
    * @return the List of objects found or a NoResultException if none are found
    */    
    List<T> getByExternalIds(List<String> externalsId, String propertyDate,Date startDate,Date endDate,FetchStrategy... fetchStrategy);

    /**
     * Convenience method to retrieve an object by its name while applying a FetchStrategy to its fields.
     * The test is case-sensitive.
     * @param name
     * @param fetchStrategyName
     * @return
     */
    T getByName(String name, FetchStrategy... fetchStrategy);
    /**
     * Convenience method to retrieve an object by its name while applying a FetchStrategy to its fields.
     * The test can be case-sensitive or not.
     * @param name
     * @param caseSensitive whether or not the test is case sesnsitive
     * @param fetchStrategyName
     * @return
     */
    T getByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    /**
     * Convenience method to retrieve an object by its system name while applying a FetchStrategy to its fields.
     * The test will be case-sensitive.
     * @param name
      * @param fetchStrategyName
     * @return
     */
     T getBySystemName(String name, FetchStrategy... fetchStrategy);
     /**
      * Convenience method to retrieve an object by its system name while applying a FetchStrategy to its fields.
      * The test can be case-sensitive or not.
      * @param name
      * @param caseSensitive whether or not the test is case sesnsitive
      * @param fetchStrategyName
      * @return
      */
      public T getBySystemName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);


    //------------------------------------------------------------------------------------------
    /**
     * Convenience method to count all instances of T without any restriction
     */
    Long countAll();
    /**
    * Convenience method to retrieve all instances of T without any restriction
    * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
    */
    List<T> getAll(FetchStrategy... fetchStrategy);
    /**
    * Convenience method to retrieve all instances of T without any restriction, and sorting them
     * @param sort the Sorting spec
     * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
     */
    List<T> getAll(Sorting sort, FetchStrategy... fetchStrategy);
    /**
    * Convenience method to retrieve a page of instances of T without any restriction, with possible sorting
     * @param page the page spec to return, possibly with a Sorting spec
     * @param fetchStrategy the FetchStrategy to apply to the instances of T retrieved
    * @return a list of T
     */
    List<T> getAll(Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------

    Long countAllForName(String name, LikeSpec like, boolean caseSensitive);
    List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<T> getAllForName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------



}
