package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;
import com.adfonic.util.XmlWriter;

@Component
public class XmlAdSpaceView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        writeAdSpace((AdSpace)model.get("adSpace"), xml);
        xml.endTag(true); // masg-response
    }
}
