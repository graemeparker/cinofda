package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Advertiser;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class XmlAdvertiserView extends AbstractXmlView {
	
	@Override
	protected void renderXml(Map model, HttpServletRequest request,
			XmlWriter xml) {
        xml.startTag("masg-response").newLine();
        writeAdvertiser((Advertiser)model.get(AbstractAdfonicWebService.ADVERTISER), xml);
        xml.endTag(true); // masg-response
	}
	
	

}
