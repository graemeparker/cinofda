package com.byyd.middleware.invoicing.service.jpa;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.TransactionNotification;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.invoicing.dao.TransactionNotificationDao;
import com.byyd.middleware.invoicing.service.InvoicingManager;

@Service("invoicingManager")
public class InvoicingManagerJpaImpl extends BaseJpaManagerImpl implements InvoicingManager {

    @Autowired(required=false)
    private TransactionNotificationDao transactionNotificationDao;
    
    // ------------------------------------------------------------------------------------------
    // TransactionNofication
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public TransactionNotification newTransactionNotification(Advertiser advertiser, User user, BigDecimal amount, String reference) {
        TransactionNotification transactionNotification = new TransactionNotification(advertiser, user, amount, reference);
        return transactionNotificationDao.create(transactionNotification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionNotification getTransactionNotificationById(String id, FetchStrategy... fetchStrategy) {
        return getTransactionNotificationById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionNotification getTransactionNotificationById(Long id, FetchStrategy... fetchStrategy) {
        return transactionNotificationDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public TransactionNotification update(TransactionNotification transactionNotification) {
        return transactionNotificationDao.update(transactionNotification);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(TransactionNotification transactionNotification) {
        transactionNotificationDao.delete(transactionNotification);
    }
}
