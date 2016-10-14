package com.adfonic.webservices.view.dsp;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.util.DSPetc;
import com.adfonic.webservices.view.dsp.builders.FlatJsonObjectBuilder;
import com.adfonic.webservices.view.dsp.builders.SerializedFormatBuilders;

@Component(DSPetc.VOLTARI_CPNY_NAME + DSPetc.PROF_SPECIFC_EXTR_SFX)
public class PublicationExtractorVoltari implements PublicationExtractor {

    public JSONObject getPublicationJSON(Publication pub) {
        return SerializedFormatBuilders.buildPublication(pub, new FlatJsonObjectBuilder(new JSONObject()));
    }

    @Override
    public void writePublicationXML(XmlWriter xml, Publication pub) {
        throw new UnsupportedOperationException();// no xml support for voltari
    }
}
