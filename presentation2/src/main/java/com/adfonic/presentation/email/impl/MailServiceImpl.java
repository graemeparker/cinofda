package com.adfonic.presentation.email.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.email.EmailService;
import com.adfonic.presentation.email.MailService;

@Service("mailService")
public class MailServiceImpl implements MailService{
	@Autowired
	protected EmailService emailService;
	
	private String salesMail;
	private String supportMail;
	
	public void sendSalesMail(String email, String name, String country, String phoneNumber, String description) throws Exception{
	    emailService.sendEmail("customers@adfonic.com", salesMail, "test", email + name + country + phoneNumber + description, "text/html");
	}

    public String getSalesMail() {
        return salesMail;
    }

    public void setSalesMail(String salesMail) {
        this.salesMail = salesMail;
    }

    public String getSupportMail() {
        return supportMail;
    }

    public void setSupportMail(String supportMail) {
        this.supportMail = supportMail;
    }
}

