package com.adfonic.webservices.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.User;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.CreativeDTO;
import com.adfonic.webservices.dto.DestinationDTO;
import com.adfonic.webservices.service.ICampaignService;
import com.adfonic.webservices.service.ICopyService;
import com.adfonic.webservices.service.ICreativeService;
import com.adfonic.webservices.service.IUtilService;

@Controller
public class CreativeController extends AbstractAdfonicWebService {

    @Autowired
    private ICreativeService creativeService;

    @Autowired
    private ICopyService<CreativeDTO, Creative> copyService;// do not need restricting copy since here we do just domain->dto

    @Autowired
    private IUtilService utilService;

    @Autowired
    private ICampaignService campaignService;

    @RequestMapping(value = { "/creative/create.{format}", "/creative.{format}" }, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public CreativeDTO createCreative(@PathVariable String format, @RequestBody CreativeDTO creativeDTO, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        Creative creative = creativeService.createMinimalCreative(user, creativeDTO.getCampaignID(), creativeDTO.getName(), creativeDTO.getFormat());

        DestinationDTO destinationDTO = creativeDTO.getDestination();
        utilService.validatePresence("destination", destinationDTO);
        creative = creativeService.setDestination(creative, destinationDTO.getType(), destinationDTO.getData());

        creative = creativeService.copyCustomAttributes(creative, creativeDTO.getEnglishTranslation(), null);// TODO - clean up the interface

        // TODO: remove this temporary hack once advertisers are forced to
        // select device identifier types themselves.
        Campaign campaign = campaignService.findbyExternalID(user, creativeDTO.getCampaignID());
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, getCampaignManager(), getDeviceManager());

        return (copyService.copyFromDomain(creative, CreativeDTO.class));
    }


    @RequestMapping(value = "/creative/{externalID}.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CreativeDTO getCreative(@PathVariable String format, @PathVariable String externalID, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        Creative creative = creativeService.findbyExternalID(user, externalID);

        return (copyService.copyFromDomain(creative, CreativeDTO.class));
    }


    @RequestMapping(value = "/creative/{externalID}.{format}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public CreativeDTO updateCreative(@PathVariable String format, @PathVariable String externalID, @RequestBody CreativeDTO creativeDTO, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Creative creative = creativeService.findbyExternalID(user, externalID);

        DestinationDTO destinationDTO = creativeDTO.getDestination();
        if (destinationDTO != null) {
            creativeService.setDestination(creative, destinationDTO.getType(), destinationDTO.getData());
        }

        creativeService.copyCustomAttributes(creative, creativeDTO.getEnglishTranslation(), null);// TODO - change the interface

        creativeService.notifyUpdate(creative);
        //pm.currentTransaction().commit();

        // TODO: remove this temporary hack once advertisers are forced to
        // select device identifier types themselves.
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(creative.getCampaign(), creative, getCampaignManager(), getDeviceManager());

        return (copyService.copyFromDomain(creative, CreativeDTO.class));
    }


    @RequestMapping(value = "/creative/{externalID}.{format}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    @Transactional(readOnly=false)
    public void deleteCreative(@PathVariable String format, @PathVariable String externalID, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Creative creative = creativeService.findbyExternalID(user, externalID);
        creativeService.delete(creative);

        //pm.currentTransaction().commit();
    }

    enum Command {
        submit, start, pause, stop;
    }


    @RequestMapping(value = "/creative/{externalID}.{format}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void changeCreativeStatus(@PathVariable String format, @PathVariable String externalID, @RequestParam(value = "command", required = true) Command command, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Creative creative = creativeService.findbyExternalID(user, externalID);

        switch (command) {
        case submit:
            creativeService.submit(creative);
            break;
        case start:
            creativeService.start(creative);
            break;
        case pause:
            creativeService.pause(creative);
            break;
        case stop:
            creativeService.stop(creative);
            break;
        }

        //pm.currentTransaction().commit();
    }

}
