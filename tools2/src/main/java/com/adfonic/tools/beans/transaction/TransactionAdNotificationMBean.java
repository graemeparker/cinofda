package com.adfonic.tools.beans.transaction;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.ValidationUtils;

@Component
@Scope("view")
public class TransactionAdNotificationMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionAdNotificationMBean.class.getName());

    @Autowired
    private TransactionService transactionService;

    private boolean enabled;
    private BigDecimal notifyLimit;
    private AdvertiserAccountingDto advertiser;
    private String notifyAdditionalEmails;

    public TransactionAdNotificationMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        LOGGER.debug("init-->");
        advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        this.notifyLimit = advertiser.getNotifyLimit();
        this.enabled = (notifyLimit != null);
        this.notifyAdditionalEmails = advertiser.getNotifyAdditionalEmails();
        LOGGER.debug("init<--");
    }

    public void doSave() {
        if (enabled) {
            if (notifyLimit == null || notifyLimit.compareTo(BigDecimal.ZERO) <= 0) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "notifyLimit", null, "error.myAdsAdBalanceNotification.notifyLimit");
                return;
            }
        }

        if (enabled) {
            advertiser.setNotifyLimit(notifyLimit);
            advertiser.setNotifyAdditionalEmails(notifyAdditionalEmails);
        } else {
            advertiser.setNotifyLimit(null);
            advertiser.setNotifyAdditionalEmails(null);
        }
        advertiser = transactionService.updateAdvertiser(advertiser);
        RequestContext.getCurrentInstance().execute("balanceNotificationDialog.hide()");
    }

    public void validateEmails(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null) {
            String[] emails = ((String) value).split(",");
            for (String email : emails) {
                validateEmail(email.trim());
            }
        }
    }

    public void validateEmail(String email) throws ValidatorException {
        if (ValidationUtils.isValidEmailAddress(email)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "error.myAdsAdBalanceNotification.email");
            throw new ValidatorException(fm);
        }
    }

    public void doCancel(ActionEvent event) throws Exception {
        init();
    }

    public List<String> getRecipientEmails() {
        List<String> emails = new ArrayList<String>();
        emails.add(getUser().getEmail());
        if (StringUtils.isNotEmpty(notifyAdditionalEmails)) {
            emails.addAll(Arrays.asList(notifyAdditionalEmails.split(",")));
        }
        return emails;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BigDecimal getNotifyLimit() {
        return notifyLimit;
    }

    public void setNotifyLimit(BigDecimal notifyLimit) {
        this.notifyLimit = notifyLimit;
    }

    public String getNotifyAdditionalEmails() {
        return notifyAdditionalEmails;
    }

    public void setNotifyAdditionalEmails(String notifyAdditionalEmails) {
        this.notifyAdditionalEmails = notifyAdditionalEmails;
    }
}
