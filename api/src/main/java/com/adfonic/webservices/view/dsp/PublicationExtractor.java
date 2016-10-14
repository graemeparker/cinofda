package com.adfonic.webservices.view.dsp;

import org.json.simple.JSONObject;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;

public interface PublicationExtractor {

    // statically assigned internal iab-id to adfonic not categorized. TODO put as system global
    public static final String ADFONIC_NOT_CATEGORIZED_CAT_IAB_ID = "ADF-066";

    public JSONObject getPublicationJSON(Publication pub);

    public void writePublicationXML(XmlWriter xml, Publication pub);
}
