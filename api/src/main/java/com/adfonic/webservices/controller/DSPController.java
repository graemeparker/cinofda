package com.adfonic.webservices.controller;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Audience;
import com.adfonic.domain.Company_;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPVendor;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.Publication.Status;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.AudienceDTO;
import com.adfonic.webservices.dto.AudienceSegmentDTO;
import com.adfonic.webservices.dto.AudienceVendorDTO;
import com.adfonic.webservices.exception.AuthorizationException;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.util.DSPetc;
import com.adfonic.webservices.util.DSPetc.DSP_PROFILE;
import com.adfonic.webservices.view.dsp.PublicationStreamingList;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.PublicationFilter;

/**
 * Catch-all for controller methods (mostly custom) provided as part of DSP plans
 *  Expand/specialize in future as reqd 
 *
 */
@Controller
public class DSPController extends AbstractAdfonicWebService{

    private static final transient Logger LOG = Logger.getLogger(DSPController.class.getName());

    // Fetch-strategy for use by Voltari. Reuse for anyone who require similar details
    private static final FetchStrategy PUB_SUMMARY_FS_FORIAB = new FetchStrategyBuilder()
    .addInner(Publication_.publisher)
    .addInner(Publication_.category)
    .build();

    private DSP_PROFILE getDspProfile(User user) {
        if (user.getCompany().isAccountType(AccountType.PUBLISHER)) {
            throw new AuthorizationException();
        }

        return DSP_PROFILE.getByNameOrRole(getUserManager().getUserById(user.getId(), 
                                                                        new FetchStrategyBuilder()
                                                                                .addInner(User_.company)
                                                                                .addLeft(Company_.roles)
                                                                                .build()).getCompany());
    }
    
    @Value("${publist.fetch.size:5000}")
    private int pageSize;

    @RequestMapping(value="/madison/publications/list.{format}", method=RequestMethod.GET)
    public String getAllActiveRtbPublications(HttpServletRequest request,
            Model model,
            @RequestParam(required = false)
            final String since,
            @PathVariable
            String format) throws WebServiceException, Exception{
        
        User user = authenticate(request, format);
        DSP_PROFILE profile = getDspProfile(user);
        
        if (profile == DSP_PROFILE._) {// publication listing only for matching profiles
            throw new AuthorizationException();
        }
        
        PublicationFilter filter = new PublicationFilter()
                                            .setStatuses(Publication.Status.ACTIVE);
        
        if (profile.isRtbOnly) {
            filter.setRtbIdNotNull(true);
        }
        
        if (since != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(user.getCompany().getDefaultTimeZone());
            filter.setGreaterThanOrEqualToApprovalDate(sdf.parse(since));
        }
        
        PublicationStreamingList publicationStreamingList
                                = new PublicationStreamingList(getPublicationManager(), 
                                                                        pageSize, 
                                                                        PUB_SUMMARY_FS_FORIAB, 
                                                                        filter);

        model.addAttribute("publications", publicationStreamingList);
        return profile.viewPrefix(format) + DSPetc.PUB_LIST_VIEW_SFX;
    }
    
    
    @RequestMapping(value="/madison/publication/{externalID}.{format}", method=RequestMethod.GET)
    @Transactional(readOnly=true, propagation=Propagation.REQUIRED)
    public String getPublicationByExternalId(HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            @PathVariable
            String externalID,
            @PathVariable
            String format) throws WebServiceException{
        
        User user = authenticate(request, format);
        DSP_PROFILE profile=getDspProfile(user);
        
        // Look up the publication by id
        Publication pub = getPublicationManager().getPublicationByExternalId(externalID, PUB_SUMMARY_FS_FORIAB);
        if (pub == null || pub.getStatus() != Status.ACTIVE) { // should not differentiate b/n 
                                                                    // inactive and non-existent for user
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, 
                                            "No such Publication id: " + externalID, 
                                            format);
        }

        model.addAttribute("publication", pub);
        return profile.viewPrefix(format) + DSPetc.PUB_VIEW_SFX;
    }

    @RequestMapping(value = "/audience/{AudienceID}.{format}", method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AudienceDTO getAudienceById(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Model model, 
                                       @PathVariable("AudienceID") 
                                       String audienceExternalId, 
                                       @PathVariable 
                                       String format) throws WebServiceException {

        User user = authenticate(request, format);
        DSP_PROFILE profile = getDspProfile(user);
        
        if (profile != DSP_PROFILE.WEVE) {// only for Weve now
            throw new AuthorizationException();
        }

        Audience audience = getAudienceManager().getAudienceByExternalId(audienceExternalId);

        if (audience == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Audience not found");

        }

        AudienceDTO aud = new AudienceDTO();
        aud.setId(audience.getExternalID());
        aud.setName(audience.getName());

        DMPAudience dmpAudience = audience.getDmpAudience();
        if (dmpAudience != null) {
            AudienceVendorDTO audienceVendor = new AudienceVendorDTO();
            aud.setVendors(new HashSet<AudienceVendorDTO>());
            aud.getVendors().add(audienceVendor);
            DMPVendor dmpVendor = dmpAudience.getDmpVendor();
            audienceVendor.setName(dmpVendor.getName());
            Set<AudienceSegmentDTO> audienceSegments = new HashSet<AudienceSegmentDTO>();
            for (DMPSelector dmpSelector : dmpAudience.getDmpSelectors()) {
                AudienceSegmentDTO audienceSegment = new AudienceSegmentDTO();
                audienceSegment.setVendor(dmpVendor.getName());
                audienceSegment.setId(dmpSelector.getExternalID());
                audienceSegments.add(audienceSegment);
            }

            audienceVendor.setSegment(audienceSegments);

        }
        
        
        /*
         * requirement assuch for ref
         * 
        aud.setId("000000-00000-00000-00000");
        aud.setName("my audi");
        AudienceVendor av=new AudienceVendor();
        av.setName("Weve");
        av.setSegment(new HashSet<AudienceSegmentDTO>());
        av.getSegment().add(new AudienceSegmentDTO());
        av.getSegment().iterator().next().setVendor("weve");
        av.getSegment().iterator().next().setId("[weve's ID for the segment]");
        aud.setVendors(new HashSet<AudienceVendor>());
        aud.getVendors().add(av);
        */

        return aud;
    }

}
