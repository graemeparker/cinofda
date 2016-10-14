package com.byyd.middleware.iface.dao.jpa;

import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.iface.dao.ReadOnlyDataSourceDao;
import com.byyd.middleware.iface.exception.BusinessKeyDaoException;

/**
 * This is a base class to extend to create DAO implementations that will target the reporting data source exclusively,
 * instead of the main datasource. The proper entity manager is wired from the context, and a getter overrides the base getter,
 * so all other aspects are unchanged. 
 * 
 * To change which data source a DAO will use, all you need to do is extend the proper interface and base class, then
 * write methods as you normally would.
 * 
 * @author pierre
 *
 * @param <T>
 */
public class ReadOnlyDataSourceDaoJpaImpl<T extends BusinessKey> extends BusinessKeyDaoJpaImpl<T> implements ReadOnlyDataSourceDao<T> {

    private static final transient Logger LOG = Logger.getLogger(ReadOnlyDataSourceDaoJpaImpl.class.getName());

    @Autowired(required=false)
    @Qualifier("readOnlyEntityManagerFactory")
    private EntityManagerFactory entityManagerFactory;
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory == null) {
            String msg = "It seems no EntityManagerFactory with name readOnlyEntityManagerFactory was defined in the context used to boot this application";
            LOG.severe(msg);
            throw new BusinessKeyDaoException(msg);
        }
        return entityManagerFactory;
    }


}
