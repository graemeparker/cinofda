package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;

@Component
public class JsonAdSpaceView extends AbstractJsonView {
    @Override
    protected void renderJson(Map model, HttpServletRequest request, JSONObject json) {
        json.put("masg-response", getAdSpaceJSON((AdSpace)model.get("adSpace")));
    }
}
