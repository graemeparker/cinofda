package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Publication;

@Component
public class JsonPublicationView extends AbstractJsonView {
    @Override
    protected void renderJson(Map model, HttpServletRequest request, JSONObject json) {
        json.put("masg-response", getPublicationJSON((Publication)model.get("publication")));
    }
}
