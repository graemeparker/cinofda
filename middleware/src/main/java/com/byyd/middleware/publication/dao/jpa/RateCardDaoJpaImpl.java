package com.byyd.middleware.publication.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.RateCard;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.RateCardDao;

@Repository
public class RateCardDaoJpaImpl extends BusinessKeyDaoJpaImpl<RateCard> implements RateCardDao {

}
