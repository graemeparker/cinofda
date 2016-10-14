package com.adfonic.datacollector.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.util.status.AppInfoServlet;

@Configuration
public class DcWebSpringConfig {

    @Bean
    public ServletRegistrationBean statusServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new AppInfoServlet(), "/status");
        Map<String, String> params = new HashMap<String, String>();
        // params.put("application-class", DomainSerializerS3.class.getName());
        registration.setInitParameters(params);
        return registration;
    }
}
