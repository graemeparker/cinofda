package com.adfonic.tools.beans.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.transactions.TransactionNotificationDto;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;
import com.adfonic.email.EmailService;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.CurrencyUtils;

@Component
@Scope("view")
public class TransactionFundsInMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionFundsInMBean.class.getName());
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000.00");
    @Value("${mail.address.finance}")
    private String CONFIRMATION_TO;
    private static final String CONFIRMATION_SUBJECT = "New Wire Transfer";
    private static final String CONFIRMATION_TEMPLATE = "/templates/transaction_notification.html";

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailAddressManager emailAddressManager;

    @Autowired
    private EmailService emailService;

    private BigDecimal amount;
    private String reference;

    public TransactionFundsInMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        LOGGER.debug("init-->");
        LOGGER.debug("init<--");
    }

    public void doSave() throws Exception {
        LOGGER.debug("doSave-->");

        TransactionNotificationDto transactionNotification = transactionService.newTransactionNotification(getUser().getAdvertiserDto(),
                getUser(), amount, reference);

        if (transactionNotification != null) {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("user", getUser());
            values.put("advertiser", getUser().getAdvertiserDto());
            values.put("transactionNotification", transactionNotification);
            values.put("companyName", getToolsApplicationBean().getCompanyName());

            String body = templateToString(CONFIRMATION_TEMPLATE, values);

            emailService.sendEmail(emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY), CONFIRMATION_TO, CONFIRMATION_SUBJECT,
                    body, "text/html");

        }

        RequestContext.getCurrentInstance().execute("fundsInDialog.hide()");
        LOGGER.debug("doSave<--");
    }

    public void doCancel(ActionEvent event) throws Exception {
        LOGGER.debug("doCancel<--");
        init();
        LOGGER.debug("doCancel-->");
    }

    public void validateAmount(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        BigDecimal amount = (BigDecimal) value;
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null,
                    "error.transactions.fundsin.dialog.amount.minimum", CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_AMOUNT));
            throw new ValidatorException(fm);
        }
    }

    public String getMinimumAmountMessage() {
        return FacesUtils.getBundleMessage("page.transactions.fundsin.dialog.minimum", CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_AMOUNT));
    }

    public BigDecimal getMinimumAmount() {
        return MIN_AMOUNT;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAdvertiserBalance() {
        return accountService.getAccountBalance(getUser().getAdvertiserDto());
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

}
