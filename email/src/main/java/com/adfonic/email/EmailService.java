package com.adfonic.email;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendEmail(String from,
                   String to,
                   String subject,
                   String body,
                   String contentType) throws com.adfonic.email.EmailException;
    
    void sendEmail(String from,
                   String replyTo,
                   List<String> to,
                   List<String> cc,
                   List<String> bcc,
                   Map<String, String> headers,
                   String subject,
                   String body,
                   String contentType)  throws com.adfonic.email.EmailException;
}
