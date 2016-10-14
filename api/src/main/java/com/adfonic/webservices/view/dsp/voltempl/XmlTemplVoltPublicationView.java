package com.adfonic.webservices.view.dsp.voltempl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.view.XmlPublicationView;

@Component
public class XmlTemplVoltPublicationView extends XmlPublicationView {

    @Autowired
    private PublicationExtractorTemplateVolt publicationExtractor;


    @Override
    protected void writePublication(Publication pub, XmlWriter xml) {
        publicationExtractor.writePublicationXML(xml, pub);
    }

}
