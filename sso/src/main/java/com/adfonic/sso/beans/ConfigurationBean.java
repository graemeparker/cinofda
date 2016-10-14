package com.adfonic.sso.beans;

import org.springframework.beans.factory.annotation.Value;

public class ConfigurationBean {
    
    @Value ("${navigationBean.tools2BaseUrl}")
    private String tools2BaseUrl;
    
    @Value ("${navigationBean.customerSupportLink}")
    private String customerSupportLink;
    
    @Value ("${navigationBean.salesSupportLink}")
    private String salesSupportLink;
    
    @Value ("${navigationBean.wordpressBaseUrl}")
    private String wordpressBaseUrl;
    
    @Value ("${navigationBean.termsCondsLink}")
    private String termsCondsLink;
    
    @Value ("${navigationBean.privacyPolicyLink}")
    private String privacyPolicyLink;
    
    @Value ("${navigationBean.endUserPrivacyPolicyLink}")
    private String endUserPrivacyPolicyLink;
    
    @Value ("${navigationBean.developerBaseUrl}")
    private String developerBaseUrl;
    
    @Value ("${recaptcha.publickey}")
    private String recaptchaPublickey;
    
    @Value ("${recaptcha.privatekey}")
    private String recaptchaPrivatekey;
    
    @Value ("${sso.companyName}")
    private String companyName;
    
    @Value ("${sso.companyLegalName}")
    private String companyLegalName;
    
    public String getTools2BaseUrl() {
        return tools2BaseUrl;
    }

    public void setTools2BaseUrl(String tools2BaseUrl) {
        this.tools2BaseUrl = tools2BaseUrl;
    }

    public String getCustomerSupportLink() {
        return customerSupportLink;
    }

    public void setCustomerSupportLink(String customerSupportLink) {
        this.customerSupportLink = customerSupportLink;
    }

    public String getSalesSupportLink() {
        return salesSupportLink;
    }

    public void setSalesSupportLink(String salesSupportLink) {
        this.salesSupportLink = salesSupportLink;
    }

    public String getWordpressBaseUrl() {
        return wordpressBaseUrl;
    }

    public void setWordpressBaseUrl(String wordpressBaseUrl) {
        this.wordpressBaseUrl = wordpressBaseUrl;
    }

    public String getTermsCondsLink() {
        return termsCondsLink;
    }

    public void setTermsCondsLink(String termsCondsLink) {
        this.termsCondsLink = termsCondsLink;
    }

    public String getPrivacyPolicyLink() {
        return privacyPolicyLink;
    }

    public void setPrivacyPolicyLink(String privacyPolicyLink) {
        this.privacyPolicyLink = privacyPolicyLink;
    }

    public String getEndUserPrivacyPolicyLink() {
        return endUserPrivacyPolicyLink;
    }

    public void setEndUserPrivacyPolicyLink(String endUserPrivacyPolicyLink) {
        this.endUserPrivacyPolicyLink = endUserPrivacyPolicyLink;
    }

    public String getDeveloperBaseUrl() {
        return developerBaseUrl;
    }

    public void setDeveloperBaseUrl(String developerBaseUrl) {
        this.developerBaseUrl = developerBaseUrl;
    }

    public String getRecaptchaPublickey() {
        return recaptchaPublickey;
    }

    public void setRecaptchaPublickey(String recaptchaPublickey) {
        this.recaptchaPublickey = recaptchaPublickey;
    }

    public String getRecaptchaPrivatekey() {
        return recaptchaPrivatekey;
    }

    public void setRecaptchaPrivatekey(String recaptchaPrivatekey) {
        this.recaptchaPrivatekey = recaptchaPrivatekey;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }
}
