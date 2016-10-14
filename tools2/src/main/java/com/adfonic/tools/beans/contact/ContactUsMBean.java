package com.adfonic.tools.beans.contact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.email.MailService;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("request")
@URLMappings(mappings = { @URLMapping(id = "support", pattern = "/support", viewId = "/WEB-INF/jsf/contact-pages/support.jsf"),
        @URLMapping(id = "salesContact", pattern = "/about/contact-us", viewId = "/WEB-INF/jsf/contact-pages/sales.jsf"), })
public class ContactUsMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ContactUsMBean.class);

    @Autowired
    private ContactConfiguration config;

    @Autowired
    private LocationService locationService;

    @Autowired
    private MailService mailService;

    // Manage bean properties
    private List<CountryDto> countries = null;
    private CountryDto selectedCountry;
    private String email;
    private String name;
    private String phoneNumber;
    private String description;
    private Boolean sended = null;

    private enum FormType {
        SUPPORT, SALES
    }

    @Override
    @URLActions(actions = { @URLAction(mappingId = "salesContact"), @URLAction(mappingId = "support") })
    public void init() throws Exception {
        sended = null;
    }

    public void doSalesRequest() throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attending sales contact request for email " + this.email);
        }
        this.sended = sendRequest(FormType.SALES);
    }

    public void doSupportRequest() throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attending support request for email " + this.email);
        }
        this.sended = sendRequest(FormType.SUPPORT);
    }

    private Boolean sendRequest(FormType formType) {
        Boolean result = sendSalesforceRequest(formType);
        cleanForm();
        return result;
    }

    private Boolean sendSalesforceRequest(FormType formType) {
        Boolean requestSended = false;
        HttpClient httpclient = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating Salesforce " + formType.name() + " request for user email " + this.email);
            }
            httpclient = new DefaultHttpClient();

            HttpPost httpPost = null;
            if (formType == FormType.SUPPORT) {
                httpPost = new HttpPost(config.getSalesforceCasesUrl());
            } else {
                httpPost = new HttpPost(config.getSalesforceLeadsUrl());
            }

            List<NameValuePair> parameters = buildHttpParams(formType);
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sending Salesforce request for user email " + this.email + " using parameters: " + parameters);
            }
            HttpResponse response = httpclient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() > 400) {
                LOGGER.error("Salesforce response received for user email " + this.email + " with code status: " + statusLine);
            } else {
                requestSended = true;
                LOGGER.info("Salesforce response received for user email " + this.email + " with code status: " + statusLine);
                if (config.getDebug()) {
                    if (LOGGER.isDebugEnabled()) {
                        String logMsg = null;
                        try {
                            logMsg = getHttpResponse(response);
                        } catch (RuntimeException ex) {
                            httpPost.abort();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Exception reading salesforce response on debug mode.", ex);
                            }
                        }
                        LOGGER.info("Salesforce response (debug mode): " + logMsg);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception ocurred managing salesforce contact request for user email " + this.email, e);
        } finally {
            if (httpclient != null && httpclient.getConnectionManager() != null) {
                httpclient.getConnectionManager().shutdown();
            }
        }
        return requestSended;
    }

    private List<NameValuePair> buildHttpParams(FormType formType) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        // Contact information
        // Email
        parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_EMAIL, this.email));

        // Phone
        parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_PHONE, this.phoneNumber));
        // Country
        String countryName = getSelectedCountryName();
        if (countryName != null) {
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_COUNTRY, countryName));
        }

        // Salesforce config
        if (formType == FormType.SUPPORT) {
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_ORGID, config.getSalesforceOrgId()));
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_NAME, this.name));
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_DESCRIPTION, this.description));
        } else {
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_OID, config.getSalesforceOrgId()));
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_FIRST_NAME, this.name));
            // description
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_00N20000007w7Pg, this.description));
        }

        parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_LEAD_SOURCE, config.getLeadSource()));
        // parameters.add(new
        // BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_RETURL,
        // ""));

        // Debug information
        if (config.getDebug()) {
            parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_DEBUG, "1"));
            if (StringUtils.isNotEmpty(config.getDebugEmail())) {
                parameters.add(new BasicNameValuePair(ContactConfiguration.SALESFORCE_PARAM_DEBUG_EMAIL, config.getDebugEmail()));
            }
        }

        return parameters;
    }

    private String getHttpResponse(HttpResponse response) throws RuntimeException {
        StringBuffer sbResponse = new StringBuffer();

        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                sbResponse.append(reader.readLine());
            } catch (IOException ex) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception reading salesforce response on debug mode", ex);
                }
            } finally {
                try {
                    if (instream != null) {
                        instream.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return sbResponse.toString();
    }

    private String getSelectedCountryName() {
        String countryName = null;
        CountryDto country = getSelectedCountry();
        if (country != null) {
            countryName = country.getName();
        }
        return countryName;
    }

    private void cleanForm() {
        this.name = null;
        this.email = null;
        this.selectedCountry = null;
        this.phoneNumber = null;
        this.description = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CountryDto> getCountries() {
        if (countries == null) {
            countries = (List<CountryDto>) locationService.getAllCountries();
        }
        return countries;
    }

    public void setCountries(List<CountryDto> countries) {
        this.countries = countries;
    }

    public CountryDto getSelectedCountry() {
        if (selectedCountry == null) {
            UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
            if (userDto != null) {
                selectedCountry = userDto.getCountry();
            }
        }

        return selectedCountry;
    }

    public void setSelectedCountry(CountryDto selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public Boolean getSended() {
        return sended;
    }

    /**
     * Class to retrieve the configuration for this section
     */
    public static class ContactConfiguration implements Serializable {

        private static final long serialVersionUID = 1L;

        private static final String SALESFORCE_PARAM_ORGID = "orgid";
        private static final String SALESFORCE_PARAM_OID = "oid";
        private static final String SALESFORCE_PARAM_LEAD_SOURCE = "lead_source";
        private static final String SALESFORCE_PARAM_DEBUG = "debug";
        private static final String SALESFORCE_PARAM_DEBUG_EMAIL = "debugEmail";
        private static final String SALESFORCE_PARAM_NAME = "name";
        private static final String SALESFORCE_PARAM_FIRST_NAME = "first_name";// don't
                                                                               // freak
                                                                               // out
                                                                               // David.
        private static final String SALESFORCE_PARAM_EMAIL = "email";
        private static final String SALESFORCE_PARAM_COUNTRY = "country";
        private static final String SALESFORCE_PARAM_PHONE = "phone";
        private static final String SALESFORCE_PARAM_00N20000007w7Pg = "00N20000007w7Pg";
        private static final String SALESFORCE_PARAM_DESCRIPTION = "description";

        private String salesforceCasesUrl;
        private String salesforceLeadsUrl;
        private String salesforceOrgId;
        private String leadSource;
        private Boolean debug = false;
        private String debugEmail;

        public String getSalesforceLeadsUrl() {
            return salesforceLeadsUrl;
        }

        public void setSalesforceLeadsUrl(String salesforceLeadsUrl) {
            this.salesforceLeadsUrl = salesforceLeadsUrl;
        }

        public String getSalesforceCasesUrl() {
            return salesforceCasesUrl;
        }

        public void setSalesforceCasesUrl(String salesforceCasesUrl) {
            this.salesforceCasesUrl = salesforceCasesUrl;
        }

        public String getSalesforceOrgId() {
            return salesforceOrgId;
        }

        public void setSalesforceOrgId(String salesforceOrgId) {
            this.salesforceOrgId = salesforceOrgId;
        }

        public String getLeadSource() {
            return leadSource;
        }

        public void setLeadSource(String leadSource) {
            this.leadSource = leadSource;
        }

        public Boolean getDebug() {
            return debug;
        }

        public void setDebug(Boolean debug) {
            this.debug = debug;
        }

        public String getDebugEmail() {
            return debugEmail;
        }

        public void setDebugEmail(String debugEmail) {
            this.debugEmail = debugEmail;
        }
    }
}