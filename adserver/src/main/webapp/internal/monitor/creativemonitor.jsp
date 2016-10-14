<%@page import="java.util.concurrent.atomic.AtomicLong"%>
<%@page import="java.util.concurrent.ConcurrentMap"%>
<%@page import="com.adfonic.adserver.monitor.AdserverMonitor"%>
<%@page import="com.adfonic.domain.cache.ext.AdserverDomainCache"%>
<%@page import="com.adfonic.domain.cache.AdserverDomainCacheManager"%>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="com.quova.data._1.Ipinfo" %>
<%@ page import="com.adfonic.adserver.impl.BasicTargetingEngineImpl" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.geo.*" %>
<%@ page import="com.adfonic.quova.QuovaClient" %>
<%@ page import="com.adfonic.util.IpAddressUtils" %>
<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ include file="../include/defines.jsp" %>


<%
DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
AdserverDomainCacheManager adserverDomainCacheManager = appContext.getBean(AdserverDomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();
AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
AdserverMonitor adserverMonitor = appContext.getBean(AdserverMonitor.class);
String creativeId = request.getParameter("creativeId");
ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> adspaceMap = null;
if(creativeId != null && !creativeId.trim().equals("")){
	Long creativeidLong = Long.parseLong(creativeId);
	adspaceMap = adserverMonitor.getCreativeMonitoringData(creativeidLong);
}
%>
<head>

</head>

<%@ include file="../include/top.jsp" %>

<p>
<h2>Rejection Reasons for Creative : <%= creativeId %></h2>
<%
if(adspaceMap != null){
	
	for(Entry<Long, ConcurrentMap<String, AtomicLong>> oneEntry:adspaceMap.entrySet()){
		%>
		<% if(oneEntry.getKey() == 0) {%>
		<h2>For All Adspaces</h2>
		<% }else{ %>
		<h2>For Adspace ID : <%= oneEntry.getKey() %></h2>
		<% } %>
		
		<table border="1">
		<tr>
		<th>Rejection Reason</th>
		<th>Total Count</th>
		</tr>
		<% 
		for(Entry<String, AtomicLong> oneAdspaceEntry:oneEntry.getValue().entrySet()){
%>
<tr>
<td><%= oneAdspaceEntry.getKey() %></td>
<td><%= oneAdspaceEntry.getValue() %></td>
</tr>
<%			
		}
		%>
		</table>
		<%
	}
}
%>



