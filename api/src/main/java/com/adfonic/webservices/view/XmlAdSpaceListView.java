package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;
import com.adfonic.util.XmlWriter;

@Component
public class XmlAdSpaceListView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        for (AdSpace pub : (List<AdSpace>)model.get("adSpaces")) {
            writeAdSpace(pub, xml);
        }
        xml.endTag(true); // masg-response
    }
}
