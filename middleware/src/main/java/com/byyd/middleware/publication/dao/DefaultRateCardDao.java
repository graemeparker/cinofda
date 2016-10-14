package com.byyd.middleware.publication.dao;

import com.adfonic.domain.BidType;
import com.adfonic.domain.DefaultRateCard;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface DefaultRateCardDao extends BusinessKeyDao<DefaultRateCard> {

    DefaultRateCard getByBidType(BidType bidType, FetchStrategy... fetchStrategy);
}
