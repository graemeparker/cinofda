package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class JsonCampaignView extends AbstractJsonView {

	@Override
	@SuppressWarnings("unchecked")
	protected void renderJson(Map model, HttpServletRequest request,
			JSONObject json) {
        json.put("masg-response", getCampaignJSON((Campaign)model.get(AbstractAdfonicWebService.CAMPAIGN)));
	}
	

}
