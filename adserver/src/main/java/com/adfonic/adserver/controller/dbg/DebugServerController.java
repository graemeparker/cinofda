package com.adfonic.adserver.controller.dbg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.anthavio.aspect.ApiPolicyOverride;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.dbg.dto.DbgApplicationDto;
import com.adfonic.adserver.controller.dbg.dto.DbgCacheMetaDto;
import com.adfonic.adserver.controller.dbg.dto.DbgContainerDto;
import com.adfonic.adserver.controller.dbg.dto.DbgSpringInfoDto;
import com.adfonic.adserver.controller.dbg.dto.DbgSpringInfoDto.DbgSpringEnvironmentDto;
import com.adfonic.adserver.controller.rtb.BiddingSwitchController;
import com.adfonic.adserver.impl.DataCacheProperties;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.util.ConfUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping("/adserver")
public class DebugServerController {

    public static final String APP_CONTEXT = "/application";
    public static final String SRV_CONTEXT = "/container";
    public static final String THREAD_CONTEXT = "/threads";

    @Autowired
    private DomainCacheManager domainCacheManager;

    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Value("file:" + ConfUtils.CONFIG_DIR_CONFIG + "/" + Constant.AS_CONFIG_FILENAME)
    private File propertiesFile;

    @Autowired
    private DataCacheProperties dcProperties;

    private final ObjectMapper debugJsonMapper = DebugBidController.debugJsonMapper;

    private final Date startedAt = new Date();

    static enum ServerFields {
        cache, build, system, environment, address, properties, context, spring;
    }

    @RequestMapping(value = SRV_CONTEXT, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String debugContainer(HttpServletRequest httpRequest) throws Exception {
        DbgContainerDto serverDto = new DbgContainerDto();
        serverDto.setStartedAt(startedAt);
        serverDto.setSnapshotAt(new Date());

        if (httpRequest.getParameter("noaddress") == null) {
            Map<String, String> addressMap = getInetAddressInfo();
            serverDto.setInetAddress(addressMap);
        }

        if (httpRequest.getParameter("nosystem") == null) {
            serverDto.setSystemProperties(toStringMap(System.getProperties()));
        }

        if (httpRequest.getParameter("noenvironment") == null) {
            serverDto.setEnvironmentVariables(System.getenv());
        }

        if (httpRequest.getParameter("nocontext") == null) {
            Map<String, Object> contextMap = getServletContextInfo(httpRequest.getServletContext());
            serverDto.setServletContext(contextMap);
        }

        if (httpRequest.getParameter("nospring") == null) {
            DbgSpringInfoDto springInfo = getSpringContextInfo(httpRequest.getServletContext());
            serverDto.setSpringContext(springInfo);
        }

        return debugJsonMapper.writeValueAsString(serverDto);
        //return debugAdServer(httpRequest);
    }

    @RequestMapping(value = APP_CONTEXT, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String debugApplication(HttpServletRequest httpRequest) throws Exception {
        DbgApplicationDto adserverDto = new DbgApplicationDto();
        adserverDto.setBidding(BiddingSwitchController.BIDDING_ENABLED);
        adserverDto.setStartedAt(startedAt);
        adserverDto.setSnapshotAt(new Date());

        //Map<ServerFields, Boolean> fields = fields(httpRequest, ServerFields.class);

        if (httpRequest.getParameter("nocache") == null) {
            DbgCacheMetaDto adserverCacheDto = DbgBuilder.getCacheMetaData(adserverCacheManager);
            adserverDto.setAdserverCache(adserverCacheDto);

            DbgCacheMetaDto domainCacheDto = DbgBuilder.getCacheMetaData(domainCacheManager);
            adserverDto.setDomainCache(domainCacheDto);
        }

        if (httpRequest.getParameter("nobuild") == null) {
            try (InputStream propsStream = getClass().getResourceAsStream("/META-INF/buildmetadata.properties")) {
                //buildmetadata-maven-plugin
                Properties buildProperties = new Properties();
                if (propsStream != null) {
                    buildProperties.load(propsStream);
                    adserverDto.setBuildProperties(toStringMap(buildProperties));
                }
            }
        }

        if (httpRequest.getParameter("noproperties") == null) {
            adserverDto.setAdserverProperties(getAdserverProperties());
            adserverDto.setDataCacheProperties(dcProperties.getAllProperties());
        }

        return debugJsonMapper.writeValueAsString(adserverDto);
    }

    private Map<String, String> getAdserverProperties() {
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(propertiesFile)) {
            properties.load(stream);
        } catch (Exception x) {
            properties.setProperty("exception", String.valueOf(x));
        }
        Map<String, String> map = toStringMap(properties);

        for (Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            if (name != null && name.indexOf("password") != -1) {
                entry.setValue("************");
            }
        }
        return map;
    }

    private Map<String, String> toStringMap(Properties properties) {
        Map<String, String> map = new HashMap<String, String>(properties.size());
        for (Entry<Object, Object> entry : properties.entrySet()) {
            map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return map;
    }

    @RequestMapping(value = "/request", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String debugRequest(HttpServletRequest httpRequest) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("request", getRequestInfo(httpRequest));
        return debugJsonMapper.writeValueAsString(response);
    }

    /**
     * Test if logging message happens to be in correct file regardless the logging framework used
     */
    @RequestMapping(value = "/logger", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    @ApiPolicyOverride
    public String debugLogger(HttpServletRequest httpRequest) {

        String logger = httpRequest.getParameter("logger");
        if (StringUtils.isBlank(logger)) {
            logger = "DebugLogger";
        }
        String message = httpRequest.getParameter("message");
        if (StringUtils.isBlank(message)) {
            message = "Generic debug logging message at " + new Date();
        }
        String level = httpRequest.getParameter("level");
        if (StringUtils.isBlank(level)) {
            level = "info";
        }

        if (level.equals("trace") || level.startsWith("finer") || level.startsWith("finest")) {
            org.apache.log4j.Logger.getLogger(logger).trace("log4j1 Logger says: " + message);
            org.apache.logging.log4j.LogManager.getLogger(logger).trace("log4j2 Logger says: " + message);
            org.slf4j.LoggerFactory.getLogger(logger).trace("slf4j Logger says: " + message);
            java.util.logging.Logger.getLogger(logger).finest("java.util Logger says: " + message);
            org.apache.commons.logging.LogFactory.getLog(logger).trace("commons-logging Logger says: " + message);
        } else if (level.equals("debug") || level.startsWith("fine")) {
            org.apache.log4j.Logger.getLogger(logger).debug("log4j1 Logger says: " + message);
            org.apache.logging.log4j.LogManager.getLogger(logger).debug("log4j2 Logger says: " + message);
            org.slf4j.LoggerFactory.getLogger(logger).debug("slf4j Logger says: " + message);
            java.util.logging.Logger.getLogger(logger).fine("java.util Logger says: " + message);
            org.apache.commons.logging.LogFactory.getLog(logger).debug("commons-logging Logger says: " + message);
        } else if (level.equals("info")) {
            org.apache.log4j.Logger.getLogger(logger).info("log4j1 Logger says: " + message);
            org.apache.logging.log4j.LogManager.getLogger(logger).info("log4j2 Logger says: " + message);
            org.slf4j.LoggerFactory.getLogger(logger).info("slf4j Logger says: " + message);
            java.util.logging.Logger.getLogger(logger).info("java.util Logger says: " + message);
            org.apache.commons.logging.LogFactory.getLog(logger).info("commons-logging Logger says: " + message);
        } else if (level.equals("warn") || level.equals("warning")) {
            org.apache.log4j.Logger.getLogger(logger).warn("log4j1 Logger says: " + message);
            org.apache.logging.log4j.LogManager.getLogger(logger).warn("log4j2 Logger says: " + message);
            org.slf4j.LoggerFactory.getLogger(logger).warn("slf4j Logger says: " + message);
            java.util.logging.Logger.getLogger(logger).warning("java.util Logger says: " + message);
            org.apache.commons.logging.LogFactory.getLog(logger).warn("commons-logging Logger says: " + message);
        } else if (level.equals("error") || level.equals("severe")) {
            org.apache.log4j.Logger.getLogger(logger).error("log4j1 Logger says: " + message);
            org.apache.logging.log4j.LogManager.getLogger(logger).error("log4j2 Logger says: " + message);
            org.slf4j.LoggerFactory.getLogger(logger).error("slf4j Logger says: " + message);
            java.util.logging.Logger.getLogger(logger).severe("java.util Logger says: " + message);
            org.apache.commons.logging.LogFactory.getLog(logger).error("commons-logging Logger says: " + message);
        } else {
            throw new IllegalArgumentException("Unsupported level: " + level);
        }

        System.out.println("System.out says: " + message);
        System.out.println("System.err says: " + message);
        return message;
    }

    /**
     * 
     */
    @RequestMapping(value = "/threads", method = RequestMethod.GET, produces = "text/plain")
    @ApiPolicyOverride
    public void debugThreads(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();

        Comparator<? super Thread> comparator = new Comparator<Thread>() {
            @Override
            public int compare(Thread first, Thread second) {
                return first.getName().toLowerCase().compareTo(second.getName().toLowerCase());
            }
        };

        PrintWriter writer = httpResponse.getWriter();
        List<Thread> sortedThreads = allStackTraces.keySet().stream().sorted(comparator).collect(Collectors.toList());
        for (Thread thread : sortedThreads) {
            writer.println(thread.getId() + ", Name: " + thread.getName() + ", Priority: " + thread.getPriority() + ", State: " + thread.getState() + ", ThreadGroup: "
                    + thread.getThreadGroup());
            StackTraceElement[] stackTrace = allStackTraces.get(thread);
            for (StackTraceElement element : stackTrace) {
                writer.println(element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
            }
            writer.println();
        }

    }

    public static Map<String, Object> getRequestInfo(HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, Object> httpRequestMap = getHttpRequestInfo(httpRequest, true);
        result.putAll(httpRequestMap);

        Map<String, Object> sessionInfo = getSessionInfo(httpRequest.getSession());
        result.put("session", sessionInfo);

        result.put("cookies", httpRequest.getCookies());

        return result;
    }

    public static Map<String, Object> getHttpRequestInfo(HttpServletRequest httpRequest, boolean attributes) {
        Map<String, Object> httpRequestMap = new HashMap<String, Object>();
        httpRequestMap.put("@type", httpRequest.getClass().getName());
        httpRequestMap.put("CharacterEncoding", httpRequest.getCharacterEncoding());
        httpRequestMap.put("AuthType", httpRequest.getAuthType());
        httpRequestMap.put("ContentType", httpRequest.getContentType());
        httpRequestMap.put("ContextPath", httpRequest.getContextPath());
        httpRequestMap.put("LocalAddr", httpRequest.getLocalAddr());
        httpRequestMap.put("Locale", httpRequest.getLocale());
        httpRequestMap.put("Locales", httpRequest.getLocales());
        httpRequestMap.put("LocalName", httpRequest.getLocalName());
        httpRequestMap.put("LocalPort", httpRequest.getLocalPort());
        httpRequestMap.put("Method", httpRequest.getMethod());
        httpRequestMap.put("PathInfo", httpRequest.getPathInfo());
        httpRequestMap.put("PathTranslated", httpRequest.getPathTranslated());
        httpRequestMap.put("Protocol", httpRequest.getProtocol());
        httpRequestMap.put("QueryString", httpRequest.getQueryString());

        httpRequestMap.put("RemoteAddr", httpRequest.getRemoteAddr());
        httpRequestMap.put("RemoteHost", httpRequest.getRemoteHost());
        httpRequestMap.put("RemotePort", httpRequest.getRemotePort());
        httpRequestMap.put("RemoteUser", httpRequest.getRemoteUser());

        httpRequestMap.put("RequestedSessionId", httpRequest.getRequestedSessionId());
        httpRequestMap.put("RequestURI", httpRequest.getRequestURI());
        httpRequestMap.put("RequestURL", httpRequest.getRequestURL());

        httpRequestMap.put("Scheme", httpRequest.getScheme());
        httpRequestMap.put("ServerName", httpRequest.getServerName());
        httpRequestMap.put("ServerPort", httpRequest.getServerPort());
        httpRequestMap.put("ServletPath", httpRequest.getServletPath());
        httpRequestMap.put("UserPrincipal", String.valueOf(httpRequest.getUserPrincipal()));

        httpRequestMap.put("RequestedSessionIdFromCookie", httpRequest.isRequestedSessionIdFromCookie());
        httpRequestMap.put("RequestedSessionIdFromURL", httpRequest.isRequestedSessionIdFromURL());
        httpRequestMap.put("RequestedSessionIdValid", httpRequest.isRequestedSessionIdValid());

        Map<String, Object> parametersMap = new HashMap<String, Object>();
        Map<String, String[]> parameters = httpRequest.getParameterMap();
        for (Entry<String, String[]> entry : parameters.entrySet()) {
            parametersMap.put(entry.getKey(), entry.getValue()); //Not nice - getValue() returns array
        }
        httpRequestMap.put("parameters", parametersMap);

        Map<String, Object> headersMap = new HashMap<String, Object>();
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headersMap.put(headerName, httpRequest.getHeader(headerName));
        }
        httpRequestMap.put("headers", headersMap);

        if (attributes) {
            Map<String, Object> attributesMap = new HashMap<String, Object>();
            Enumeration<String> attributeNames = httpRequest.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                attributesMap.put(name, String.valueOf(httpRequest.getAttribute(name)));
            }
            httpRequestMap.put("attributes", attributesMap);
        }
        return httpRequestMap;
    }

    private static Map<String, String> getInetAddressInfo() {
        Map<String, String> addressMap = new HashMap<String, String>();
        try {
            InetAddress address = InetAddress.getLocalHost();
            addressMap.put("CanonicalHostName", address.getCanonicalHostName());
            addressMap.put("HostAddress", address.getHostAddress());
            addressMap.put("HostName", address.getHostName());
            StringBuilder ipAddr = new StringBuilder();
            for (byte b : address.getAddress()) {
                ipAddr.append(b).append('.');
            }
            ipAddr.deleteCharAt(ipAddr.length() - 1);

        } catch (UnknownHostException uhx) {
            addressMap.put("error", String.valueOf(uhx));
        }
        return addressMap;
    }

    private DbgSpringInfoDto getSpringContextInfo(ServletContext servletContext) {
        DbgSpringInfoDto springInfo = new DbgSpringInfoDto();
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        springInfo.setId(springContext.getId());
        springInfo.setDisplayName(springContext.getDisplayName());
        springInfo.setApplicationName(springContext.getApplicationName());
        springInfo.setBeanDefinitionCount(springContext.getBeanDefinitionCount());

        StandardServletEnvironment environment = (StandardServletEnvironment) springContext.getEnvironment();
        DbgSpringEnvironmentDto environmentInfo = new DbgSpringEnvironmentDto();
        environmentInfo.setActiveProfiles(environment.getActiveProfiles());
        environmentInfo.setDefaultProfiles(environment.getDefaultProfiles());
        environmentInfo.setPropertySources(getAdserverProperties());
        Map<String, String> sourcesMap = new HashMap<String, String>();
        Iterator<PropertySource<?>> propertySourcesIterator = environment.getPropertySources().iterator();
        while (propertySourcesIterator.hasNext()) {
            PropertySource<?> propertySource = propertySourcesIterator.next();
            sourcesMap.put(propertySource.getName(), String.valueOf(propertySource.getSource()));
        }
        environmentInfo.setPropertySources(sourcesMap);
        springInfo.setEnvironment(environmentInfo);

        String[] beanDefinitionNames = springContext.getBeanDefinitionNames();
        Map<String, String> definitionMap = new HashMap<String, String>();
        for (String beanName : beanDefinitionNames) {
            definitionMap.put(beanName, String.valueOf(springContext.getBean(beanName)));
        }
        springInfo.setBeanDefinitionMap(definitionMap);
        return springInfo;
    }

    private Map<String, Object> getServletContextInfo(ServletContext servletContext) {
        Map<String, Object> servletContextMap = new HashMap<String, Object>();
        servletContextMap.put("ContextPath", servletContext.getContextPath());
        servletContextMap.put("ServerInfo", servletContext.getServerInfo());
        servletContextMap.put("EffectiveSessionTrackingModes", servletContext.getEffectiveSessionTrackingModes());
        servletContextMap.put("FilterRegistrations", servletContext.getFilterRegistrations().keySet());
        servletContextMap.put("InitParameterNames", servletContext.getInitParameterNames());
        servletContextMap.put("JspConfigDescriptor", servletContext.getJspConfigDescriptor());
        servletContextMap.put("ServletContextName", servletContext.getServletContextName());
        servletContextMap.put("ServletRegistrations", servletContext.getServletRegistrations().keySet());
        //servletContextMap.put("VirtualServerName", servletContext.getVirtualServerName());

        Enumeration<String> contextAttributeNames = servletContext.getAttributeNames();
        Map<String, Object> contextAttributesMap = new HashMap<String, Object>();
        while (contextAttributeNames.hasMoreElements()) {
            String name = contextAttributeNames.nextElement();
            //skip this extremely long attribute
            if ("org.apache.tomcat.util.scan.MergedWebXml".equals(name)) {
                continue;
            }
            Object attribute = servletContext.getAttribute(name);
            if (attribute != null) {
                contextAttributesMap.put(name, String.valueOf(attribute));
            } else {
                contextAttributesMap.put(name, null); //nulls are important indication so keep them
            }
        }
        servletContextMap.put("attributes", contextAttributesMap);
        return servletContextMap;
    }

    private static Map<String, Object> getSessionInfo(HttpSession httpSession) {
        if (httpSession != null) {
            Map<String, Object> sessionMap = new HashMap<String, Object>();
            sessionMap.put("Id", httpSession.getId());
            sessionMap.put("CreationTime", httpSession.getCreationTime());
            sessionMap.put("LastAccessedTime", httpSession.getLastAccessedTime());
            sessionMap.put("MaxInactiveInterval", httpSession.getMaxInactiveInterval());

            Map<String, Object> sessionAttributesMap = new HashMap<String, Object>();
            Enumeration<String> attributeNames = httpSession.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object attribute = httpSession.getAttribute(name);
                if (attribute != null) {
                    if (attribute instanceof Number || attribute instanceof Date || attribute instanceof Boolean) {
                        sessionAttributesMap.put(name, attribute);
                    } else {
                        sessionAttributesMap.put(name, String.valueOf(attribute));
                    }
                } else {
                    sessionAttributesMap.put(name, null); //nulls are important indicator
                }
            }
            sessionMap.put("attributes", sessionAttributesMap);
            return sessionMap;
        }
        return null;
    }

    private <T extends Enum<T>> Map<T, Boolean> fields(HttpServletRequest request, Class<T> enumClass) {

        String pfields = request.getParameter("fields");
        if (pfields == null) {
            return Collections.emptyMap();
        } else {
            String[] split = pfields.split(",");
            T[] constants = enumClass.getEnumConstants();
            Map<T, Boolean> result = new HashMap<T, Boolean>(split.length);
            for (String item : split) {
                for (T t : constants) {
                    Boolean flag;
                    if (item.startsWith("!")) {
                        flag = Boolean.FALSE;
                        item = item.substring(1);
                    } else {
                        flag = Boolean.TRUE;
                    }
                    if (t.name().equals(item)) {
                        result.put(t, flag);
                    }
                }
            }
            return result;
        }
    }
}
