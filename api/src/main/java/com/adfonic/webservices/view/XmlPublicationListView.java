package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;
import com.adfonic.util.XmlWriter;

@Component
public class XmlPublicationListView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        for (Publication pub : (List<Publication>)model.get("publications")) {
            writePublication(pub, xml);
        }
        xml.endTag(true); // masg-response
    }
}
