package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.adfonic.webservices.view.util.GenericMarshaller;

public abstract class AbstractGenericView implements GenericView, View{

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        getMarshaller().marshal(model.get(RESULT), (String)model.get(RESULT_LIST_WRAPPER), response.getWriter());
    }
    
    protected abstract GenericMarshaller getMarshaller();

}