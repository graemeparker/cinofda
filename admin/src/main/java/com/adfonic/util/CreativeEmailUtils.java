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

import com.adfonic.domain.AccountType;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.CreativeHistory_;
import com.adfonic.domain.Creative_;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;
import com.adfonic.email.EmailException;
import com.adfonic.email.EmailService;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

/**
 * Encapsulation of logic for sending creative approval/rejection emails
 */
public class CreativeEmailUtils {
    private static final transient Logger LOG = Logger.getLogger(CreativeEmailUtils.class.getName());
    
    private static final FetchStrategy CREATIVE_FS = new FetchStrategyBuilder()
        .addLeft(Creative_.assignedTo)
        .addLeft(Creative_.campaign)
        .addLeft(Campaign_.timePeriods)
        .addInner(Campaign_.advertiser)
        .addInner(Campaign_.category)
        .addInner(Advertiser_.company)
        .addLeft(Advertiser_.users)
        .addLeft(Company_.accountManager)
        .build();

    private static final FetchStrategy CAMPAIGN_FS = new FetchStrategyBuilder()
        .addLeft(Campaign_.creatives)
        .build();

    private static final FetchStrategy CAMPAIGN_WATCHERS_FS = new FetchStrategyBuilder()
        .addLeft(Campaign_.watchers)
        .build();

    private static final FetchStrategy CREATIVE_HISTORY_FS = new FetchStrategyBuilder()
        .addLeft(CreativeHistory_.adfonicUser)
        .addLeft(CreativeHistory_.assignedTo)
        .build();
    
    private final CreativeManager creativeManager;
    private final CampaignManager campaignManager;
    private final EmailService emailService;
    private final EmailAddressManager emailAddressManager;
    private final String tools2RootUrl;
    private final String webRootUrl;
    private final String creativeApprovalsDashboardUrl;
    private final String creativeUrl;

    @Autowired
    public CreativeEmailUtils(CreativeManager creativeManager,
                              CampaignManager campaignManager,
                              EmailService emailService,
                              EmailAddressManager emailAddressManager,
                              @Value("${tools2.root.url}")
                              String tools2RootUrl,
                              @Value("${web.root.url}")
                              String webRootUrl,
                              @Value("${approval.creative.dashboardUrl}")
                              String creativeApprovalsDashboardUrl,
                              @Value("${approval.creative.creativeUrl}")
                              String creativeUrl)
    {
        this.creativeManager = creativeManager;
        this.campaignManager = campaignManager;
        this.emailService = emailService;
        this.emailAddressManager = emailAddressManager;
        this.tools2RootUrl = tools2RootUrl;
        this.webRootUrl = webRootUrl;
        this.creativeApprovalsDashboardUrl = creativeApprovalsDashboardUrl;
        this.creativeUrl = creativeUrl;
    }

    /**
     * Send an email to the advertiser when a creative has been approved
     * @throws com.adfonic.email.EmailException
     * @throws java.io.IOException
     */
    public void sendCreativeApprovalEmail(Creative creative, boolean update, FacesContext fc) throws EmailException, IOException {
        // Reload the creative with the fetch strategy we know we need
        // in order to fill out our email templates
        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FS);

        Map<String,Object> values = new HashMap<>();
        values.put("creative",creative);
        values.put("urlTools", tools2RootUrl);
        values.put("urlWeb", webRootUrl);

        // Far as I know, there's no easy way to iterate in the template,
        // so we build this up manually
        Campaign campaign = campaignManager.getCampaignById(creative.getCampaign().getId(), CAMPAIGN_FS);
        List<Creative> creatives = campaign.getCreatives();

        StringBuilder htmlListBuf = new StringBuilder("<ul>");
        for (int i=0 ; i < creatives.size(); i++){
            Creative cre = creatives.get(i);
            htmlListBuf.append("<li>Creative ").append(i + 1)
                .append(" [")
                .append(cre.getName()).append("]: ")
                .append(cre.getStatus())
                .append("</li>\n");
        }
        htmlListBuf.append("</ul>");
        values.put("creativeListHtml",htmlListBuf.toString());
        String template;
        String templatePath;
        String subject;
        if (!update) {
            if (creative.getCampaign().isHouseAd()) {
                templatePath = "/templates/housead_creative_approved.html";
            }
            else if (creative.getCampaign().getAdvertiser().getCompany().isAccountType(AccountType.AGENCY)) {
                templatePath = "/templates/agency_creative_approved.html";
            }
            else {
                templatePath = "/templates/creative_approved.html";
            }
            template = IOUtils.toString(this.getClass().getResourceAsStream(templatePath));
            subject = "Your creative " + creative.getName() + " has been approved";
        } else {
            if (creative.getCampaign().isHouseAd()) {
                templatePath = "/templates/housead_updated_creative_approved.html";
            }
            else if (creative.getCampaign().getAdvertiser().getCompany().isAccountType(AccountType.AGENCY)) {
                templatePath = "/templates/agency_updated_creative_approved.html";
            }
            else {
                templatePath = "/templates/updated_creative_approved.html";
            }
            template = IOUtils.toString(this.getClass().getResourceAsStream(templatePath));
            subject = "Your updated creative " + creative.getName() + " has been approved";
        }

        subject += " " + makeSubjectToken(creative);
        
        ELEmailUtils.sendEmailToAdvertiser(creative.getCampaign().getAdvertiser(),
                                           false, // don't Cc Advertiser.notifyEmails (this wasn't done originally, at least)
                                           emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY),
                                           subject,
                                           "text/html",
                                           template,
                                           values,
                                           fc.getELContext(),
                                           FacesContextHelper.getApplication(fc).getExpressionFactory(),
                                           emailService);
    }

    /**
     * Send an email to the advertiser when a creative has been rejected
     * @throws com.adfonic.email.EmailException
     * @throws java.io.IOException
     */
    public void sendCreativeRejectionEmail(Creative creative, String comment, FacesContext fc) throws EmailException, IOException {
        // Reload the creative with the fetch strategy we know we need
        // in order to fill out our email templates
        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FS);
        
        Map<String,Object> values = new HashMap<>();
        values.put("creative",creative);
        values.put("comment", comment);
        values.put("reason", comment); // TODO: remove this, it's not used in any templates
        values.put("urlTools", tools2RootUrl);
        values.put("urlWeb", webRootUrl);
        Campaign campaign = campaignManager.getCampaignById(creative.getCampaign().getId(), CAMPAIGN_FS);
        List<Creative> creatives = campaign.getCreatives();
        StringBuilder htmlListBuf = new StringBuilder("<ul>");
        for (int i=0 ; i < creatives.size(); i++){
            Creative cre = creatives.get(i);
            htmlListBuf.append("<li>Creative ").append(i + 1)
                .append(" [")
                .append(cre.getName()).append("]: ")
                .append(cre.getStatus())
                .append("</li>\n");
        }
        htmlListBuf.append("</ul>");
        values.put("creativeListHtml",htmlListBuf.toString());

        String templatePath;
        if (creative.getCampaign().isHouseAd()) {
            templatePath = "/templates/housead_creative_rejected.html";
        }
        else if (creative.getCampaign().getAdvertiser().getCompany().isAccountType(AccountType.AGENCY)) {
            templatePath = "/templates/agency_creative_rejected.html";
        }
        else {
            templatePath = "/templates/creative_rejected.html";
        }

        ELEmailUtils.sendEmailToAdvertiser(creative.getCampaign().getAdvertiser(),
                                           false, // don't Cc Advertiser.notifyEmails (this wasn't done originally, at least)
                                           emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY),
                                           "Your creative " + creative.getName() + " has been rejected [" + creative.getExternalID() + "]",
                                           "text/html",
                                           IOUtils.toString(this.getClass().getResourceAsStream(templatePath)),
                                           values,
                                           fc.getELContext(),
                                           FacesContextHelper.getApplication(fc).getExpressionFactory(),
                                           emailService);
    }

    /**
     * Send an email to the advertiser when an admin has commented on a creative
     * @throws com.adfonic.email.EmailException
     * @throws java.io.IOException
     */
    public void sendCreativeCommentEmail(Creative creative, String comment, FacesContext fc) throws EmailException, IOException {
        // Reload the creative with the fetch strategy we know we need
        // in order to fill out our email templates
        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FS);

        Map<String,Object> values = new HashMap<>();
        values.put("creative",creative);
        values.put("urlTools", tools2RootUrl);
        values.put("urlWeb", webRootUrl);
        values.put("comment", comment);

        String template = IOUtils.toString(this.getClass().getResourceAsStream("/templates/creative_comment.html"));
        String subject = "Adfonic " + makeSubjectToken(creative) + " " + creative.getName();
        
        ELEmailUtils.sendEmailToUser(creative.getCampaign().getAdvertiser().getCompany().getAccountManager(),
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

    public void sendUpdateEmailToWatchers(Creative creative, FacesContext fc) throws EmailException, IOException {
        Campaign campaign = campaignManager.getCampaignById(creative.getCampaign().getId(), CAMPAIGN_WATCHERS_FS);
        List<String> watcherEmails = new ArrayList<String>();
        for (AdfonicUser watcher : campaign.getWatchers()) {
            watcherEmails.add(watcher.getFormattedEmail());
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending update email for Creative id=" + creative.getId() + " to watchers: " + StringUtils.join(watcherEmails, ", "));
        }
        
        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FS);
        
        Map<String,Object> values = new HashMap<>();
        values.put("creative", creative);
        values.put("creativeApprovalsDashboardUrl", creativeApprovalsDashboardUrl);
        values.put("creativeUrl", creativeUrl);
        values.put("urlWeb", webRootUrl);

        StringBuilder historyTable = new StringBuilder()
            .append("<table border=1 cellpadding=2 cellspacing=0>")
            .append("<tr>")
            .append("<th>Event Time</th>")
            .append("<th>Logged By</th>")
            .append("<th>Assigned To</th>")
            .append("<th>Approval Status</th>")
            .append("<th>Comment</th>")
            .append("</tr>");
        for (CreativeHistory entry : creativeManager.getCreativeHistory(creative, CREATIVE_HISTORY_FS)) {
            historyTable.append("<tr>")
                .append("<td>").append(entry.getEventTime()).append("</td>")
                .append("<td>").append(entry.getAdfonicUser() == null ? "&nbsp;" : entry.getAdfonicUser().getFullName()).append("</td>")
                .append("<td>").append(entry.getAssignedTo() == null ? "&nbsp;" : entry.getAssignedTo().getFullName()).append("</td>")
                .append("<td>").append(entry.getStatus().name()).append("</td>")
                .append("<td>").append(StringUtils.defaultIfEmpty(entry.getComment(), "&nbsp;")).append("</td>")
                .append("</tr>");
        }
        historyTable.append("</table>");
        
        values.put("historyTable", historyTable.toString());
        
        String template = IOUtils.toString(this.getClass().getResourceAsStream("/templates/creative_updated_for_watchers.html"));
        String body = ELUtils.evaluateTemplate(template, values, fc.getELContext(), FacesContextHelper.getApplication(fc).getExpressionFactory());

        String subject = "Creative " + creative.getId() + " Updated";
        String fromAddress = emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY);
        
        emailService.sendEmail(fromAddress, fromAddress, watcherEmails, null, null, null, subject, body, "text/html");
    }

    /**
     * Build a token, which when inserted into an email subject, will allow us
     * to recognize the Creative if/when the recipient replies to us.
     */
    static String makeSubjectToken(Creative creative) {
        // AO-196 - it must be distinct from the way Jtrac does it.
        // Jtrac does it like:  Adfonic #CRE-12345 summary here
        // We'll do it like:  Adfonic [CRE-12345] summary here
        // See SupportEmailPoller in the tasks module for the receiving end
        return "[CRE-" + creative.getId() + "]";
    }
}
