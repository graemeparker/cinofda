package com.byyd.middleware.publication.dao.jpa;

import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.byyd.middleware.publication.dao.PublicationReadOnlyDao;

/**
 * Please see comments in BasePublicationDao
 * @author pierre
 *
 */
@Repository
public class PublicationReadOnlyDaoJpaImpl extends BasePublicationDaoJpaImpl implements PublicationReadOnlyDao {

    private static final transient Logger LOG = Logger.getLogger(PublicationReadOnlyDaoJpaImpl.class.getName());

    @Autowired(required=false)
    @Qualifier("readOnlyEntityManagerFactory")
    private EntityManagerFactory entityManagerFactory;
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory == null) {
            LOG.severe("It seems no EntityManagerFactory with name readOnlyEntityManagerFactory was defined in the context used to boot this application");
        }
        return entityManagerFactory;
    }

}
