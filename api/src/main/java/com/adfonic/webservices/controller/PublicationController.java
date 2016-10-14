package com.adfonic.webservices.controller;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.domain.Language;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.User;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.util.Reporting;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.PublicationFilter;

@Controller
public class PublicationController extends AbstractAdfonicWebService {
    private static final transient Logger LOG = Logger.getLogger(PublicationController.class.getName());

    private static final FetchStrategy PUBLICATION_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Publication_.publisher)
        .addLeft(Publication_.languages)
        .build();

    private static final FetchStrategy PUBLICATION_TYPE_FETCH_STRATEGY = new FetchStrategyBuilder()
        .build();

    private static final FetchStrategy TRANSPARENT_NETWORK_FETCH_STRATEGY = new FetchStrategyBuilder()
        .build();

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) throws Exception {
        webDataBinder.registerCustomEditor(PublicationType.class, new PublicationTypeEditor());
        webDataBinder.registerCustomEditor(LanguageSet.class, new LanguageSetEditor());
    }

    public static class LanguageSet {
        private final Set<Language> languages = new LinkedHashSet<Language>();
        public Set<Language> getLanguages() { return languages; }
    }

    @RequestMapping(value="/publication/create.{format}", method=RequestMethod.POST)
    public String createPublication(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    @RequestParam
                                    String name,
                                    @RequestParam
                                    PublicationType type,
                                    @RequestParam(required=false)
                                    String description,
                                    @RequestParam(required=false)
                                    String reference,
                                    @RequestParam(required=false)
                                    String url,
                                    @RequestParam(required=false)
                                    Boolean transparent,
                                    @RequestParam(required=false)
                                    LanguageSet languages,
                                    @RequestParam(required=false)
                                    Integer requests,
                                    @RequestParam(required=false)
                                    Integer uniques,
                                    @RequestParam(required=false)
                                    Boolean autoapprove,
                                    @PathVariable
                                    String format)
                    throws Exception {
        try {
            LOG.fine("createPublication() starts");
            User user = authenticate(request, format);

            Publisher publisher = user.getCompany().getPublisher();
            
            // Create the new publication
            Publication pub = new Publication(publisher);
            pub.setName(name);
            pub.setPublicationType(type);
            pub.setDescription(treatBlankAsNull(description));
            pub.setReference(treatBlankAsNull(reference));
            pub.setURLString(treatBlankAsNull(url));
            
            if (transparent != null && transparent.booleanValue()) {
                TransparentNetwork tn = getPublicationManager().newTransparentNetwork(name, treatBlankAsNull(description));
                pub.setTransparentNetwork(tn);
            }
            if (languages != null) {
                for (Language language : languages.getLanguages()) {
                    pub.getLanguages().add(language);
                }
            }
            // MAD-918 requests and users are not used anymore and the columns are deleted from PUBLICATION table
            // once we review the process of versioning we should create new service for this   
            if (autoapprove != null) {
                pub.setAutoApproval(autoapprove.booleanValue());
            }

            pub.setStatus(Publication.Status.PENDING);
            pub.setCategory(getCommonManager().getDefaultCategory());

            pub.setDisclosed(publisher.isDisclosed());
            
            //  We're about to submit to jtrac.
            pub.setSubmissionTime(new Date());
 
            LOG.fine("createPublication() persisting Publication");
            pub = getPublicationManager().create(pub);

            model.addAttribute("publication", pub);
            LOG.fine("createPublication() all done");

            return format + "PublicationView";
        } catch(Exception e) {
            LOG.severe(ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    @RequestMapping(value="/publication/{externalID}.{format}",method=RequestMethod.GET)
    public String showPublication(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Model model,
                                  @PathVariable
                                  String externalID,
                                  @PathVariable
                                  String format)
        throws Exception
    {
        User user = authenticate(request, format);
        model.addAttribute("publication", resolvePublication(externalID, user, format));
        return format + "PublicationView";
    }

    @RequestMapping(value="/publication/{externalID}.{format}",method=RequestMethod.POST)
    public String updatePublication(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    @RequestParam(required=false)
                                    String name,
                                    @RequestParam(required=false)
                                    String description,
                                    @RequestParam(required=false)
                                    String reference,
                                    @RequestParam(required=false)
                                    String url,
                                    @RequestParam(required=false)
                                    Boolean transparent,
                                    @RequestParam(required=false)
                                    LanguageSet languages,
                                    @RequestParam(required=false)
                                    Integer requests,
                                    @RequestParam(required=false)
                                    Integer uniques,
                                    @RequestParam(required=false)
                                    Boolean autoapprove,
                                    @RequestParam(required=false)
                                    Publication.Status status,
                                    @PathVariable
                                    String externalID,
                                    @PathVariable
                                    String format)
        throws Exception
    {
        User user = authenticate(request, format);
        Publication pub = resolvePublication(externalID, user, format);

        // Update the publication
        if (name != null) {
            if ("".equals(name)) {
                throw new WebServiceException(ErrorCode.INVALID_ARGUMENT, "Publication name cannot be null/blank", format);
            } else {
                pub.setName(name);
            }
        }
        if (description != null) {
            pub.setDescription(treatBlankAsNull(description));
        }
        if (reference != null) {
            pub.setReference(treatBlankAsNull(reference));
        }
        if (url != null) {
            pub.setURLString(treatBlankAsNull(url));
        }

        TransparentNetwork transparentNetwork;
        if (transparent != null
                && transparent.booleanValue() == ((transparentNetwork=pub.getTransparentNetwork()) == null)) {//means toggle
            if (transparentNetwork == null) {
                // Set it up now
                transparentNetwork = getPublicationManager().newTransparentNetwork(pub.getName(), description, TRANSPARENT_NETWORK_FETCH_STRATEGY);
            } else {
                // Remove the TN now
                getPublicationManager().delete(transparentNetwork);
                transparentNetwork = null;
            }
            pub.setTransparentNetwork(transparentNetwork);
        }

        if (languages != null) {
            pub.getLanguages().clear();
            for (Language language : languages.getLanguages()) {
                pub.getLanguages().add(language);
            }
        }
        // MAD-918 requests and users are not used anymore and the columns are deleted from PUBLICATION table
        // once we review the process of versioning we should create new service for this
        if (autoapprove != null) {
            pub.setAutoApproval(autoapprove.booleanValue());
        }
        if (status != null) {
            pub.transitionStatus(status);
        }

        pub = getPublicationManager().update(pub);

        model.addAttribute("publication", pub);
        return format + "PublicationView";
    }

    @RequestMapping(value="/publication/list.{format}",method=RequestMethod.GET)
    public String listPublications(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model,
                                   @RequestParam(required=false)
                                   Publication.Status status,
                                   @PathVariable
                                   String format)
        throws Exception
    {
        User user = authenticate(request, format);

        PublicationFilter filter = new PublicationFilter();
        filter.setPublisher(user.getCompany().getPublisher());
        if (status != null) {
            filter.setStatuses(Collections.singleton(status));
        }

        model.addAttribute("publications", getPublicationManager().getAllPublications(filter, new Sorting(SortOrder.asc("name")), PUBLICATION_FETCH_STRATEGY));
        return format + "PublicationListView";
    }

    public enum ReportingLevel {
        publication,
            adslot
            }

    @Autowired
    Reporting reporting;
    
    @RequestMapping(value="/publication/statistics.{format}",method=RequestMethod.GET)
    public String getAllReportingStatsSQL(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Model model,
                                       @RequestParam(defaultValue="publication")
                                       ReportingLevel level,
                                       @RequestParam
                                       String start,
                                       @RequestParam
                                       String end,
                                       @RequestParam(defaultValue="false")
                                       boolean bycountry,
                                       @PathVariable
                                       String format)
        throws Exception
    {
        User user = authenticate(request, format);

        // just to see if it parses alright
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = user.getCompany().getDefaultTimeZone();
        sdf.setTimeZone(tz);
        // edge case where old report used to return valid stuff when input format was not right
        start = sdf.format(sdf.parse(start));
        end = sdf.format(sdf.parse(end));

        Object result;
        switch (level) {
        case adslot:
            result = bycountry? reporting.getPubStatsGroupedByAdSpaceByCountry(user.getCompany().getPublisher().getId(), null, start, end)
                              : reporting.getPubStatsGroupedByAdSpaceLegacy(user.getCompany().getPublisher().getId(), null, start, end);
            break;
        case publication:
        default:
            result = bycountry? reporting.getPubStatsGroupedByPublicationByCountry(user.getCompany().getPublisher().getId(), null, start, end)
                              : reporting.getPubStatsGroupedByPublicationLegacy(user.getCompany().getPublisher().getId(), null, start, end);
            break;
        }

        model.addAttribute("result", result);
        model.addAttribute("unique", false);

        return format + "PublisherStatisticsView";
    }

    @RequestMapping(value="/publication/{externalID}/statistics.{format}",method=RequestMethod.GET)
    public String getPublicationReportingStatsSQL(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Model model,
                                               @RequestParam(defaultValue="publication")
                                               ReportingLevel level,
                                               @RequestParam
                                               String start,
                                               @RequestParam
                                               String end,
                                               @RequestParam(defaultValue="false") // TODO - parameter is ignored; it has never worked
                                               boolean bycountry,
                                               @PathVariable
                                               String externalID,
                                               @PathVariable
                                               String format)
        throws Exception
    {
        User user = authenticate(request, format);
        Publication pub = resolvePublication(externalID, user, format);

        // just to see if it parses alright
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = user.getCompany().getDefaultTimeZone();
        sdf.setTimeZone(tz);
        // edge case where old report used to return valid stuff when input format was not right
        start = sdf.format(sdf.parse(start));
        end = sdf.format(sdf.parse(end));
        
        Object result;
        switch (level) {
        case adslot:
            result = bycountry? reporting.getPubStatsGroupedByAdSpaceByCountry(pub.getPublisher().getId(), pub.getId(), start, end)
                              : reporting.getPubStatsGroupedByAdSpaceLegacy(pub.getPublisher().getId(), pub.getId(), start, end);
            break;
        case publication:
        default:
            result = bycountry? reporting.getPubStatsGroupedByPublicationByCountry(pub.getPublisher().getId(), pub.getId(), start, end)
                              : reporting.getPubStatsGroupedByPublicationLegacy(pub.getPublisher().getId(), pub.getId(), start, end);
            break;
        }

        model.addAttribute("result", result);
        model.addAttribute("unique", true);

        return format + "PublisherStatisticsView";
    }

    private Publication resolvePublication(String externalID, User user, String format) throws WebServiceException {
        // Look up the publication by id
        Publication pub = getPublicationManager().getPublicationByExternalId(externalID, PUBLICATION_FETCH_STRATEGY);
        if (pub == null) {
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, "No such Publication id: " + externalID, format);
        }

        // Make sure the publication belongs to them
        if (!user.getCompany().getPublisher().equals(pub.getPublisher())) {
            throw new WebServiceException(ErrorCode.NOT_OWNER, "Not authorized to update this Publication", format);
        }

        return pub;
    }

    /** Custom property editor that allows our methods to take PublicationType
        directly, letting Spring resolve it by name
    */
    private final class PublicationTypeEditor extends java.beans.PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            PublicationType pubType = getPublicationManager().getPublicationTypeBySystemName(text, PUBLICATION_TYPE_FETCH_STRATEGY);
            if (pubType == null) {
                throw new IllegalArgumentException("No such PublicationType: " + text);
            }
            setValue(pubType);
        }

        @Override
        public String getAsText() {
            PublicationType pubType = (PublicationType)getValue();
            return pubType != null ? pubType.getSystemName() : "";
        }
    }

    /** Custom property editor that allows our methods to take an array of
        Language objects, interpreted from a comma-separated list of ISO codes
    */
    private final class LanguageSetEditor extends java.beans.PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            LanguageSet languageSet = new LanguageSet();
            for (String isoCode : StringUtils.split(text, ',')) {
                Language language = getCommonManager().getLanguageByIsoCode(isoCode);
                if (language == null) {
                    throw new IllegalArgumentException("No such language: " + isoCode);
                }
                languageSet.getLanguages().add(language);
            }
            setValue(languageSet);
        }

        @Override
        public String getAsText() {
            LanguageSet languageSet = (LanguageSet)getValue();
            if (languageSet == null) {
                return null;
            }
            StringBuilder bld = new StringBuilder();
            for (Language language : languageSet.getLanguages()) {
                if (bld.length() > 0) {
                    bld.append(',');
                }
                bld.append(language.getISOCode());
            }
            return bld.toString();
        }
    }
}
