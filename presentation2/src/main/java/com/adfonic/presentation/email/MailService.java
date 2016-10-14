package com.adfonic.presentation.email;

public interface MailService {
    
    void sendSalesMail(
            String email, 
            String name, 
            String country, 
            String phoneNumber,
            String description)  throws Exception;    
}
