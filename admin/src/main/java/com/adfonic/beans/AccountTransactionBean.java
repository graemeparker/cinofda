package com.adfonic.beans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.TransactionType;
import com.adfonic.domain.User;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.byyd.middleware.utils.TransactionalRunner;

@ManagedBean
@ViewScoped
public class AccountTransactionBean extends BaseBean {

    private User user;
    private Advertiser advertiser;
    private Publisher publisher;
    private boolean agencyAccount;
    private AccountType accountType;
    private List<SelectItem> companyAdvertisers;
    private Account publisherAccount;
    private AccountDetailDto accountDetail = new AccountDetailDto();

    @ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;

    @PostConstruct
    private void init() {
        if (adminAccountBean == null || adminAccountBean.getUser() == null) {
            // this shouldn't happen as the view triggers a check
            throw new AdminGeneralException("no user loaded");
        }
        this.user = adminAccountBean.getUser();

        TransactionalRunner runner = getTransactionalRunner();
        runner.runTransactional(
                new Runnable() {
                    public void run() {
                        load();
                    }
                }
        );
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public boolean isAgencyAccount() {
        return agencyAccount;
    }

    public void setAgencyAccount(boolean agencyAccount) {
        this.agencyAccount = agencyAccount;
    }

    public String getAccountType() {
        if (accountType == null) {
            if (agencyAccount) {
                accountType = AccountType.ADVERTISER;
            }
            else {
                accountType = AccountType.PUBLISHER;
            }
        }
        return accountType.toString();
    }

    public void setAccountType(String accountType) {
        this.accountType = AccountType.valueOf(accountType);
    }

    public List<SelectItem> getCompanyAdvertisers() {
        return companyAdvertisers;
    }

    public void setCompanyAdvertisers(List<SelectItem> companyAdvertisers) {
        this.companyAdvertisers = companyAdvertisers;
    }

	public AdminAccountBean getAdminAccountBean() {
        return adminAccountBean;
    }

    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }

    public BigDecimal getBalance() {
        if (advertiser == null){
            return BigDecimal.ZERO;
        }
        else {
            // get the latest
            TransactionalRunner runner = getTransactionalRunner();
            runner.runTransactional(
                    new Runnable() {
                        public void run() {
                            loadAdvertiserAccount();
                        }
                    }
            );
            return advertiser.getAccount().getBalance();
        }
    }

    public Account getPublisherAccount() {
        return publisherAccount;
    }

    public void setPublisherAccount(Account publisherAccount) {
        this.publisherAccount = publisherAccount;
    }

    public AccountDetailDto getAccountDetail() {
		return accountDetail;
	}

	public void setAccountDetail(AccountDetailDto accountDetail) {
		this.accountDetail = accountDetail;
	}

	public void doCreateTransaction() {
        Account account = null;

        if (accountType == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("accountTypeForm:accountType", new FacesMessage("Account type must be selected"));
            return;
        }
        else {
            switch (accountType) {
                case ADVERTISER:
                    if (advertiser == null) {
                        if (agencyAccount) {
                            FacesContext fc = FacesContext.getCurrentInstance();
                            fc.addMessage("transactionForm:amount", new FacesMessage("Advertiser must be selected"));
                            return;
                        }
                    }
                    else {
                        TransactionalRunner runner = getTransactionalRunner();
                        runner.runTransactional(
                                new Runnable() {
                                    public void run() {
                                        loadAdvertiserAccount();
                                    }
                                }
                        );
                        account = advertiser.getAccount();
                    }
                    break;
                case PUBLISHER:
                default:
                    account = publisherAccount;
                    break;
            }
        }

        if (account != null) {
        	BigDecimal amount = accountDetail.getAmount();
            if (accountDetail.getTransactionType() == TransactionType.FUNDS_OUT) {
                if (account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0) {
                	accountDetail.setAmount(amount.multiply(BigDecimal.valueOf(-1.0)));
                } else if (account.getBalance().setScale(2, RoundingMode.CEILING).subtract(amount).compareTo(BigDecimal.ZERO) >= 0) {
                	accountDetail.setAmount(account.getBalance().multiply(BigDecimal.valueOf(-1.0)));
                } else {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage("transactionForm:amount", new FacesMessage("Amount must not have a negative balance"));
                    return;
                }
            }
            try {
                TransactionalRunner runner = getTransactionalRunner();
                final Account finalAccount = account;
                runner.callTransactional(
                        new Callable<AccountDetail>() {
                            public AccountDetail call() throws Exception {
                            	return getAccountManager().newAccountDetail(finalAccount, new Date(), accountDetail.getAmount(), BigDecimal.ZERO,
                            			accountDetail.getTransactionType(), accountDetail.getDescription(), accountDetail.getReference(), accountDetail.getOpportunity());
                            }
                        }
                );
                setRequestFlag("didUpdateBalance");
                
                // Set account form fields to empty
                accountDetail = new AccountDetailDto();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error saving account detail for account id=" + account.getId(), e);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        companyAdvertisers = loadCompanyAdvertisers();
        agencyAccount = user.getCompany().isAccountType(AccountType.AGENCY);
        advertiser = loadAdvertiser();
        publisher = getPublisherManager().getPublisherById(user.getCompany().getPublisher().getId());
        publisherAccount = getAccountManager().getAccountById(publisher.getAccount().getId());
    }

    public List<SelectItem> loadCompanyAdvertisers() {
        if (companyAdvertisers == null) {
            List<SelectItem> items = new ArrayList<SelectItem>();
            for (Advertiser a : getAdvertiserManager().getAllAdvertisersForCompany(user.getCompany())) {
                items.add(new SelectItem(a, (StringUtils.isBlank(a.getName()) ? "[default]" : a.getName()) + "/" + a.getId()));
            }
            companyAdvertisers = items;
        }
        return companyAdvertisers;
    }

    public void loadAdvertiserAccount() {
        advertiser = getAdvertiserManager().getAdvertiserById(advertiser.getId());
        // hydrate
        advertiser.getAccount().getBalance();
    }
    
    public Advertiser loadAdvertiser(){
        Iterator<Advertiser> iter = getAdvertiserManager().getAllAdvertisersForCompany(user.getCompany()).iterator();
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }
}
