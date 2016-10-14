package com.adfonic.sso.services;

import java.util.Map;

import com.adfonic.email.EmailAddressType;

public interface EmailService {

    public void sendEmail(EmailAddressType from,
                          String to,
                          String subject,
                          Map<String, String> values, 
                          String templatePath);
    
}
