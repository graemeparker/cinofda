package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Advertiser;
import com.adfonic.webservices.controller.AbstractAdfonicWebService;

@Component
public class JsonAdvertiserListView extends AbstractJsonView {

	@Override
	@SuppressWarnings("unchecked")
	protected void renderJson(Map model, HttpServletRequest request,
			JSONObject json) {
	       JSONArray masgResponse = new JSONArray();
	        for (Advertiser adv : (List<Advertiser>)model.get(AbstractAdfonicWebService.ADVERTISERS)) {
	            masgResponse.add(getAdvertiserJSON(adv));
	        }
	        json.put("masg-response", masgResponse);
	}

}
