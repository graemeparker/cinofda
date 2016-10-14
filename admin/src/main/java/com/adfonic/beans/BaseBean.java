package com.adfonic.beans;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Format;
import com.adfonic.domain.Language;
import com.adfonic.domain.Publication;
import com.adfonic.domain.User;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailService;
import com.adfonic.reporting.EntityResolver;
import com.adfonic.util.EnumerationUtils;
import com.adfonic.util.FacesContextHelper;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CategoryHierarchyService;
import com.byyd.middleware.common.service.CategorySearchService;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.creative.service.ExtendedCreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.jpa.GenericCachingEntityResolver;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;
import com.byyd.middleware.utils.TransactionalRunner;

/**
 * Note: the names of methods in this class purposefully avoid using getXXX()
 * notation to ensure they will not be confused with bean properties.
 */
public class BaseBean {
    protected Logger logger = Logger.getLogger(getClass().getName());

    public static final TimeZone DEFAULT_TZ = TimeZone.getTimeZone("GMT");

    @ManagedProperty(value = "#{categorySearchService}")
    protected CategorySearchService categorySearchService;
    public void setCategorySearchService(CategorySearchService categorySearchService) {
        this.categorySearchService = categorySearchService;
    }

    @ManagedProperty(value = "#{categoryHierarchyService}")
    protected CategoryHierarchyService categoryHierarchyService;
    public void setCategoryHierarchyService(CategoryHierarchyService categoryHierarchyService) {
        this.categoryHierarchyService = categoryHierarchyService;
    }

    @ManagedProperty(value = "#{emailService}")
    protected EmailService emailService;

    @ManagedProperty(value = "#{emailAddressManager}", name = "emailAddressManager")
    protected EmailAddressManager emailAddrMgr;
    
    @ManagedProperty(value = "#{userManager}", name = "userManager")
    private UserManager userManager;
    
    @ManagedProperty(value = "#{advertiserManager}", name = "advertiserManager")
    private AdvertiserManager advertiserManager;

    @ManagedProperty(value = "#{publisherManager}", name = "publisherManager")
    private PublisherManager publisherManager;
    
    @ManagedProperty(value = "#{companyManager}", name = "companyManager")
    private CompanyManager companyManager;
    
    @ManagedProperty(value = "#{accountManager}", name = "accountManager")
    private AccountManager accountManager;
    
    @ManagedProperty(value = "#{commonManager}", name = "commonManager")
    private CommonManager commonManager;
    
    @ManagedProperty(value = "#{creativeManager}", name = "creativeManager")
    private CreativeManager creativeManager;
    
    @ManagedProperty(value = "#{extendedCreativeManager}", name = "extendedCreativeManager")
    private ExtendedCreativeManager extendedCreativeManager;
    
    @ManagedProperty(value = "#{assetManager}", name = "assetManager")
    private AssetManager assetManager;
    
    @ManagedProperty(value = "#{deviceManager}", name = "deviceManager")
    private DeviceManager deviceManager;
    
    @ManagedProperty(value = "#{campaignManager}", name = "campaignManager")
    private CampaignManager campaignManager;
    
    @ManagedProperty(value = "#{targetingManager}", name = "targetingManager")
    private TargetingManager targetingManager;
    
    @ManagedProperty(value = "#{biddingManager}", name = "biddingManager")
    private BiddingManager biddingManager;
    
    @ManagedProperty(value = "#{feeManager}", name = "feeManager")
    private FeeManager feeManager;

    @ManagedProperty(value = "#{publicationManager}", name = "publicationManager")
    private PublicationManager publicationManager;

    @ManagedProperty(value="#{jmsTemplate}",name="jmsTemplate")
    private JmsTemplate jmsTemplate;
    
    @ManagedProperty(value = "#{transactionalRunner}", name = "transactionalRunner")
    private TransactionalRunner transactionalRunner;
    
    private static Set<Advertiser> advertisersForAdfonicUser = null;
    
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
    
    public void setAdvertiserManager(AdvertiserManager advertiserManager) {
        this.advertiserManager = advertiserManager;
    }

    public void setPublisherManager(PublisherManager publisherManager) {
        this.publisherManager = publisherManager;
    }

    public void setCompanyManager(CompanyManager companyManager) {
        this.companyManager = companyManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void setCommonManager(CommonManager commonManager) {
        this.commonManager = commonManager;
    }

    public void setCreativeManager(CreativeManager creativeManager) {
        this.creativeManager = creativeManager;
    }

    public void setExtendedCreativeManager(ExtendedCreativeManager extendedCreativeManager) {
        this.extendedCreativeManager = extendedCreativeManager;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public void setCampaignManager(CampaignManager campaignManager) {
        this.campaignManager = campaignManager;
    }
    
    public void setTargetingManager(TargetingManager targetingManager) {
        this.targetingManager = targetingManager;
    }

    public void setBiddingManager(BiddingManager biddingManager) {
        this.biddingManager = biddingManager;
    }

    public void setFeeManager(FeeManager feeManager) {
		this.feeManager = feeManager;
	}

	public void setPublicationManager(PublicationManager publicationManager) {
        this.publicationManager = publicationManager;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setEmailAddressManager(EmailAddressManager emailAddrMgr) {
        this.emailAddrMgr = emailAddrMgr;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    public UserManager getUserManager() {
        if(userManager == null) {
            userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        }
        return userManager;
    }

    public AdvertiserManager getAdvertiserManager() {
        if(advertiserManager == null) {
            advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
        }
        return advertiserManager;
    }

    public PublisherManager getPublisherManager() {
        if(publisherManager == null) {
            publisherManager = AdfonicBeanDispatcher.getBean(PublisherManager.class);
        }
        return publisherManager;
    }

    public CompanyManager getCompanyManager() {
        if(companyManager == null) {
            companyManager = AdfonicBeanDispatcher.getBean(CompanyManager.class);
        }
        return companyManager;
    }

    public AccountManager getAccountManager() {
        if(accountManager == null) {
            accountManager = AdfonicBeanDispatcher.getBean(AccountManager.class);
        }
        return accountManager;
    }
    
    public CommonManager getCommonManager() {
        if(commonManager == null) {
            commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        }
        return commonManager;
    }
    
    public CreativeManager getCreativeManager() {
        if(creativeManager == null) {
            creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
        }
        return creativeManager;
    }
    
    public ExtendedCreativeManager getExtendedCreativeManager() {
        if(extendedCreativeManager == null) {
            extendedCreativeManager = AdfonicBeanDispatcher.getBean(ExtendedCreativeManager.class);
        }
        return extendedCreativeManager;
    }
    
    public AssetManager getAssetManager() {
        if(assetManager == null) {
            assetManager = AdfonicBeanDispatcher.getBean(AssetManager.class);
        }
        return assetManager;
    }
    
    public DeviceManager getDeviceManager() {
        if(deviceManager == null) {
            deviceManager = AdfonicBeanDispatcher.getBean(DeviceManager.class);
        }
        return deviceManager;
    }

    public CampaignManager getCampaignManager() {
        if(campaignManager == null) {
            campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        }
        return campaignManager;
    }
    
    public TargetingManager getTargetingManager() {
        if(targetingManager == null) {
            targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        }
        return targetingManager;
    }
    

    public BiddingManager getBiddingManager() {
        if(biddingManager == null) {
            biddingManager = AdfonicBeanDispatcher.getBean(BiddingManager.class);
        }
        return biddingManager;
    }
    
    public FeeManager getFeeManager() {
        if(feeManager == null) {
        	feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        }
        return feeManager;
    }
    
    public PublicationManager getPublicationManager() {
        if(publicationManager == null) {
            publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
        }
        return publicationManager;
    }

    /** Session attribute name for saving the AdfonicUser */
    static final String ADFONIC_USER_KEY = "adfonicUser";

    /** This method signature is meant to be called from Servlets as well. */
    public static AdfonicUser adfonicUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        } else {
            return (AdfonicUser) session.getAttribute(ADFONIC_USER_KEY);
        }
    }
    
    /** This method returns true if the logged user is restricted */
    public static boolean isRestrictedUser(){
        return Functions.hasAdminRole(adfonicUser(), "RestrictedAdmin");
    }
    
    public static boolean isSuperAdmin(){
        return Functions.hasAdminRole(adfonicUser(), "SuperAdmin");
    }
    
    public static void loadAdvertisers(){
        if(isRestrictedUser()){
            advertisersForAdfonicUser = new HashSet<Advertiser>();
            AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
            UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
            List<User> users = userManager.getUsersForAdfonicUser(adfonicUser()); 
            for(User u : users){
                List<Advertiser> advertisers = null;
                if (u.getCompany().isAccountType(AccountType.AGENCY)){
                    advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(u,"");
                }
                else{
                    AdvertiserFilter advertiserFilter = new AdvertiserFilter().setCompany(u.getCompany());
                    advertisers = advertiserManager.getAllAdvertisers(advertiserFilter);
                }
                advertisersForAdfonicUser.addAll(advertisers);
            }
        }
    }
    
    /** This method returns the list of advertisers that adfonic users has access to*/
    public static Collection<Advertiser> getAdvertisersForAdfonicUser(){
        loadAdvertisers();
        return advertisersForAdfonicUser;
    }

    /**
     * Set the logged-in AdfonicUser in session
     */
    public static void setAdfonicUser(HttpServletRequest request, AdfonicUser adfonicUser) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ADFONIC_USER_KEY, adfonicUser);
    }

    /**
    * Helper method to get the User. Copies the (detached) user from the
    * session (if any), reattaches it, and stores it in request scope.
    */
    public static AdfonicUser adfonicUser() {
        return adfonicUser((HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest());
    }
    
    protected static String getUserId() {
    	AdfonicUser user = adfonicUser();
        return user == null ? "null" : String.valueOf(user.getId());
    }

    /**
    * Gets a message from Messages.properties.
    */
    public static FacesMessage messageForId(String messageID, Object... args) {
        return MessageFactory.getMessage(messageID, args);
    }

    /**
    * Gets the scheme, hostname, port, and any URL prefix associated with the
    * running application, e.g. http://www.adfonic.com or
    * http://localhost:8080/adfonic-tools
    *
    * The output of this method will NOT have a trailing slash.
    */
    public static String getURLRoot() {
        return getURLRoot(true);
    }

    /**
    * Gets the scheme, hostname, port, and an optional context path associated
    * with the running application, e.g. http://www.adfonic.com or
    * http://localhost:8080/adfonic-tools
    *
    * The output of this method will NOT have a trailing slash.
    *
    * @param includeContextPath
    *            whether or not to include the context path for the running
    *            application (i.e. /adfonic-tools)
    */
    public static String getURLRoot(boolean includeContextPath) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) fc
                .getExternalContext().getRequest();
        ServletContext sc = (ServletContext) fc.getExternalContext()
                .getContext();
        return getURLRoot(request, sc, includeContextPath);
    }

    public static String getURLRoot(HttpServletRequest request,
            ServletContext sc) {
        return getURLRoot(request, sc, true);
    }

    public static String getURLRoot(HttpServletRequest request,
            ServletContext sc, boolean includeContextPath) {
        StringBuilder out = new StringBuilder();
        String scheme = request.getScheme();
        out.append(request.getScheme()).append("://")
                .append(request.getServerName());
        int port = request.getServerPort();
        if (("http".equals(scheme) && port != 80)
                || ("https".equals(scheme) && port != 443)) {
            out.append(':').append(port);
        }
        if (includeContextPath) {
            out.append(sc.getContextPath());
        }
        return out.toString();
    }

    public static Language getUserLanguage() {
        // Static methods cannot use the AutoWired stuff.
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);

        String languages = ((HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest())
                .getHeader("Accept-Language");

        return commonManager.getLanguageByIsoCode(extractLanguage(languages));
    }

    public static String extractLanguage(String languages) {
        if (languages != null) {
            // Use a tokenizer to separate acceptable languages
            StringTokenizer tokenizer = new StringTokenizer(languages, ",");

            while (tokenizer.hasMoreTokens()) {
                String lang = tokenizer.nextToken();
                Locale loc = getLocaleForLanguage(lang);
                return loc.getLanguage();
            }
        }
        return "en"; // default
    }

    private static Locale getLocaleForLanguage(String lang) {
        Locale loc;
        int semi, dash;

        // Cut off any q-value that might come after a semi-colon
        if ((semi = lang.indexOf(';')) != -1) {
            lang = lang.substring(0, semi);
        }

        // Trim any whitespace
        lang = lang.trim();

        // Create a Locale from the language. A dash may separate the
        // language from the country.
        if ((dash = lang.indexOf('-')) == -1) {
            loc = new Locale(lang, ""); // No dash, no country
        } else {
            loc = new Locale(lang.substring(0, dash), lang.substring(dash + 1));
        }

        return loc;
    }

    public static Locale getUserLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    /** Convenience method for accessing request parameters. */
    public String getRequestParameter(String name) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) fc
                .getExternalContext().getRequest();
        return request.getParameter(name);
    }

    public String templateToString(String templateResource,
            Map<String, Object> values) {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            return FacesContextHelper.evaluateTemplate(fc, getClass()
                    .getResourceAsStream(templateResource), values);
        } catch (IOException e) {
            throw new AdminGeneralException("Failed to evaluate template: " + templateResource, e);
        }
    }

    public void setRequestFlag(String attributeName) {
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute(attributeName, Boolean.TRUE);
    }

    public void removeSessionAttribute(String attributeName) {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        if (session != null) {
            session.removeAttribute(attributeName);
        }
    }

    /**
    * Clears out the contents of the current session without invalidating it.
    */
    public void clearSession() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        if (session == null) {
            return;
        }

        for (String attributeName : EnumerationUtils.makeIterable(session.getAttributeNames())) {
                session.removeAttribute(attributeName);
        }
    }

    protected final EntityResolver<Campaign> getCampaignResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Campaign>(Campaign.class, getCampaignManager(), fetchStrategy);
    }

    protected final EntityResolver<Creative> getCreativeResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Creative>(Creative.class, getCreativeManager(), fetchStrategy);
    }

    protected final EntityResolver<Format> getFormatResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Format>(Format.class, getCreativeManager(), fetchStrategy);
    }

    protected final EntityResolver<Publication> getPublicationResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Publication>(Publication.class, getPublicationManager(), fetchStrategy);
    }
    
    public TransactionalRunner getTransactionalRunner() {
        if(transactionalRunner == null) {
            transactionalRunner = AdfonicBeanDispatcher.getBean(TransactionalRunner.class);
        }
        return transactionalRunner;
    }

    public void setTransactionalRunner(TransactionalRunner transactionalRunner) {
        this.transactionalRunner = transactionalRunner;
    }
 
    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TZ;
    }
    
    protected String emptyToNull(String input) {
        return (StringUtils.isEmpty(input)) ? null : input;
    }
}
