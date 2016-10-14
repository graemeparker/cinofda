package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
@SuppressWarnings("unchecked")
public class XmlCampaignListView extends AbstractXmlView {
	
	@Override
	protected void renderXml(Map model, HttpServletRequest request,
			XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        List<Campaign> campaigns = (List<Campaign>)model.get(AbstractAdfonicWebService.CAMPAIGNS);
        if(campaigns != null) {
        	for(Campaign campaign : campaigns) {
                writeCampaign(campaign, xml);
        	}
        }
        xml.endTag(true); // masg-response
	}
	
	

}
