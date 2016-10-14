package com.adfonic.webservices.view.dsp.builders;

import com.adfonic.util.XmlWriter;

public class FlatCustomXmlElemWriter implements FlatObjectBuilder<XmlWriter>{
    
    XmlWriter xml;
    
    public FlatCustomXmlElemWriter(XmlWriter xml){
        this.xml=xml;
    }

    @Override
    public FlatObjectBuilder<XmlWriter> set(String name, String value) {
        xml.startTag(name).text(value).endTag(true);
        return this;
    }

    @Override
    public XmlWriter built() {
        return xml;
    }

}
