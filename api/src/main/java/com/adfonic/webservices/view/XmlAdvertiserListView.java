package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Advertiser;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class XmlAdvertiserListView extends AbstractXmlView {
	
	@Override
	@SuppressWarnings("unchecked")
	protected void renderXml(Map model, HttpServletRequest request,
			XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        List<Advertiser> advertisers = (List<Advertiser>)model.get(AbstractAdfonicWebService.ADVERTISERS);
        if(advertisers != null) {
        	for(Advertiser advertiser : advertisers) {
                writeAdvertiser(advertiser, xml);
        	}
        }
        xml.endTag(true); // masg-response
	}
	
	

}
