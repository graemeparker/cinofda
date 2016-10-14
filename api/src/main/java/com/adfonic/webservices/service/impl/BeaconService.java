package com.adfonic.webservices.service.impl;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.adfonic.webservices.service.IBeaconService;

@Service
public class BeaconService extends WebApplicationObjectSupport implements IBeaconService {
    private static final transient Logger LOG = Logger.getLogger(BeaconService.class.getName());

    private static final String BEACON_GIF = "images/1x1transparent.gif";

    private byte[] beaconContent;


    @PostConstruct
    public void initializeGifContent() throws java.io.IOException {
        // Read in the 1x1 transparent GIF content
        beaconContent = IOUtils.toByteArray(getServletContext().getResourceAsStream(BEACON_GIF));
        LOG.info("initializeGifContent: Read " + beaconContent.length + " bytes from " + BEACON_GIF);
    }


    @Override
    public byte[] beaconContent() {
        return beaconContent;
    }

}
