package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;

@Component
public class XmlPublicationView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        writePublication((Publication)model.get("publication"), xml);
        xml.endTag(true); // masg-response
    }
}
