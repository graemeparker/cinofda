<%@page import="com.adfonic.domain.cache.DomainCacheManager"%>
<%@page import="com.adfonic.adserver.controller.dbg.DbgBuilder"%>
<%@page import="com.adfonic.adserver.controller.dbg.DebugCacheController"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.adfonic.domain.cache.ext.AdserverDomainCache"%>
<%@page import="com.adfonic.domain.cache.AdserverDomainCacheManager"%>
<%
    if (title == null) {
    title = org.apache.commons.io.FilenameUtils.getName(request.getRequestURI());
}

ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
AdserverDomainCacheManager adserverCacheManager = springContext.getBean(AdserverDomainCacheManager.class);
DomainCacheManager domainCacheManager = springContext.getBean(DomainCacheManager.class);

String createdFmt = "unknown";
String loadedFmt = "unknown";
SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

Date acCreationDate = adserverCacheManager.getCache().getPopulationStartedAt();
if(acCreationDate != null) {
    createdFmt = sdf.format(acCreationDate);
}
Date acLoadedDate = adserverCacheManager.getLastReloadAt();
if(acLoadedDate != null) {
    loadedFmt = sdf.format(acLoadedDate);
}

Date dcCreationDate = domainCacheManager.getCache().getPopulationStartedAt();
Date dcLoadedDate = domainCacheManager.getLastReloadAt();
%>
<html>
<head>
<title><%= title %></title>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/style.css"/>
</head>
<body>
<div style="width: 100%;overflow: hidden;">
	<div style="float:left;"><b><%= title %></b></div>
	<div style="float:right;">
		<a href="/adcache">AdServer Cache</a> created: <%=acCreationDate != null?sdf.format(acCreationDate):"null"%>, loaded: <%=acLoadedDate != null?sdf.format(acLoadedDate):"null"%>
		<br/>
		<a href="/adcache">Domain Cache</a>&nbsp;&nbsp;&nbsp; created: <%=dcCreationDate != null?sdf.format(dcCreationDate):"null"%>, loaded: <%=dcLoadedDate != null?sdf.format(dcLoadedDate):"null"%>
	</div>
</div>
<p>
<% if (!request.getRequestURI().endsWith("/") && !request.getRequestURI().endsWith("index.jsp")) { %>
&nbsp;
<a href="index.jsp">home</a>
<% } %></p>

<% if (_errMsg != null) { %>
<p><font size="+1" color="red"><b><%= _errMsg %></b></font></p>
<% } %>
<% if (_msg != null) { %>
<p><font size="+1"><b><%= _msg %></b></font></p>
<% } %>
