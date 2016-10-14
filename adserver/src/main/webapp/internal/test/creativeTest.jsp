<%@ page import="com.adfonic.data.cache.test.CreativesTest" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>

<%
	ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    CreativesTest test = appContext.getBean(CreativesTest.class);
%>

<%=test.creativesTest()%>