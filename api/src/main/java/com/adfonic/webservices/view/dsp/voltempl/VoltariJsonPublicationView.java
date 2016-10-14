package com.adfonic.webservices.view.dsp.voltempl;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.webservices.util.DSPetc;
import com.adfonic.webservices.view.JsonPublicationView;


@Component("json" + DSPetc.VIEW_SFX_VOLTARI_TEMPL + DSPetc.PUB_VIEW_SFX)
public class VoltariJsonPublicationView extends JsonPublicationView {

    @Autowired
    private PublicationExtractorTemplateVolt publicationExtractor;

    @Override
    protected JSONObject getPublicationJSON(Publication pub) {
        return publicationExtractor.getPublicationJSON(pub);
    }
}
