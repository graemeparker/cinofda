package com.byyd.middleware.creative.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CreativeAttribute;
import com.byyd.middleware.creative.dao.CreativeAttributeDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CreativeAttributeDaoJpaImpl extends BusinessKeyDaoJpaImpl<CreativeAttribute> implements CreativeAttributeDao {

}
