package com.adfonic.email.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.adfonic.email.EmailService;

public abstract class AbstractEmailService implements EmailService {
    @Override
    public void sendEmail(String from,
                          String to,
                          String subject,
                          String body,
                          String contentType) throws com.adfonic.email.EmailException {
        sendEmail(from,
                  from, // replyTo
                  Arrays.asList(new String[] { to }),
                  null, // cc
                  null, // bcc
                  null, // headers
                  subject,
                  body,
                  contentType);
    }
    
    @Override
    public abstract void sendEmail(String from,
                                   String replyTo,
                                   List<String> to,
                                   List<String> cc,
                                   List<String> bcc,
                                   Map<String,String> headers,
                                   String subject,
                                   String body,
                                   String contentType)
        throws com.adfonic.email.EmailException;
}
