package com.adfonic.webservices.view.dsp;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.util.DSPetc;
import com.adfonic.webservices.view.dsp.builders.FlatCustomXmlElemWriter;
import com.adfonic.webservices.view.dsp.builders.LowerCamelCaseTransform;
import com.adfonic.webservices.view.dsp.builders.NameTransformingObjectBuilder;
import com.adfonic.webservices.view.dsp.builders.SerializedFormatBuilders;

@Component(DSPetc.WEVE_DISCR + DSPetc.PROF_SPECIFC_EXTR_SFX)
public class PublicationExtractorWeve implements PublicationExtractor{

    @Override
    public JSONObject getPublicationJSON(Publication pub) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writePublicationXML(XmlWriter xml, Publication pub) {
        SerializedFormatBuilders.buildPublication(pub, 
//                                    new FlatCustomXmlElemWriter(xml.startTag("publication").newLine()))
                                      new NameTransformingObjectBuilder<>(
                                              new FlatCustomXmlElemWriter(xml.startTag("publication").newLine()), 
                                              LowerCamelCaseTransform.INSTANCE))
        .endTag(true);
    }

}
