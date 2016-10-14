package com.adfonic.email;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

public class EmailAddressManager {
    private Properties addresses;

    @Required
    public void setAddresses(Properties addresses) {
        this.addresses = addresses;
        
        // Ensure that all address types have been configured
        for (EmailAddressType emailAddressType : EmailAddressType.values()) {
            if (!addresses.containsKey(emailAddressType.name())) {
                throw new RuntimeException("Email address not configured: " +
                                           emailAddressType.name());
            }
        }
    }
    
    public String getEmailAddress(EmailAddressType emailAddressType) {
        return addresses.getProperty(emailAddressType.name());
    }
}
