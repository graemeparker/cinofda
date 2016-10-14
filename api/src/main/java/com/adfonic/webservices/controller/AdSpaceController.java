package com.adfonic.webservices.controller;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.User;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.util.Reporting;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Controller
public class AdSpaceController extends AbstractAdfonicWebService {

    private static final FetchStrategy AD_SPACE_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addLeft(AdSpace_.publication)
        .addInner(Publication_.publisher)
        .build();

    private static final FetchStrategy PUBLICATION_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Publication_.publisher)
        .addLeft(Publication_.languages)
        .build();

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) throws Exception {
        webDataBinder.registerCustomEditor(Publication.class, new PublicationEditor());
        webDataBinder.registerCustomEditor(FormatSet.class, new FormatSetEditor());
    }

    public static class FormatSet {
        private final Set<Format> formats = new LinkedHashSet<Format>();
        public Set<Format> getFormats() { return formats; }
    }

    @RequestMapping(value="/adslot/create.{format}", method=RequestMethod.POST)
    public String createAdSpace(HttpServletRequest request,
                                HttpServletResponse response,
                                Model model,
                                @RequestParam
                                Publication publication,
                                @RequestParam
                                String name,
                                @RequestParam
                                FormatSet formats,
                                @PathVariable
                                String format)
        throws Exception
    {
        User user = authenticate(request, format);

        // Make sure the publication belongs to them
        if (!user.getCompany().getPublisher().equals(publication.getPublisher())) {
            throw new WebServiceException(ErrorCode.NOT_OWNER, "Not authorized to create ad slot under this Publication", format);
        }

        // Create the new adSpace
        AdSpace adSpace = getPublicationManager().newAdSpace(publication, name, formats.getFormats(), AD_SPACE_FETCH_STRATEGY);

        model.addAttribute("adSpace", adSpace);
        return format + "AdSpaceView";
    }

    @RequestMapping(value="/adslot/{externalID}.{format}",method=RequestMethod.GET)
    public String showAdSpace(HttpServletRequest request,
                              HttpServletResponse response,
                              Model model,
                              @PathVariable
                              String externalID,
                              @PathVariable
                              String format)
        throws Exception
    {
        User user = authenticate(request, format);
        AdSpace adSpace = resolveAdSpace(externalID, user, format);
        model.addAttribute("adSpace", adSpace);
        return format + "AdSpaceView";
    }

    @RequestMapping(value="/adslot/{externalID}.{format}",method=RequestMethod.POST)
    public String updateAdSpace(HttpServletRequest request,
                                HttpServletResponse response,
                                Model model,
                                @RequestParam
                                String name,
                                @RequestParam(required=false)
                                FormatSet formats,
                                @PathVariable
                                String externalID,
                                @PathVariable
                                String format)
        throws Exception
    {
        User user = authenticate(request, format);
        AdSpace adSpace = resolveAdSpace(externalID, user, format);

        // Update the adSpace
        if (name != null) {
            if ("".equals(name)) {
                throw new WebServiceException(ErrorCode.INVALID_ARGUMENT, "AdSpace name cannot be null/blank", format);
            } else {
                adSpace.setName(name);
            }
        }
        if (formats != null) {
            adSpace.getFormats().clear();
            for (Format fmt : formats.getFormats()) {
                adSpace.getFormats().add(fmt);
            }
        }
        adSpace = getPublicationManager().update(adSpace);
        adSpace = getPublicationManager().getAdSpaceById(adSpace.getId(), AD_SPACE_FETCH_STRATEGY);

        model.addAttribute("adSpace", adSpace);
        return format + "AdSpaceView";
    }

    @RequestMapping(value="/adslot/list.{format}",method=RequestMethod.GET)
    public String listAdSpaces(HttpServletRequest request,
                               HttpServletResponse response,
                               Model model,
                               @RequestParam(required=false)
                               Publication publication,
                               @PathVariable
                               String format)
        throws Exception
    {
        User user = authenticate(request, format);

        if (publication != null) {
            // Make sure the publication belongs to them
            if (!user.getCompany().getPublisher().equals(publication.getPublisher())) {
                throw new WebServiceException(ErrorCode.NOT_OWNER, "Not authorized to view ad slots under this Publication", format);
            }
            model.addAttribute("adSpaces", getPublicationManager().getAllAdSpacesForPublication(publication, new Sorting(SortOrder.asc("name")), AD_SPACE_FETCH_STRATEGY));
        } else {
            model.addAttribute("adSpaces", getPublicationManager().getAllAdSpacesForPublisher(user.getCompany().getPublisher(), new Sorting(SortOrder.asc("name")), AD_SPACE_FETCH_STRATEGY));
        }
        return format + "AdSpaceListView";
    }


    @Autowired
    Reporting reporting;
    
    @RequestMapping(value="/adslot/{externalID}/statistics.{format}",method=RequestMethod.GET)
    public String getAdSpaceReportingStatsSQL(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Model model,
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
        AdSpace adSpace = resolveAdSpace(externalID, user, format);

        // It is publisher time anyway - just see if it parses
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = user.getCompany().getDefaultTimeZone();
        sdf.setTimeZone(tz);
        // edge case where old report used to return valid stuff when input format was not right
        start = sdf.format(sdf.parse(start));
        end = sdf.format(sdf.parse(end));

        Object result = bycountry? reporting.getAdSpaceStatsByCountry(adSpace.getId(), start, end)
                                 : reporting.getAdSpaceStatsLegacy(adSpace.getId(), start, end);
        
        model.addAttribute("result", result);
        model.addAttribute("unique", true);

        return format + "PublisherStatisticsView";
    }

    private AdSpace resolveAdSpace(String externalID, User user, String format) throws WebServiceException {
        // Look up the adSpace by id
        AdSpace adSpace = getPublicationManager().getAdSpaceByExternalId(externalID, AD_SPACE_FETCH_STRATEGY);
        if (adSpace == null) {
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, "No such AdSpace id: " + externalID, format);
        }

        // Make sure the adSpace belongs to them
        if (!user.getCompany().getPublisher().equals(adSpace.getPublication().getPublisher())) {
            throw new WebServiceException(ErrorCode.NOT_OWNER, "Not authorized to see this AdSpace", format);
        }

        return adSpace;
    }

    /** Custom property editor that allows our methods to take Publication
        directly, letting Spring resolve it by externalID
    */
    private final class PublicationEditor extends java.beans.PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            Publication pub = getPublicationManager().getPublicationByExternalId(text, PUBLICATION_FETCH_STRATEGY);
            if (pub == null) {
                throw new IllegalArgumentException("No such Publication: " + text);
            }
            setValue(pub);
        }

        @Override
        public String getAsText() {
            Publication pub = (Publication)getValue();
            return pub != null ? pub.getExternalID() : "";
        }
    }

    /** Custom property editor that allows our methods to take Format directly,
        letting Spring resolve it by system name
    */
    private final class FormatSetEditor extends java.beans.PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (text == null) {
                return;
            }
            FormatSet formatSet = new FormatSet();
            for (String systemName : text.split(",")) {
                Format format = getCommonManager().getFormatBySystemName(systemName);
                if (format == null) {
                    throw new IllegalArgumentException("No such format: " + systemName);
                }
                formatSet.getFormats().add(format);
            }
            setValue(formatSet);
        }

        @Override
        public String getAsText() {
            FormatSet formatSet = (FormatSet)getValue();
            if (formatSet == null) {
                return null;
            }
            StringBuilder bld = new StringBuilder();
            for (Format format : formatSet.getFormats()) {
                if (bld.length() > 0) {
                    bld.append(',');
                }
                bld.append(format.getSystemName());
            }
            return bld.toString();
        }
    }
}
