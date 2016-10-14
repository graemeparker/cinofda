package com.adfonic.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Company_;
//import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationHistory;
import com.adfonic.domain.PublicationHistory_;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher_;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;
import com.adfonic.email.EmailException;
import com.adfonic.email.EmailService;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

/**
 * Encapsulation of logic for sending publication approval/rejection emails
 */
public class PublicationEmailUtils {
    private static final transient Logger LOG = Logger.getLogger(PublicationEmailUtils.class.getName());
    
    private static final FetchStrategy PUBLICATION_FS = new FetchStrategyBuilder()
        .addLeft(Publication_.assignedTo)
        .addLeft(Publication_.publicationType)
        .addLeft(Publication_.publisher)
        .addInner(Publisher_.company)
        .addLeft(Company_.accountManager)
        .addLeft(Publication_.watchers)
        .build();

    private static final FetchStrategy PUBLICATION_HISTORY_FS = new FetchStrategyBuilder()
        .addLeft(PublicationHistory_.adfonicUser)
        .addLeft(PublicationHistory_.assignedTo)
        .build();
    
    private final PublicationManager publicationManager;
    private final EmailService emailService;
    private final EmailAddressManager emailAddressManager;
    private final String tools2RootUrl;
    private final String webRootUrl;
    private final String publicationApprovalsDashboardUrl;
    private final String publicationUrl;
    private final String companyName;

    @Autowired
    public PublicationEmailUtils(PublicationManager publicationManager,
                              EmailService emailService,
                              EmailAddressManager emailAddressManager,
                              @Value("${tools2.root.url}")
                              String tools2RootUrl,
                              @Value("${web.root.url}")
                              String webRootUrl,
                              @Value("${approval.publication.dashboardUrl}")
                              String publicationApprovalsDashboardUrl,
                              @Value("${approval.publication.publicationUrl}")
                              String publicationUrl,
                              @Value("${applicationBean.companyName}")
                              String companyName)
    {
        this.publicationManager = publicationManager;
        this.emailService = emailService;
        this.emailAddressManager = emailAddressManager;
        this.tools2RootUrl = tools2RootUrl;
        this.webRootUrl = webRootUrl;
        this.publicationApprovalsDashboardUrl = publicationApprovalsDashboardUrl;
        this.publicationUrl = publicationUrl;
        this.companyName = companyName;
    }

    /**
     * Send an email to the publisher when an admin has commented on a publication
     * @throws com.adfonic.email.EmailException
     * @throws java.io.IOException
     */
    public void sendPublicationCommentEmail(Long publicationId, String comment, FacesContext fc) throws EmailException, IOException {
        // Reload the publication with the fetch strategy we know we need
        // in order to fill out our email templates
        Publication publication = publicationManager.getPublicationById(publicationId, PUBLICATION_FS);

        Map<String,Object> values = new HashMap<>();
        values.put("publication",publication);
        values.put("comment", comment);
        values.put("urlTools", tools2RootUrl);
        values.put("urlWeb", webRootUrl);

        String template = IOUtils.toString(this.getClass().getResourceAsStream("/templates/publication_comment.html"));
        String subject = companyName + " " + makeSubjectToken(publication) + " " + publication.getName();
        
        ELEmailUtils.sendEmailToUser(publication.getPublisher().getCompany().getAccountManager(),
                                     null,
                                     emailAddressManager.getEmailAddress(EmailAddressType.SUPPORT),
                                     subject,
                                     "text/html",
                                     template,
                                     values,
                                     fc.getELContext(),
                                     FacesContextHelper.getApplication(fc).getExpressionFactory(),
                                     emailService);
    }

    public void sendUpdateEmailToWatchers(Long publicationId, FacesContext fc) throws EmailException, IOException {
        // Reload the publication with the fetch strategy we know we need
        // in order to fill out our email templates
        Publication publication = publicationManager.getPublicationById(publicationId, PUBLICATION_FS);
        
        List<String> watcherEmails = new ArrayList<String>();
        for (AdfonicUser watcher : publication.getWatchers()) {
            watcherEmails.add(watcher.getFormattedEmail());
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending update email for Publication id=" + publication.getId() + " to watchers: " + StringUtils.join(watcherEmails, ", "));
        }
        
        Map<String,Object> values = new HashMap<>();
        values.put("publication", publication);
        values.put("publicationApprovalsDashboardUrl", publicationApprovalsDashboardUrl);
        values.put("publicationUrl", publicationUrl);

        StringBuilder historyTable = new StringBuilder()
            .append("<table border=1 cellpadding=2 cellspacing=0>")
            .append("<tr>")
            .append("<th>Event Time</th>")
            .append("<th>Logged By</th>")
            .append("<th>Assigned To</th>")
            .append("<th>Approval Status</th>")
            .append("<th>AdOps Status</th>")
            .append("<th>Comment</th>")
            .append("</tr>");
        for (PublicationHistory entry : publicationManager.getPublicationHistory(publication, PUBLICATION_HISTORY_FS)) {
            historyTable.append("<tr>")
                .append("<td>").append(entry.getEventTime()).append("</td>")
                .append("<td>").append(entry.getAdfonicUser() == null ? "&nbsp;" : entry.getAdfonicUser().getFullName()).append("</td>")
                .append("<td>").append(entry.getAssignedTo() == null ? "&nbsp;" : entry.getAssignedTo().getFullName()).append("</td>")
                .append("<td>").append(entry.getStatus().name()).append("</td>")
                .append("<td>").append(entry.getAdOpsStatus() == null ? "&nbsp;" : entry.getAdOpsStatus().name()).append("</td>")
                .append("<td>").append(StringUtils.defaultIfEmpty(entry.getComment(), "&nbsp;")).append("</td>")
                .append("</tr>");
        }
        historyTable.append("</table>");
        
        values.put("historyTable", historyTable.toString());
        values.put("urlTools", tools2RootUrl);
        values.put("urlWeb", webRootUrl);
        
        String template = IOUtils.toString(this.getClass().getResourceAsStream("/templates/publication_updated_for_watchers.html"));
        String body = ELUtils.evaluateTemplate(template, values, fc.getELContext(), FacesContextHelper.getApplication(fc).getExpressionFactory());

        String subject = "Publication " + publication.getId() + " Updated";
        String fromAddress = emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY);
        
        emailService.sendEmail(fromAddress, fromAddress, watcherEmails, null, null, null, subject, body, "text/html");
    }

    /**
     * Build a token, which when inserted into an email subject, will allow us
     * to recognize the Publication if/when the recipient replies to us.
     */
    static String makeSubjectToken(Publication publication) {
        // AO-196 - it must be distinct from the way Jtrac does it.
        // Jtrac does it like:  Adfonic #PUB-12345 summary here
        // We'll do it like:  Adfonic [PUB-12345] summary here
        // See SupportEmailPoller in the tasks module for the receiving end
        return "[PUB-" + publication.getId() + "]";
    }
}
