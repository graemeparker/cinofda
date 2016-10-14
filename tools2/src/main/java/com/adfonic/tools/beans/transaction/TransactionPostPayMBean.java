package com.adfonic.tools.beans.transaction;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class TransactionPostPayMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private TransactionService transactionService;

    private CompanyAccountingDto company;
    private Date startOfNextMonth;
    private Date paymentDueDate;

    public TransactionPostPayMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        AdvertiserAccountingDto advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        company = advertiser.getCompany();
    }

    public Date getStartOfNextMonth() {
        if (this.startOfNextMonth == null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(company.getDefaultTimeZone()));
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            this.startOfNextMonth = cal.getTime();
        }
        return this.startOfNextMonth;
    }

    public Date getPaymentDueDate() {
        if (this.paymentDueDate == null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(company.getDefaultTimeZone()));
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_MONTH, company.getPostPayTermDays());
            this.paymentDueDate = cal.getTime();
        }
        return this.paymentDueDate;
    }

    public CompanyAccountingDto getCompany() {
        return company;
    }

    public void setCompany(CompanyAccountingDto company) {
        this.company = company;
    }
}
