package com.adfonic.tools.beans.transaction;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.collections.MapUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.PublisherAccountingDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.CurrencyUtils;

@Component
@Scope("view")
public class TransactionFundsAcrossMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionFundsAcrossMBean.class.getName());
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("25.00");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CompanyService companyService;

    private AdvertiserAccountingDto advertiser;
    private PublisherAccountingDto publisher;
    private BigDecimal amount;
    private PublisherDto publisherDto;

    public TransactionFundsAcrossMBean() {
    }

    @Override
    @PostConstruct
    protected void init() throws Exception {
        LOGGER.debug("init-->");
        advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        publisher = transactionService.getPublisherAccountingDtoForUser(getUser());
        LOGGER.debug("init<--");
    }

    public void doSave() {
        LOGGER.debug("doSave-->");
        Map<String, AccountDetailDto> results = transactionService.transferFundsAcross(advertiser.getAccount(), publisher.getAccount(),
                amount);
        advertiser = transactionService.getAdvertiserAccountingDtoForUser(getUser());
        publisher = transactionService.getPublisherAccountingDtoForUser(getUser());

        if (MapUtils.isEmpty(results)) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "fundsAcrossAmount", null, "error.transactions.fundsacross.dialog.transfer");
            return;
        } else {
            RequestContext.getCurrentInstance().execute("fundsAcrossDialog.hide()");
        }
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
                    "error.transactions.fundsacross.dialog.amount.minimum", CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_AMOUNT));
            throw new ValidatorException(fm);
        }

        if (getUser().getPublisherDto() == null && "agency".equals(getUser().getUserType()) && publisher != null) {
            publisherDto = companyService.getPublisherById(publisher.getId());
        } else if (getUser().getPublisherDto() != null) {
            publisherDto = getUser().getPublisherDto();
        }
        BigDecimal publisherBalance = accountService.getAccountBalance(publisherDto);
        if (amount.compareTo(publisherBalance) > 0) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null,
                    "error.transactions.fundsacross.dialog.amount.maximum", CurrencyUtils.CURRENCY_FORMAT_USD.format(publisherBalance));
            throw new ValidatorException(fm);
        }
    }

    public String getMinimumAmountMessage() {
        return FacesUtils.getBundleMessage("page.transactions.fundsacross.dialog.minimum", CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_AMOUNT));
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
        if (getUser().getAdvertiserDto() != null) {
            return accountService.getAccountBalance(getUser().getAdvertiserDto());
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getPublisherBalance() {
        if (getUser().getPublisherDto() == null && "agency".equals(getUser().getUserType()) && publisher != null && publisherDto == null) {
            publisherDto = companyService.getPublisherById(publisher.getId());
        } else if (getUser().getPublisherDto() != null) {
            publisherDto = getUser().getPublisherDto();
        }
        if (publisherDto != null) {
            return accountService.getAccountBalance(publisherDto);
        } else {
            return new BigDecimal(0);
        }
    }
}
