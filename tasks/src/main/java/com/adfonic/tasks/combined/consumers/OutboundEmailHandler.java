package com.adfonic.tasks.combined.consumers;

import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import com.adfonic.email.impl.JmsBasedEmailService;

@Component
public class OutboundEmailHandler {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private final JavaMailSender mailSender;

    @Autowired
    public OutboundEmailHandler(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void onOutboundEmail(Map<String, Object> msg) {
        LOG.debug("Processing {}", msg);

        // Remove the key fields from the message map
        final String body = (String) msg.remove(JmsBasedEmailService.BODY);
        final String to = (String) msg.remove(JmsBasedEmailService.TO);
        final String from = (String) msg.remove(JmsBasedEmailService.FROM);
        final String subject = (String) msg.remove(JmsBasedEmailService.SUBJECT);
        final String contentType = (String) msg.remove(JmsBasedEmailService.CONTENT_TYPE);

        // And now what's left in the message map can be used as headers
        final Map<String, Object> headers = msg;

        LOG.info("Sending email To: {}, From: {}, Subject: {}", to, from, subject);

        // Let Spring deliver the email for us
        mailSender.send(new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(from);
                message.setTo(to.split(";")); // SC-39 - handle multiple
                message.setSubject(subject);
                if ("text/html".equals(contentType)) {
                    message.setText(body, true);
                } else {
                    message.setText(body);
                }
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    // SC-39 - special handling for Cc and Bcc so we deal with
                    // multiple addresses properly
                    if ("Cc".equalsIgnoreCase(header.getKey())) {
                        message.setCc(header.getValue().toString().split(";"));
                    } else if ("Bcc".equalsIgnoreCase(header.getKey())) {
                        message.setBcc(header.getValue().toString().split(";"));
                    } else {
                        mimeMessage.setHeader(header.getKey(), header.getValue().toString());
                    }
                }
            }
        });
    }
}
