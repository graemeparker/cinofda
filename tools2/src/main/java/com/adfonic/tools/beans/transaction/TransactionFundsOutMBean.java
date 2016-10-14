package com.adfonic.tools.beans.transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.PaymentOptions;
import com.adfonic.domain.PaymentOptions.PaymentType;
import com.adfonic.dto.address.PostalAddressDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.dto.transactions.PaymentOptionsDto;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;
import com.adfonic.email.EmailService;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class TransactionFundsOutMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionFundsOutMBean.class.getName());
    private static final char SPACE = ' ';
    private static final String PAYMENT_TYPE_MSG_KEY_PREFIX = "page.transactions.fundsout.dialog.method.";
    private static final String PAYMENT_TYPE_NONE_SET_MSG_KEY = "page.transactions.fundsout.method.none";

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private EmailAddressManager emailAddressManager;

    @Autowired
    private EmailService emailService;

    private CompanyAccountingDto company;

    private PaymentOptionsDto paymentOptions;
    private String payPalLogin;
    private String chequePayee;
    private PaymentType paymentType;
    private PostalAddressDto address;
    private List<CountryDto> countries;

    public TransactionFundsOutMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        LOGGER.debug("init-->");
        AdvertiserAccountingDto advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        company = advertiser.getCompany();
        paymentOptions = company.getPaymentOptions();

        if (paymentOptions == null) {
            paymentOptions = new PaymentOptionsDto();
            paymentType = PaymentType.PAYPAL;
            address = new PostalAddressDto();
            chequePayee = getUser().getFirstName() + " " + getUser().getLastName();
        } else {
            paymentType = paymentOptions.getPaymentType();
            switch (paymentType) {
            case CHEQUE:
            case WIRE_TRANSFER:
                chequePayee = paymentOptions.getPaymentAccount();
                address = paymentOptions.getPostalAddress();
                break;
            default: // case PAYPAL:
                payPalLogin = paymentOptions.getPaymentAccount();
                chequePayee = getUser().getFirstName() + SPACE + getUser().getLastName();
                address = new PostalAddressDto();
                break;
            }
        }
        LOGGER.debug("init<--");
    }

    public void doSave() throws Exception {
        LOGGER.debug("doSave-->");

        switch (paymentType) {
        case PAYPAL:
            paymentOptions.setPaymentType(PaymentOptions.PaymentType.PAYPAL);
            paymentOptions.setPaymentAccount(payPalLogin);
            paymentOptions.setPostalAddress(null);
            break;
        case WIRE_TRANSFER:
            paymentOptions.setPaymentType(PaymentOptions.PaymentType.WIRE_TRANSFER);
            paymentOptions.setPaymentAccount(chequePayee);
            paymentOptions.setPostalAddress(address);
            break;
        default: // case CHEQUE
            paymentOptions.setPaymentType(PaymentOptions.PaymentType.CHEQUE);
            paymentOptions.setPaymentAccount(chequePayee);
            paymentOptions.setPostalAddress(address);
            break;
        }

        company = transactionService.savePaymentOptions(company, paymentOptions);

        // Email the user a confirmation
        String to = getUser().getFormattedEmail();
        String subject = "Confirmation of setting up your payments";
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("user", getUser());
        values.put("urlRoot", getURLRoot());
        values.put("companyName", getToolsApplicationBean().getCompanyName());

        String body;

        switch (paymentType) {
        case PAYPAL:
            values.put("payPalLogin", payPalLogin);
            body = templateToString("/templates/get_paid_paypal.html", values);
            break;
        case WIRE_TRANSFER:
            values.put("chequePayee", chequePayee);
            values.put("address", address);
            body = templateToString("/templates/get_paid_wire_transfer.html", values);
            break;
        default: // CHEQUE:
            values.put("chequePayee", chequePayee);
            values.put("address", address);
            body = templateToString("/templates/get_paid_cheque.html", values);
            break;
        }

        emailService.sendEmail(emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY), to, subject, body, "text/html");

        RequestContext.getCurrentInstance().execute("fundsOutDialog.hide()");
        LOGGER.debug("doSave<--");
    }

    public void doCancel(ActionEvent event) throws Exception {
        LOGGER.debug("doCancel-->");
        init();
        LOGGER.debug("doCancel<--");
    }

    public String getPaymentTypeLabel() {
        if (paymentType != null) {
            return FacesUtils.getBundleMessage(PAYMENT_TYPE_MSG_KEY_PREFIX + paymentType.toString());
        } else {
            return FacesUtils.getBundleMessage(PAYMENT_TYPE_NONE_SET_MSG_KEY);
        }
    }

    public List<CountryDto> getCountries() {
        if (countries == null) {
            countries = (List<CountryDto>) locationService.getAllCountries();
        }
        return countries;
    }

    public CompanyAccountingDto getCompany() {
        return company;
    }

    public void setCompany(CompanyAccountingDto company) {
        this.company = company;
    }

    public String getChequePayee() {
        return chequePayee;
    }

    public void setChequePayee(String chequePayee) {
        this.chequePayee = chequePayee;
    }

    public String getPayPalLogin() {
        return payPalLogin;
    }

    public void setPayPalLogin(String payPalLogin) {
        this.payPalLogin = payPalLogin;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PostalAddressDto getAddress() {
        return address;
    }

    public void setAddress(PostalAddressDto address) {
        this.address = address;
    }

}
