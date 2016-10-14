package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Publisher;
import com.byyd.middleware.account.dao.PublisherDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PublisherDaoJpaImpl extends BusinessKeyDaoJpaImpl<Publisher> implements PublisherDao {

}
