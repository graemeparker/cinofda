package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Advertiser;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class JsonAdvertiserView extends AbstractJsonView {

	@Override
	@SuppressWarnings("unchecked")
	protected void renderJson(Map model, HttpServletRequest request,
			JSONObject json) {
        json.put("masg-response", getAdvertiserJSON((Advertiser)model.get(AbstractAdfonicWebService.ADVERTISER)));
	}
	

}
