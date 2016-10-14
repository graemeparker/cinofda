package com.adfonic.webservices.view.dsp.voltempl;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.webservices.view.JsonPublicationListView;

@Component
public class JsonTemplVoltPublicationListView extends JsonPublicationListView{

    @Autowired
    private PublicationExtractorTemplateVolt publicationExtractor;

    @Override
    protected JSONObject getPublicationJSON(Publication pub) {
        return publicationExtractor.getPublicationJSON(pub);
    }
}
