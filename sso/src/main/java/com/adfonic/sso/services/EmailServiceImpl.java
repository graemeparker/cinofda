package com.adfonic.sso.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;

public class EmailServiceImpl implements EmailService {
    
    private static final Logger LOG = Logger.getLogger(EmailServiceImpl.class.getName());
    
    @Autowired
    private EmailAddressManager emailAddrMgr;
    
    @Autowired
    protected com.adfonic.email.EmailService emailService;
    
    @Override
    public void sendEmail(EmailAddressType from,
                          String to,
                          String subject,
                          Map<String, String> values, 
                          String templatePath) {
        
         String body = templateToString(templatePath, values);

         try {
             emailService.sendEmail(emailAddrMgr.getEmailAddress(EmailAddressType.NOREPLY),
                                    to,
                                    subject,
                                    body,
                                    "text/html");
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Failed to send email \"" + subject + "\" to user email=" + to, e);
         }
    }
    
    private String templateToString(String templatePath, Map<String,String> values){
        String result = null;
        
        InputStream templateStream = getClass().getResourceAsStream(templatePath);
        
        if (templateStream!=null){
            try{
                result = Streams.asString(templateStream);
            }catch(IOException ioe){
                LOG.log(Level.SEVERE, "Failed to evaluate template: " + templatePath, ioe);
            }
            
            if (result!=null){
                for (Map.Entry<String,String> entry : values.entrySet()) {
                    result = result.replaceAll("\\$\\{"+entry.getKey()+"\\}", entry.getValue());
                }
            }
        }
        
        return result;
    }
}
