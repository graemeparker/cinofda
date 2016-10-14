package com.adfonic.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Company;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.utils.TransactionalRunner;

@SessionScoped
@ManagedBean
public class AdminAccountBean extends BaseBean {

    public static final String PRETTY_ADMIN_HOME = "pretty:adminHome";
    
    private String email;
    private User user;
    private Advertiser advertiser;
    private Company company;
    private Publisher publisher;
    
    private User myAccountUser;
    private boolean agencyAccount;
    
    public AdminAccountBean(){
    }
    
    /*
     * Used to restrict access to views when no user is loaded to 
     * administer
     *    <f:metadata>
     *        <f:event 
     *        	type="preRenderView" 
     *        	listener="#{adminAccountBean.adminAccountUserCheck}" />
     *    </f:metadata>
     */
    public void adminAccountUserCheck() {
        if (user == null) {
        	logger.log(Level.FINE, 
        			"No user loaded sending home.");
            FacesContext context = 
            		FacesContext.getCurrentInstance();
            NavigationHandler navigator = 
            		context.getApplication().getNavigationHandler();
            navigator.handleNavigation(
            		context, 
            		null, 
            		PRETTY_ADMIN_HOME);
            context.responseComplete();
        }
    }

    // Autocomplete user query method
    public List<String> doUserQuery(String search) {
        List<String> userEmails = new ArrayList<String>();
        List<User> users = new ArrayList<User>();
        AdfonicUser currentUser = null;
        if(isRestrictedUser()){
            currentUser = adfonicUser();
        }
        if (StringUtils.isNotBlank(search)) {
            users = getUserManager().getAllUsersForEmailLike(
                    search,
                    currentUser,
                    new Pagination(0,50,
                    new Sorting(SortOrder.asc("email"))));
            for (User u : users) {
                userEmails.add(u.getEmail());
            }
        }
        return userEmails;
    }
    // load the user to administer
    private String loadUser(User user) {
        if (user != null) {
            this.user = user;
            TransactionalRunner runner = getTransactionalRunner();
            runner.runTransactional(
                        new Runnable() {
                            public void run() {
                                loadUser();
                            }
                        }
                    );
            return "accountDetails";
        }
        else {
            return null;
        }            
    }

    public String doFindEmail() {
        User user = getUserManager().getUserByEmail(email);
        if (user != null) {
            return loadUser(user);
        }
        else {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("mainForm:userEmail",
                    messageForId("error.accountInfo.email"));
        }
        return null;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getCompanyAdvertiserBalance() {
        return getCompanyManager().getTotalAdvertiserBalance(company);
    }

    public BigDecimal getCompanyPublisherBalance() {
        return getCompanyManager().getTotalPublisherBalance(company);
        //return publisher.getAccount().getBalance();
    }

    public boolean isAgencyAccount() {
        return agencyAccount;
    }

    public void setAgencyAccount(boolean agencyAccount) {
        this.agencyAccount = agencyAccount;
    }

    // load the selected user to administer
    public String doLoadMyAccount() {
        if (myAccountUser != null) {
            return loadUser(myAccountUser);
        }
        return null;
    }

    public User getMyAccountUser() {
        return myAccountUser;
    }

    public void setMyAccountUser(User myAccountUser) {
        this.myAccountUser = myAccountUser;
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

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void loadUser() {
        user = getUserManager().getUserById(user.getId());
        company = user.getCompany();
        publisher = user.getCompany().getPublisher();
        //hydrate
        publisher.getAccount().getBalance();
        
        advertiser = null;
        // if not agency we can snag the advertiser as well
        if (!user.getCompany().isAccountType(AccountType.AGENCY)) {
            agencyAccount = false;
            Iterator<Advertiser> iter = getAdvertiserManager().getAllAdvertisersForCompany(user.getCompany()).iterator();
            if (iter.hasNext()) {
                advertiser = iter.next();
            } else {
                advertiser = null;
            }
            if (advertiser != null) {
                advertiser = getAdvertiserManager().getAdvertiserById(advertiser.getId());
            }
        }
        else {
            agencyAccount = true;
        }
    }
}

