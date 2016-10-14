<%@ page import="com.adfonic.data.cache.test.AdSpaceTest" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>

<%
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    AdSpaceTest test = appContext.getBean(AdSpaceTest.class);
%>

<%=test.adSpaceTest()%>