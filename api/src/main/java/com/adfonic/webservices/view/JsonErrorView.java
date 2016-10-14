package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.webservices.ErrorCode;

@Component
public class JsonErrorView extends AbstractJsonView {
    @Override
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Integer responseStatus=(Integer)model.get("responseStatus");
        response.setStatus(responseStatus==null? HttpServletResponse.SC_BAD_REQUEST: responseStatus);
        super.render(model, request, response);
    }
                       
    @Override
    protected void renderJson(Map model, HttpServletRequest request, JSONObject json) {
        Integer code = (Integer)model.get("code");
        if (code == null) {
            code = ErrorCode.UNKNOWN;
        }

        String description = (String)model.get("error");
        if (description == null) {
            description = "An unknown error has occurred.";
        }

        JSONObject error = new JSONObject();
        error.put("code", code);
        error.put("description", description);
        json.put("masg-error", error);
    }
}
