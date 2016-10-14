package com.adfonic.adserver.controller.dbg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.rtb.impl.RtbIdServiceImpl;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping(FlushController.URL_CONTEXT)
public class FlushController {

    public static final String URL_CONTEXT = "/adserver/flush";

    @Autowired
    private RtbIdServiceImpl rtbIdService;

    public void flush() {
        //rtbIdService.handleUnrecognizedRtbId(null, 0, URL_CONTEXT);
    }
}
