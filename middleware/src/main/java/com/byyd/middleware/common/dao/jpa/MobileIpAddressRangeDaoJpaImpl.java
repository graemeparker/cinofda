package com.byyd.middleware.common.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.MobileIpAddressRange;
import com.byyd.middleware.common.dao.MobileIpAddressRangeDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class MobileIpAddressRangeDaoJpaImpl extends BusinessKeyDaoJpaImpl<MobileIpAddressRange> implements MobileIpAddressRangeDao {

}
