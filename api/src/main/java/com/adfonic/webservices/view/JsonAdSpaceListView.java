package com.adfonic.webservices.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;

@Component
public class JsonAdSpaceListView extends AbstractJsonView {
    @Override
    protected void renderJson(Map model, HttpServletRequest request, JSONObject json) {
        JSONArray masgResponse = new JSONArray();
        for (AdSpace pub : (List<AdSpace>)model.get("adSpaces")) {
            masgResponse.add(getAdSpaceJSON(pub));
        }
        json.put("masg-response", masgResponse);
    }
}
