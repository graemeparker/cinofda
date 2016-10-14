package com.adfonic.sso.services;

import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.PaymentOptions_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.adfonic.domain.VerificationCode;
import com.adfonic.util.AdfonicTimeZone;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class UserServiceImpl implements UserService {
    
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());
    
    // User, Advertiser and Publisher fetch Strategies
    protected static final FetchStrategy USER_FS;
    protected static final FetchStrategy USER_CREATION_FS;
    protected static final FetchStrategy ADVERTISER_FS;
    protected static final FetchStrategy PUBLISHER_FS;
    static {
        // NOTE: the publisher gotten from publisher() is actually the one linked to the user object,
        // not a session entity in its own right. So, anything needed to be set on Publisher has
        // to be done using the userFs
        USER_FS = new FetchStrategyBuilder()
                     .addLeft(User_.roles)
                     .addInner(User_.company)
                     .addLeft(Company_.advertisers)
                     .addLeft(Company_.publisher)
                     .addLeft(Company_.paymentOptions)
                     .addLeft(PaymentOptions_.postalAddress)
                     .build();
        
        USER_CREATION_FS = new FetchStrategyBuilder()
                             .addInner(User_.company)
                             .addLeft(User_.roles)
                             .build();

        ADVERTISER_FS = new FetchStrategyBuilder()
                           .addLeft(Advertiser_.campaigns)
                           .addInner(Advertiser_.company)
                           .addInner(Advertiser_.account)
                           .addInner(Company_.defaultTimeZone)
                           .build();

        PUBLISHER_FS = new FetchStrategyBuilder()
                            .addLeft(Publisher_.publications)
                            .addInner(Publisher_.company)
                            .build();
    }
    
    @Autowired
    protected UserManager userManager;
    
    @Autowired
    protected CompanyManager companyManager;
    
    @Autowired
    protected AccountManager accountManager;
    
    @Autowired
    protected AdvertiserManager advertiserManager;
    
    @Autowired
    protected PublisherManager publisherManager;
    
    @Override
    @Transactional
    public User getUserById(Long id){
        return userManager.getUserById(id);
    }
    
    @Override
    @Transactional
    public User getUserByEmail(String email){
        return userManager.getUserByEmail(email);
    }
    
    @Override
    @Transactional
    public VerificationCode resetUserPwd(User user) {
        user.setStatus(User.Status.PASSWORD_RESET);
        userManager.update(user);
        return accountManager.newVerificationCode(user, VerificationCode.CodeType.RESET_PASSWORD);
    }
    
    @Override
    @Transactional
    public boolean verifyUser(User user, VerificationCode vc) {
        boolean result = true;
        try {
            user.setStatus(User.Status.VERIFIED);
            userManager.update(user);
            accountManager.delete(vc);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to activate user id=" + vc.getUser().getId(), e);
            result = false;
        }
        return result;
    }
    
    @Override
    @Transactional
    public User updateUserPwd(Long userId, String password){
        User userResult = null;
        
        User user = getUserById(userId);
        user.setPassword(password);
        user.setStatus(User.Status.VERIFIED);
                
        try {
            userResult = userManager.update(user);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to update password/status for user id=" + user.getId(), e);
        } 
        return userResult;
    }
    
    @Override
    @Transactional
    public User createUser (String companyName,
                            Country country,
                            String email,
                            String firstName,
                            String lastName,
                            String password,
                            String referralType,
                            String referralTypeOther,
                            List<String> roleNames,
                            AccountType accountType,
                            CurrencyExchangeRate defaultCurrencyExchangeRate,
                            AdfonicTimeZone timezone,
                            boolean keepMeInformed){
        
        //Create the company
        Company company = companyManager.newCompany(companyName, country, defaultCurrencyExchangeRate);        
        company.setCountry(country);
        company.setIndividual(true);
        company.setName(companyName);
        company.setDefaultTimeZone(TimeZone.getTimeZone(timezone.getId()));
        company.setIsInvoiceDateInGMT(false);
        company.setTaxCode(null);
        if (AccountType.ADVERTISER == accountType) {
            company.clearAccountTypeFlag(AccountType.PUBLISHER);
            company.setAccountTypeFlag(AccountType.ADVERTISER); 
            // MAX-2676: Agency Console - default application
            company.setAccountTypeFlag(AccountType.AGENCY);
            roleNames.add(Role.USER_ROLE_AGENCY);
        } else if (AccountType.PUBLISHER == accountType) {
            company.clearAccountTypeFlag(AccountType.ADVERTISER);
            company.setAccountTypeFlag(AccountType.PUBLISHER);
        }
        //AD-200 - default to GMT for invoicing timezone;
        //And changing again... MAD-581 - Invoicing time zone to default to account
        company.setIsInvoiceDateInGMT(false);
        company = companyManager.update(company);
        
        // Then the user
        User user = userManager.newUser(company, email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(null);
        user.setTimeZone(TimeZone.getTimeZone(timezone.getId()));
        user.setCountry(country);
        user.setEmailOptIn(keepMeInformed);
        user.updateLastLogin();
        user.setReferralType(referralType);
        user.setReferralTypeOther(referralTypeOther);
        if(roleNames != null && !roleNames.isEmpty()) {
            for(String roleName : roleNames) {
                Role role = userManager.getRoleByName(roleName);
                if(role != null) {
                     user.getRoles().add(role);
                }
            }
        }
        user = userManager.update(user);
        
        // Update user roles
        if(roleNames != null && !roleNames.isEmpty()) {
            for(String roleName : roleNames) {
                Role role = userManager.getRoleByName(roleName);
                if(role != null) {
                     user.getRoles().add(role);
                }
            }
        }
        user = userManager.update(user);

        // Make the user the primary account for this company
        company.setAccountManager(user);
        company.setAffiliateProgram(null);
        company = companyManager.update(company);

        if (AccountType.AGENCY!=accountType) {
            advertiserManager.newAdvertiser(company, companyName);
            publisherManager.newPublisher(company, companyName, Publisher.DEFAULT_REV_SHARE);
        }
        
        // Reload and return a properly hydrated user. Add things to the FS if
        // more is needed in the RegisterBean
        return userManager.getUserById(user.getId(), USER_CREATION_FS);
        
    }
}
