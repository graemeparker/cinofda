package com.adfonic.tasks.combined;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.el.ExpressionFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.Advertiser;
import com.adfonic.email.EmailService;
import com.adfonic.util.ELUtils;

import de.odysseus.el.util.SimpleContext;

public abstract class TemplatedEmailSendingTask {
    @Autowired
    private EmailService emailService;
    @Autowired
    private ExpressionFactory expressionFactory;
    @Value("${mail.address.NOREPLY}")
    private String from;
    @Value("${tools2.url}")
    private String urlRoot;

    protected static String loadResource(String templateLocation) throws java.io.IOException {
        InputStream inputStream = TemplatedEmailSendingTask.class.getResourceAsStream(templateLocation);
        try {
            return IOUtils.toString(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
	
    public void sendEmail(String to, String ccString, String subject, String templateText, Map<String,Object> params) throws com.adfonic.email.EmailException {
        List<String> toList = new ArrayList<String>();
        toList.add(to);
        sendEmail(toList, ccString, subject, templateText, params);
    }
    
    public void sendEmail(List<String> toList, String ccString, String subject, String templateText, Map<String,Object> params) throws com.adfonic.email.EmailException {
        List<String> ccList = null;
        if (StringUtils.isNotBlank(ccString)) {
            ccList = Arrays.asList(ccString.trim().split(","));
        }
        sendEmail(toList, ccList, subject, templateText, params);
    }
    
    protected void sendEmail(List<String> toList, List<String> ccList, String subject, String templateText, Map<String,Object> params) throws com.adfonic.email.EmailException {
        // Make sure all templates have ${urlRoot} available
        params.put("urlRoot", urlRoot);
        emailService.sendEmail(from,
                               from, // replyTo
                               toList,
                               ccList,
                               null, // bcc
                               null, // headers
                               subject,
                               ELUtils.evaluateTemplate(templateText, params, new SimpleContext(), expressionFactory),
                               "text/html");
    }

    protected void sendEmailToAdvertiser(Advertiser advertiser, String subject, String templateText, Map<String,Object> params) throws com.adfonic.email.EmailException {
        // Make sure all templates have ${urlRoot} available
        params.put("urlRoot", urlRoot);
        ELEmailUtils.sendEmailToAdvertiser(advertiser,
                                           true, // ccNotifyAdditionalEmails
                                           from,
                                           subject,
                                           "text/html",
                                           templateText,
                                           params,
                                           new SimpleContext(),
                                           expressionFactory,
                                           emailService);
    }
}
