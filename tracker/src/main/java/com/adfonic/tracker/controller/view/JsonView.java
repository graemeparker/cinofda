package com.adfonic.tracker.controller.view;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JsonView {

    private static final transient Logger LOG = LoggerFactory.getLogger(JsonView.class.getName());

    public static final MediaType JSON_MIME_TYPE = MediaType.APPLICATION_JSON;

    @Autowired
    private MappingJackson2HttpMessageConverter jsonConverter;

    public ModelAndView render(Map<String, Object> model, HttpServletResponse response) {
        try {
            LOG.debug("Rendering JSON view for {}", model);
            jsonConverter.write(model, JSON_MIME_TYPE, new ServletServerHttpResponse(response));
        } catch (HttpMessageNotWritableException e) {
            LOG.warn("Cannot render view for {}", model);
        } catch (IOException e) {
            LOG.warn("IO Error for {}", model);
        }
        return null;
    }
}
