package com.adfonic.email.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.jms.JmsUtils;

public class JmsBasedEmailService extends AbstractEmailService {
    /**
     * This key lets consumers know how to extract the body from the map
     * that gets sent as the JMS message.  The consumer on the other end
     * use this to remove the body from the Map.  Subsequently, the consumer
     * can simply send the body and headers (what's left in the Map after
     * removing the body) to a Camel Mail endpoint.
     */
    public static final String BODY = "__body";

    // These are exposed in case a consumer on the other end needs them
    public static final String FROM = "From";
    public static final String REPLY_TO = "Reply-To";
    public static final String TO = "To";
    public static final String CC = "Cc";
    public static final String BCC = "Bcc";
    public static final String SUBJECT = "Subject";
    public static final String CONTENT_TYPE = "Content-Type";

    private final JmsTemplate jmsTemplate;
    private final JmsUtils jmsUtils;
    private Queue outboundEmailQueue;

    @Autowired
    public JmsBasedEmailService(ConnectionFactory connectionFactory) {
        this(new JmsTemplate(connectionFactory), new JmsUtils());
    }

    JmsBasedEmailService(JmsTemplate jmsTemplate, JmsUtils jmsUtils) {
        this.jmsTemplate = jmsTemplate;
        this.jmsUtils = jmsUtils;
    }
    
    @Required
    public void setOutboundEmailQueue(Queue outboundEmailQueue) {
        this.outboundEmailQueue = outboundEmailQueue;
    }

    @Override
    public void sendEmail(String from,
                          String replyTo,
                          List<String> to,
                          List<String> cc,
                          List<String> bcc,
                          Map<String,String> mimeMessageHeaders,
                          String subject,
                          String body,
                          String contentType) throws com.adfonic.email.EmailException {
        // As of September 17, 2011, we're using Camel Mail to deliver outbound
        // email, instead of directly using the javamail API.  So what we do here
        // is simply construct the headers Map that can be handed over to the
        // Camel Mail endpoint.  http://camel.apache.org/mail.html
        // NOTE: HashMap instead of Map, tight coupling since it's Serializable
        final Map<String,Object> msg = new HashMap<String,Object>();
        msg.put(FROM, from);
        if (StringUtils.isNotEmpty(replyTo)) {
            msg.put(REPLY_TO, replyTo);
        }
        if (CollectionUtils.isNotEmpty(to)) {
            msg.put(TO, StringUtils.join(to, ';'));
        }
        if (CollectionUtils.isNotEmpty(cc)) {
            msg.put(CC, StringUtils.join(cc, ';'));
        }
        if (CollectionUtils.isNotEmpty(bcc)) {
            msg.put(BCC, StringUtils.join(bcc, ';'));
        }
        if (StringUtils.isNotEmpty(subject)) {
            msg.put(SUBJECT, subject);
        }
        if (StringUtils.isNotEmpty(contentType)) {
            msg.put(CONTENT_TYPE, contentType);
        }
        if (mimeMessageHeaders != null) {
            msg.putAll(mimeMessageHeaders);
        }

        // Since JMS doesn't propagate headers properly, it only passes
        // the body, we're sending the whole map itself as the JMS message.
        // So we need to store the body in the map...for now...the consumer
        // on the other end can simply remove the body, and then use the
        // remaining map content as the headers when invoking a Camel Mail
        // endpoint.
        msg.put(BODY, body);

        // The consumer will need to be set up to expect separate body and headers
        jmsUtils.sendObject(jmsTemplate, outboundEmailQueue, (HashMap<String,Object>) msg);
    }
}
