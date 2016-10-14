package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class XmlCampaignView extends AbstractXmlView {
	
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        writeCampaign((Campaign)model.get(AbstractAdfonicWebService.CAMPAIGN), xml);
        xml.endTag(true); // masg-response
    }
}
