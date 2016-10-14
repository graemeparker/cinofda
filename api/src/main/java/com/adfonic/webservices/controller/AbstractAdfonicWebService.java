package com.adfonic.webservices.controller;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Country;
import com.adfonic.domain.PluginVendor;
import com.adfonic.domain.Publication;
import com.adfonic.domain.User;
import com.adfonic.reporting.EntityResolver;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.exception.AuthenticationException;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.interceptor.AuthenticationInterceptor;
import com.adfonic.webservices.interceptor.PluginVendorAuthenticationInterceptor;
import com.adfonic.webservices.view.GenericView;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.jpa.GenericCachingEntityResolver;
import com.byyd.middleware.integrations.service.IntegrationsManager;
import com.byyd.middleware.publication.service.PublicationManager;

public abstract class AbstractAdfonicWebService {
    private static final transient Logger LOG = Logger.getLogger(AbstractAdfonicWebService.class.getName());
    
    public static final String ADVERTISER					= "advertiser";
    public static final String ADVERTISERS					= "advertisers";
    public static final String CAMPAIGN						= "campaign";
    public static final String CAMPAIGNS					= "campaigns";

    @Autowired
    private UserManager userManager;
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private CompanyManager companyManager;
    @Autowired
    private CommonManager commonManager;
    @Autowired
    private CampaignManager campaignManager;
    @Autowired
    private TargetingManager targetingManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private AudienceManager audienceManager;
    @Autowired
    private IntegrationsManager integrationsManager;

    public UserManager getUserManager() {
        return userManager;
    }
    
    protected AdvertiserManager getAdvertiserManager() {
        return advertiserManager;
    }

    protected PublisherManager getPublisherManager() {
        return publisherManager;
    }

    protected final CompanyManager getCompanyManager() {
        return companyManager;
    }

    protected final CommonManager getCommonManager() {
        return commonManager;
    }
    
    protected CampaignManager getCampaignManager() {
        return campaignManager;
    }

    protected TargetingManager getTargetingManager() {
        return targetingManager;
    }

    protected final DeviceManager getDeviceManager() {
        return deviceManager;
    }
    
    protected final CreativeManager getCreativeManager() {
        return creativeManager;
    }

    protected PublicationManager getPublicationManager() {
        return publicationManager;
    }

    protected AudienceManager getAudienceManager() {
        return audienceManager;
    }

    protected IntegrationsManager getIntegrationsManager() {
        return integrationsManager;
    }

    /** Authenticate a User on the request.  This method will always return
        a non-null User object, otherwise it will throw an exception that
        can be used to render an error view.
    */
    protected User authenticate(HttpServletRequest request, String format) throws WebServiceException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Setting format for " + request.getRequestURI() + " to " + format);
        }
        AbstractAdfonicWebService.format.set(format);
        
        User user = AuthenticationInterceptor.getAuthenticatedUserFromRequest(request);
        if (user != null) {
            return user;
        }

        // The user wasn't authenticated...let's propagate the authentication exception
        // out in a format-friendly way.
        AuthenticationException exception = AuthenticationInterceptor.getAuthenticationExceptionFromRequest(request);
        if (exception != null) {
            throw new WebServiceException(exception.getCode(), exception.getMessage(), format);
        } else {
            // This should never happen unless something is mis-configured
            LOG.severe("No authenticated User and no AuthenticationException...is the AuthenticationInterceptor enabled?");
            throw new WebServiceException(ErrorCode.AUTH_NO_AUTHORIZATION, "Authentication not performed", format);
        }
    }
    
    protected PluginVendor authenticatePluginVendor(HttpServletRequest request, String format) throws WebServiceException {
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Setting format for " + request.getRequestURI() + " to " + format);
        }
        
        AbstractAdfonicWebService.format.set(format);
        
        PluginVendor pluginVendor = PluginVendorAuthenticationInterceptor.getAuthenticatedPluginVendorFromRequest(request);
        
        if (pluginVendor != null) {
            return pluginVendor;
        }
        
        AuthenticationException exception = PluginVendorAuthenticationInterceptor.getAuthenticationExceptionFromRequest(request);
        if (exception != null) {
            throw new WebServiceException(exception.getCode(), exception.getMessage(), format);
        } else {
            // This should never happen unless something is mis-configured
            LOG.severe("No authenticated PluginVendor and no AuthenticationException...is the PluginVendorAuthenticationInterceptor enabled?");
            throw new WebServiceException(ErrorCode.AUTH_NO_AUTHORIZATION, "Authentication not performed", format);
        }
    }
    
    /** Return a ModelAndView that points to the respective format's
        error view, and stores the error code and message for the view
        to render.
    */
    @ExceptionHandler(WebServiceException.class)
    public ModelAndView handleWebServiceException(WebServiceException e) {
        ModelAndView mv = new ModelAndView(e.getResponseFormat() + "ErrorView");
        mv.addObject("code", Integer.valueOf(e.getCode()));
        mv.addObject("error", e.getMessage());
        Integer responseStatus=e.getResponseStatus();
        if(responseStatus!=null){
        	mv.addObject("responseStatus", responseStatus);
        }
        return mv;
    }
    
    /** Same as above for ServiceException. Format implied by the thread */
    @ExceptionHandler(ServiceException.class)
    public ModelAndView handleServiceException(ServiceException e) {
        ModelAndView mv = new ModelAndView(format.get() + "ErrorView");
        int errorCode = e.getErrorCode();
        mv.addObject("code", Integer.valueOf(errorCode));
        mv.addObject("error", e.getMessage());
        Integer statusCode;
        statusCode = ErrorCode.Status.map.get(errorCode);
        if (statusCode == null) {
            statusCode = 403;// spec says 403 for everything; so default it
        }
        mv.addObject("responseStatus", statusCode);
        return mv;
    }

    private static ThreadLocal<String> format = new ThreadLocal<String>();// no need for initialValue

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ModelAndView handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        String responseFormat = FilenameUtils.getExtension(request.getRequestURI());
        ModelAndView mv = new ModelAndView(responseFormat + "ErrorView");
        mv.addObject("code", ErrorCode.PARAMETER_MISSING);
        mv.addObject("error", "Required parameter not provided: \"" + e.getParameterName() + "\"");
        return mv;
    }

    @ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class })
    public ModelAndView handleSelectExceptions(Exception e, HttpServletRequest request) {
        //  Things IllegalArgumentException is common for missing parameters and so forth, so we don't care about that.
        return handleExceptions(e, request, false);
    }
    
    @ExceptionHandler
    public ModelAndView handleGeneralException(Exception e, HttpServletRequest request) {
        //  Log traces for any other general exceptions, which should be logged.
        return handleExceptions(e, request, true);
    }

    public ModelAndView handleExceptions(Exception e, HttpServletRequest request, boolean doScavenge) {
        // Call Exception by the name of Exception to make sure error scavenger does not pick it up just for this. Not certain if needed.
        // Scavenger behavior not clear from desc and don't have logtail on local to do a quick experiment 
        StringBuilder message = new StringBuilder().append("Exception caught during request from ").append(request.getRemoteAddr());
        User user = AuthenticationInterceptor.getAuthenticatedUserFromRequest(request);
        if (user != null) {
            message.append(" by User id=").append(user.getId()).append(", email=").append(user.getEmail());
        } else {
            message.append(" (no User)");
        }
        message.append(" for ").append(request.getMethod()).append(" ").append(request.getRequestURI());
        if (!StringUtils.isEmpty(request.getQueryString())) {
            message.append('?').append(request.getQueryString());
        }
        
        String responseFormat = FilenameUtils.getExtension(request.getRequestURI());
        ModelAndView mv = new ModelAndView(responseFormat + "ErrorView");
        //mv.addObject("error", e.getClass().getName() + ": " + e.getMessage());
        String userErrorMessage=getUserErrorMessage(e);
        mv.addObject("error", userErrorMessage);
        //if error-description is specified, error-code should be too. 
        mv.addObject("code", ErrorCode.PROCESSING_ERROR);
        // TODO - categorize errors; return exception details only for user errors;
        //   - change err-code to 4XXX for user errors; 500X should not carry exception details

        if(doScavenge){
            LOG.log(Level.WARNING, message.insert(0, 'E').toString(), e);
        }else{
            LOG.log(Level.WARNING, message.append(" : ").append(userErrorMessage).toString());
        }
        
        return mv;
    }

    protected String itemView(Object object, String format, Model model) {
        model.addAttribute(GenericView.RESULT, object);
        return format + GenericView.BASE_VIEWNAME;
    }    
    
    protected String listView(List<?> list, String format, Model model, String wrapperName) {
        model.addAttribute(GenericView.RESULT, list);
        model.addAttribute(GenericView.RESULT_LIST_WRAPPER, wrapperName);
        return format + GenericView.BASE_VIEWNAME;
    }
    
    protected static String treatBlankAsNull(String value) {
        return "".equals(value) ? null : value;
    }
    
    private String getUserErrorMessage(Throwable t) {
        Throwable cause = t.getCause();
        return cause == null ? t.getMessage() : getUserErrorMessage(cause);
    }

    protected final EntityResolver<Campaign> getCampaignResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Campaign>(Campaign.class, getCampaignManager(), fetchStrategy);
    }

    protected final EntityResolver<AdSpace> getAdSpaceResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<AdSpace>(AdSpace.class, getPublicationManager(), fetchStrategy);
    }
    
    protected final EntityResolver<Publication> getPublicationResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Publication>(Publication.class, getPublicationManager(), fetchStrategy);
    }
    
    protected final EntityResolver<Country> getCountryResolver(final FetchStrategy ... fetchStrategy) {
        return new GenericCachingEntityResolver<Country>(Country.class, getCommonManager(), fetchStrategy);
    }
}
