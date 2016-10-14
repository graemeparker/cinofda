package com.adfonic.webservices.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.ErrorCode;

@Component
public class XmlErrorView extends AbstractXmlView {
    @Override
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Integer responseStatus=(Integer)model.get("responseStatus");
        response.setStatus(responseStatus==null? HttpServletResponse.SC_BAD_REQUEST: responseStatus);
        super.render(model, request, response);
    }
                       
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        Integer code = (Integer)model.get("code");
        if (code == null) {
            code = ErrorCode.UNKNOWN;
        }

        String description = (String)model.get("error");
        if (description == null) {
            description = "An unknown error has occurred.";
        }

        xml.startTag("masg-error").newLine()
            .startTag("code").text(code).endTag(true)
            .startTag("description").text(description).endTag(true)
            .endTag(true); // masg-error
    }
}
