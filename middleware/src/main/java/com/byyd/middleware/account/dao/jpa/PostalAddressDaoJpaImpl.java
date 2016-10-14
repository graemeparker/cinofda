package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PostalAddress;
import com.byyd.middleware.account.dao.PostalAddressDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PostalAddressDaoJpaImpl extends BusinessKeyDaoJpaImpl<PostalAddress> implements PostalAddressDao {

}
