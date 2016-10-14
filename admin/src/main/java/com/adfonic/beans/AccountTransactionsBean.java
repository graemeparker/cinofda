package com.adfonic.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.utils.TransactionalRunner;

@ManagedBean
@ViewScoped
public class AccountTransactionsBean extends BaseBean {
	
	private static final transient Logger LOG = Logger.getLogger(AdminAdManagementBean.class.getName());

    private User user;
    private Advertiser advertiser;
    private Publisher publisher;
    private List<SelectItem> companyAdvertisers;
    private Account publisherAccount;
    private List<AccountDetail> advertiserTransactions;
    private List<AccountDetail> publisherTransactions;
    
    @ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;

    @PostConstruct
    private void init() {
        if (adminAccountBean == null || adminAccountBean.getUser() == null) {
        	// delegating redirection to the view triggers (adminAccountBean.adminAccountUserCheck)
        	LOG.log(Level.FINE, "admin account bean and user must be loaded");
        }else{
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

    public BigDecimal getAdvertiserBalance() {
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

    public List<AccountDetail> getPublisherTransactions() {
        return this.publisherTransactions;
    }

    public List<AccountDetail> getAdvertiserTransactions() {
        return this.advertiserTransactions;
    }    
    
    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        companyAdvertisers = loadCompanyAdvertisers();
        advertiser = loadAdvertiser();
        loadAdvertiserAccount();
        publisher = getPublisherManager().getPublisherById(user.getCompany().getPublisher().getId());
        publisherAccount = getAccountManager().getAccountById(publisher.getAccount().getId());
        this.publisherTransactions = loadPublisherTransactions();
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
        this.advertiserTransactions = loadAdvertiserTransactions();
    }
    
    public Advertiser loadAdvertiser(){
        Iterator<Advertiser> iter = getAdvertiserManager().getAllAdvertisersForCompany(user.getCompany()).iterator();
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }
    
    private List<AccountDetail> loadAdvertiserTransactions() {
        return getAccountManager().getAllTransactions(advertiser.getAccount(), new Sorting(new SortOrder(Direction.DESC, "transactionTime")));
    }
    
    private List<AccountDetail> loadPublisherTransactions() {
        return getAccountManager().getAllTransactions(publisher.getAccount(), new Sorting(new SortOrder(Direction.DESC, "transactionTime")));
    }
    
    
}
