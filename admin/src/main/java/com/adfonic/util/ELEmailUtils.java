package com.adfonic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.User;
import com.adfonic.email.EmailException;
import com.adfonic.email.EmailService;

/**
 * Utilities for sending EL template-based emails in a provider-agnostic way
 */
public final class ELEmailUtils {
    private static final transient Logger LOG = Logger.getLogger(ELEmailUtils.class.getName());
    
    private ELEmailUtils() {}

    /**
     * Send email to an advertiser.  This method will deliver the supplied email
     * template (EL) to all users associated with the advertiser.  Each email sent
     * is customized for the user receiving it...i.e. the "user" property in the
     * template will refer to the recipient user.  If the advertiser has no users
     * associated, the email will be sent to the company account manager.
     * @param advertiser the Advertiser
     * @param ccNotifyAdditionalEmails whether or not Advertiser.notifyAdditionalEmails
     * should be Cc'd on each email
     * @param fromAddress the email address from which the email will be sent
     * @param subject the email subject
     * @param contentType the Content-Type of the email body (i.e. text/html)
     * @param template the EL template content
     * @param templateProperties the properties to be used when resolving variables
     * referenced in the EL template
     * @param elContext the ELContext instance to use
     * @param expressionFactory the EL ExpressionFactory instance to use
     * @param emailService the EmailService used to send the email
     * @throws EmailException if sending the email fails
     */
    public static void sendEmailToAdvertiser(Advertiser advertiser,
                                             boolean ccNotifyAdditionalEmails,
                                             String fromAddress,
                                             String subject,
                                             String contentType,
                                             String template,
                                             Map<String,Object> templateProperties,
                                             ELContext elContext,
                                             ExpressionFactory expressionFactory,
                                             EmailService emailService)
        throws EmailException
    {
        List<User> toUsers = new ArrayList<User>();
        toUsers.addAll(advertiser.getUsers());
        if (toUsers.isEmpty()) {
            toUsers.add(advertiser.getCompany().getAccountManager());
        }

        // Make sure "advertiser" and "company" are always available
        if (!templateProperties.containsKey("advertiser")) {
            templateProperties.put("advertiser", advertiser);
        }
        if (!templateProperties.containsKey("company")) {
            templateProperties.put("company", advertiser.getCompany());
        }

        // We plan to set "user" in the template properties to each user as we send email to
        // that user.  That way each email can be personally addressed or customized. But only
        // do that if there's not already a "user" property value supplied by the caller.
        boolean setUserToEachUser = !templateProperties.containsKey("user");

        // Cc the Advertiser's notifyAdditionalEmails if instructed to do that
        List<String> ccList = null;
        if (ccNotifyAdditionalEmails && advertiser.getNotifyAdditionalEmails() != null) {
            ccList = new ArrayList<String>();
            for (String additionalEmail : StringUtils.split(advertiser.getNotifyAdditionalEmails(), ',')) {
                ccList.add(additionalEmail.trim());
            }
        }

        for (User toUser : toUsers) {
            // If the properties didn't contain "user" as supplied, make sure
            // it's defined in order to customize the content for each recipient.
            if (setUserToEachUser) {
                templateProperties.put("user", toUser);
            }

            sendEmailToUser(toUser, ccList, fromAddress, subject, contentType, template, templateProperties, elContext, expressionFactory, emailService);
            
            // This is a hack in order to avoid sending the Cc list the same
            // email for different users over and over.  Just Cc them once.
            ccList = null;
        }
    }

    /**
     * Send email to a user.  This method will deliver the supplied email
     * template (EL) to the given user and the list of Cc'd addresses, when
     * supplied.
     * @param user the recipient User
     * @param ccList optional list of email addresses to Cc
     * @param fromAddress the email address from which the email will be sent
     * @param subject the email subject
     * @param contentType the Content-Type of the email body (i.e. text/html)
     * @param template the EL template content
     * @param templateProperties the properties to be used when resolving variables
     * referenced in the EL template
     * @param elContext the ELContext instance to use
     * @param expressionFactory the EL ExpressionFactory instance to use
     * @param emailService the EmailService used to send the email
     * @throws EmailException if sending the email fails
     */
    public static void sendEmailToUser(User user, List<String> ccList, String fromAddress, String subject, String contentType, String template, Map<String,Object> templateProperties, ELContext elContext, ExpressionFactory expressionFactory, EmailService emailService) throws EmailException {
        String body = ELUtils.evaluateTemplate(template, templateProperties, elContext, expressionFactory);

        // Construct the To: address as "First Last" <email@domain.tld>
        String toAddress = user.getFormattedEmail();

        // Send the email, Cc'ing if necessary
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Sending email From: " + fromAddress + ", To: " + toAddress + ", Cc: " + ccList + ", Subject: " + subject);
        }
        emailService.sendEmail(fromAddress, fromAddress, Collections.singletonList(toAddress), ccList, null, null, subject, body, contentType);
    }
}
