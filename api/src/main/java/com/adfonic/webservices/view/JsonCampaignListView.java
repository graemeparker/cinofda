package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class JsonCampaignListView extends AbstractJsonView {
	@Override
	@SuppressWarnings("unchecked")
	protected void renderJson(Map model, HttpServletRequest request,
			JSONObject json) {
	       JSONArray masgResponse = new JSONArray();
	        for (Campaign cmp : (List<Campaign>)model.get(AbstractAdfonicWebService.CAMPAIGNS)) {
	            masgResponse.add(getCampaignJSON(cmp));
	        }
	        json.put("masg-response", masgResponse);
	}

}
