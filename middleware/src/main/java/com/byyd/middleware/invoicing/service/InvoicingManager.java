package com.byyd.middleware.invoicing.service;

import java.math.BigDecimal;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.TransactionNotification;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.BaseManager;

public interface InvoicingManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // TransactionNotification
    //------------------------------------------------------------------------------------------
    TransactionNotification newTransactionNotification(Advertiser advertiser, User user, BigDecimal amount, String reference);
    TransactionNotification getTransactionNotificationById(String id, FetchStrategy... fetchStrategy);
    TransactionNotification getTransactionNotificationById(Long id, FetchStrategy... fetchStrategy);
    TransactionNotification update(TransactionNotification transactionNotification);
    void delete(TransactionNotification transactionNotification);
}
