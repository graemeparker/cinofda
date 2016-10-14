package com.adfonic.webservices.util;

import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.adfonic.util.BasicAuthUtils;

public class MockWebContextLoader extends AbstractContextLoader {

    //public static final ServletContext SERVLET_CONTEXT = new MockServletContext("/src/main/webapp", new FileSystemResourceLoader());
    public static final ServletContext SERVLET_CONTEXT = new MockServletContext("/Users/ac/adfonic/svn/adformat/webservices/src/main/webapp", new FileSystemResourceLoader());
    //public static final ServletContext SERVLET_CONTEXT = new MockServletContext("/Users/ac/adfonic/svn/adformat/webservices/target/adfonic-webservices-1.0", new FileSystemResourceLoader());

    private final static GenericWebApplicationContext webContext = new GenericWebApplicationContext();


    protected static BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
        return new XmlBeanDefinitionReader(context);
    }


    /*public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception*/ static{

        SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
        webContext.setServletContext(SERVLET_CONTEXT);
        //createBeanDefinitionReader(webContext).loadBeanDefinitions(locations);
        
        System.setProperty("adfonic.config.home", "src/test/resources");
        createBeanDefinitionReader(webContext).loadBeanDefinitions(new String[]{"classpath:dispatcher-servlet-test.xml", "classpath:adfonic-webservices-test-context.xml"});
        AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
        try{
        webContext.refresh();
        }catch(Throwable t){
            t.printStackTrace();
        }
        webContext.registerShutdownHook();
        //return webContext;
    }


    public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception {

        SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
        webContext.setServletContext(SERVLET_CONTEXT);
        createBeanDefinitionReader(webContext).loadBeanDefinitions(locations);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
        webContext.refresh();
        webContext.registerShutdownHook();
        return webContext;
    }

    // Required as of Spring 3.1 to implement SmartContextLoader
    public final ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        return webContext;
    }

    public static WebApplicationContext getInstance() {
        return webContext;
    }


    protected String getResourceSuffix() {
        return "-context.xml";
    }

    private static DispatcherServlet dispatcherServlet;


    @SuppressWarnings("serial")
    public static DispatcherServlet getServletInstance() {
        try {
            if (null == dispatcherServlet) {
                dispatcherServlet = new DispatcherServlet() {
                    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
                        GenericWebApplicationContext wac = new GenericWebApplicationContext();
                        wac.setParent(MockWebContextLoader.getInstance());
                        // wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
                        wac.refresh();
                        return wac;
                    }
                };

                MockServletConfig config=new MockServletConfig("Adfonic Web Services");
                //config.addInitParameter("contextConfigLocation", "classpath:adfonic-webservices-context.xml, classpath:dispatcher-servlet.xml");
                dispatcherServlet.init(config);
            }
        } catch (Throwable t) {
            Assert.fail("Unable to create a dispatcher servlet: " + t.getMessage());
        }
        return dispatcherServlet;
    }


    public static MockHttpServletRequest mockRequest(String method, String uri, String user, String pass, Form form) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        req.setAuthType(MockHttpServletRequest.BASIC_AUTH);
        
        req.addHeader("Authorization", BasicAuthUtils.generateAuthorizationHeader(user, pass));
        
        if (form == null) {
            return req;
        }
        for (Entry<String, String> entry : form.getEntries()) {
            req.addParameter(entry.getKey(), entry.getValue());
        }
        return req;
    }


    public static MockHttpServletResponse mockResponse() {
        return new MockHttpServletResponse();
    }
}
