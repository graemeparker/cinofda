package com.byyd.middleware.invoicing.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.TransactionNotification;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.invoicing.dao.TransactionNotificationDao;

@Repository
public class TransactionNotificationDaoJpaImpl extends BusinessKeyDaoJpaImpl<TransactionNotification> implements TransactionNotificationDao {

}
