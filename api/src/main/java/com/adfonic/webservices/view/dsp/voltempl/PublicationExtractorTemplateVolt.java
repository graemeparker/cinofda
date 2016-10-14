package com.adfonic.webservices.view.dsp.voltempl;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.view.dsp.PublicationExtractor;
import com.adfonic.webservices.view.dsp.PublicationExtractorVoltari;
import com.adfonic.webservices.view.dsp.PublicationExtractorWeve;

/*
 * Just a common wrapper for use in views as the same views would suffice for
 * both voltari and weve currently
 */
@Component
public class PublicationExtractorTemplateVolt implements PublicationExtractor{

    @Autowired
    private PublicationExtractorVoltari voltariExtractor;

    @Autowired
    private PublicationExtractorWeve weveExtractor;

    @Override
    public JSONObject getPublicationJSON(Publication pub) {
        return voltariExtractor.getPublicationJSON(pub);
    }

    @Override
    public void writePublicationXML(XmlWriter xml, Publication pub) {
        weveExtractor.writePublicationXML(xml, pub);
    }

}
