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
String campaignId = request.getParameter("campaignId");
String adspaceId = request.getParameter("adspaceId");
String action = request.getParameter("action");
if(action != null){
	if(action.equalsIgnoreCase("StartMonitoring")){
		if (creativeId != null && !"".equals(creativeId.trim())) {
			long creativeIdLong = Long.parseLong(creativeId);
			if (adspaceId != null && !"".equals(adspaceId.trim())) {
				long adspaceIdLong = Long.parseLong(adspaceId);
				adserverMonitor.setCreativeAdspaceMonitorning(creativeIdLong, adspaceIdLong);
			}else{
				adserverMonitor.setCreativeMonitorning(creativeIdLong);
			}
		}
		if (campaignId != null && !"".equals(campaignId.trim())) {
			long campaignIdLong = Long.parseLong(campaignId);
			if (adspaceId != null && !"".equals(adspaceId.trim())) {
				long adspaceIdLong = Long.parseLong(adspaceId);
				adserverMonitor.setCampaignAdspaceMonitorning(campaignIdLong, adspaceIdLong);
			}else{
				adserverMonitor.setCampaignMonitorning(campaignIdLong);
			}
		}
	}
	if(action.equalsIgnoreCase("ClearCreativeMonitoring")){
		adserverMonitor.clearAllCreativeMonitoring();
	}
	if(action.equalsIgnoreCase("ClearCampaignMonitoring")){
		adserverMonitor.clearAllCampaignMonitoring();
	}
	if(action.startsWith("ClearOneCreativeMonitoring")){
		String actions[] = action.split(":");
		Long creativeIdLong = Long.parseLong(actions[1]);
		adserverMonitor.clearCreativeMonitoring(creativeIdLong);
	}
	if(action.startsWith("ClearOneCampaignMonitoring")){
		String actions[] = action.split(":");
		Long campaignIdLong = Long.parseLong(actions[1]);
		adserverMonitor.clearCampaignMonitoring(campaignIdLong);
	}
}

String url = request.getContextPath();
String error = null;
if(url.contains("byyd.net") && !url.contains("qa.byyd.net")){
	error= "You must use the direct url to one of the server like http://ch1adserver01:8080/internal/monitor/index.html";
}

%>
<head>

</head>

<%@ include file="../include/top.jsp" %>

<% if(error != null){ %>
<h3 style="color: #FF0000;background-color: #000000"><%= error %></h3>
<% } %>
<form method="get" action="<%= request.getRequestURI() %>">

<p>
<h2>Already monitoring Creatives <input type="submit" name="action" value="ClearCreativeMonitoring"/></h2>
<table>
<%
	for(Long oneCreative:adserverMonitor.getAllCreativesBeingMonitored()) {
%>
<tr><td>
<a href="./creativemonitor.jsp?creativeId=<%= oneCreative %>"><%= oneCreative %></a>
</td>
<td><input type="submit" name="action" value="ClearOneCreativeMonitoring:<%= oneCreative %>"/></td>
</tr>
<%} %>
</table>
<h2>Already monitoring Campaign <input type="submit" name="action" value="ClearCampaignMonitoring"/></h2>
<table>
<%
	for(Long oneCampaign:adserverMonitor.getAllCampaignsBeingMonitored()) {
%>
<tr><td>
<a href="./campaignmonitor.jsp?campaignId=<%= oneCampaign %>"><%= oneCampaign %></a>
</td>
<td><input type="submit" name="action" title="Stop Monitoring" value="ClearOneCampaignMonitoring:<%= oneCampaign %>"/></td>
</tr>
<%} %>
</table>


<table>
<tr>
<th>Creative Id:</th><td><input type="text" size=20 name="creativeId" value=""/></td>
</tr>
<tr>
<th colspan="2" align="center" >or</th>
</tr>
<tr>
<th>Campaign Id:</th><td><input type="text" size=20 name="campaignId" value=""/></td>
</tr>
<tr>
<th colspan="2" align="center" ></th>
</tr>
<tr>
<th colspan="2" align="center" >With or Without</th>
</tr>
<tr>
<th>Adspace Id:</th><td><input type="text" size=20 name="adspaceId" value=""/></td>
</tr>
<tr>
<th></th><td><input type="submit" name="action" value="StartMonitoring"/></td>
</tr>
</table>

</form>



