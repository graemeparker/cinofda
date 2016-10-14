package com.adfonic.webservices;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.webservices.view.JsonErrorView;
import com.adfonic.webservices.view.XmlErrorView;

@Component
public class RenderTimeError {

    @Autowired
    private JsonErrorView jsonErrorView;

    @Autowired
    private XmlErrorView xmlErrorView;

    public void renderXML(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        xmlErrorView.render(model, request, response);
    }

    public void renderJSON(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        jsonErrorView.render(model, request, response);
    }
}
