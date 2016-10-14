package com.adfonic.tools.beans.transaction;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
        @URLMapping(id = "transactions-advertiser", pattern = "/transactions/advertiser", viewId = "/WEB-INF/jsf/transactions/advertiserTransactions.jsf"),
        @URLMapping(id = "transactions-publisher", pattern = "/transactions/publisher", viewId = "/WEB-INF/jsf/transactions/publisherTransactions.jsf") })
public class TransactionMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionMBean.class.getName());

    @Autowired
    private TransactionHistoryMBean transactionHistoryMBean;

    @Autowired
    private TransactionDailyBudgetMBean transactionDailyBudgetMBean;

    @Autowired
    private TransactionAdNotificationMBean transactionAdNotificationMBean;

    @Autowired
    private TransactionPostPayMBean transactionPostPayMBean;

    @Autowired
    private TransactionFundsOutMBean transactionFundsOutMBean;

    @Autowired
    private TransactionFundsAcrossMBean transactionFundsAcrossMBean;

    @Autowired
    private TransactionFundsInMBean transactionFundsInMBean;

    @Override
    @PostConstruct
    public void init() throws Exception {
        LOGGER.debug("init-->");
        getNavigationSessionBean().navigate(Constants.TRANSACTIONS);
        LOGGER.debug("init<--");
    }

    public TransactionHistoryMBean getTransactionHistoryMBean() {
        return transactionHistoryMBean;
    }

    public void setTransactionHistoryMBean(TransactionHistoryMBean transactionHistoryMBean) {
        this.transactionHistoryMBean = transactionHistoryMBean;
    }

    public TransactionDailyBudgetMBean getTransactionDailyBudgetMBean() {
        return transactionDailyBudgetMBean;
    }

    public void setTransactionDailyBudgetMBean(TransactionDailyBudgetMBean transactionDailyBudgetMBean) {
        this.transactionDailyBudgetMBean = transactionDailyBudgetMBean;
    }

    public TransactionAdNotificationMBean getTransactionAdNotificationMBean() {
        return transactionAdNotificationMBean;
    }

    public void setTransactionAdNotificationMBean(TransactionAdNotificationMBean transactionAdNotificationMBean) {
        this.transactionAdNotificationMBean = transactionAdNotificationMBean;
    }

    public TransactionPostPayMBean getTransactionPostPayMBean() {
        return transactionPostPayMBean;
    }

    public void setTransactionPostPayMBean(TransactionPostPayMBean transactionPostPayMBean) {
        this.transactionPostPayMBean = transactionPostPayMBean;
    }

    public TransactionFundsOutMBean getTransactionFundsOutMBean() {
        return transactionFundsOutMBean;
    }

    public void setTransactionFundsOutMBean(TransactionFundsOutMBean transactionFundsOutMBean) {
        this.transactionFundsOutMBean = transactionFundsOutMBean;
    }

    public TransactionFundsAcrossMBean getTransactionFundsAcrossMBean() {
        return transactionFundsAcrossMBean;
    }

    public void setTransactionFundsAcrossMBean(TransactionFundsAcrossMBean transactionFundsAcrossMBean) {
        this.transactionFundsAcrossMBean = transactionFundsAcrossMBean;
    }

    public TransactionFundsInMBean getTransactionFundsInMBean() {
        return transactionFundsInMBean;
    }

    public void setTransactionFundsInMBean(TransactionFundsInMBean transactionFundsInMBean) {
        this.transactionFundsInMBean = transactionFundsInMBean;
    }

}
