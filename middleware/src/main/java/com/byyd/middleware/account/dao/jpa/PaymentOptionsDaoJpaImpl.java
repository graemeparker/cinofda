package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PaymentOptions;
import com.byyd.middleware.account.dao.PaymentOptionsDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PaymentOptionsDaoJpaImpl extends BusinessKeyDaoJpaImpl<PaymentOptions> implements PaymentOptionsDao {

}
