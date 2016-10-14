package com.byyd.middleware.iface.dao;

import com.adfonic.domain.BusinessKey;

/**
 * This is a base DAO interface to extend when creating DAOs that will require the use of
 * the read-only database exclusively. Its implementing class will encapsulate the autowiring of
 * the proper EntityManagerFactory and provide an overriding getter for it. All other
 * aspects remain unchanged.
 *  
 * To change which data source a DAO will use, all you need to do is extend the proper interface and base class, then
 * write methods as you normally would.
 * 
 * @author pierre
 *
 * @param <T>
 */
public interface ReadOnlyDataSourceDao<T extends BusinessKey> extends BusinessKeyDao<T> {

}
