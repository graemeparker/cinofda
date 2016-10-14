<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="java.io.*" %>
<%@ page import="com.adfonic.adserver.ImpressionService" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.adserver.KryoManager" %>
<%@ include file="include/defines.jsp" %>
<%
ImpressionService impressionService = appContext.getBean(ImpressionService.class);

String impressionExternalID = request.getParameter("impressionExternalID");
if ("".equals(impressionExternalID)) {
    impressionExternalID = null;
}

Impression impression = null;
if (impressionExternalID != null) {
    impression = impressionService.getImpression(impressionExternalID);
}

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

title = "Impression Explorer";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="<%= request.getRequestURI() %>">
<b>Impression external ID:</b>
<input type="text" size=40 name="impressionExternalID" value="<%= impressionExternalID == null ? "" : impressionExternalID %>"/>
<input type="submit" value="Go"/>
</form>
</p>

<% if (impressionExternalID != null) { %>
<% if (impression == null) { %>
<p>
<b>NOT FOUND: <%= impressionExternalID %></b>
</p>
<% } else {
KryoManager kryoManager = appContext.getBean(KryoManager.class);
int serializedSize = kryoManager.writeObject(impression).length;

AdSpaceDto adSpace = adserverDomainCache.getAdSpaceById(impression.getAdSpaceId());
CreativeDto creative = adserverDomainCache.getCreativeById(impression.getCreativeId());
%>
<p>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th align=right>External ID:</th>
<td align=left><%= impression.getExternalID() %></td>
</tr>
<tr>
<th align=right>Creation Time:</th>
<td align=left><%= impression.getCreationTime() %></td>
</tr>
<tr>
<th align=right>Test Mode:</th>
<td align=left><%= impression.isTestMode() %></td>
</tr>
<tr>
<th align=right>Tracking Identifier (SHA1):</th>
<td align=left><%= impression.getTrackingIdentifier() %></td>
</tr>
<tr>
<th align=right>Device Identifiers:</th>
<td align=left><%
boolean firstDeviceId = true;
for (Map.Entry<Long,String> entry : impression.getDeviceIdentifiers().entrySet()) {
    if (firstDeviceId) {
        firstDeviceId = false;
    } else {
        out.print(", ");
    }
    out.print(domainCache.getDeviceIdentifierTypeById(entry.getKey()).getSystemName());
    out.print('=');
    out.print(entry.getValue());
}
%></td>
</tr>
<tr>
<th align=right>AdSpace ID:</th>
<td align=left><%= adSpace.getId() %></td>
</tr>
<tr>
<th align=right>Publication:</th>
<td align=left>id=<%= adSpace.getPublication().getId() %>, <%= adSpace.getPublication().getName() %></td>
</tr>
<tr>
<th align=right>Publisher ID:</th>
<td align=left><%= adSpace.getPublication().getPublisher().getId() %></td>
</tr>
<% if (creative != null) { %>
<tr>
<th align=right>Creative ID:</th>
<td align=left><%= creative.getId() %></td>
</tr>
<tr>
<th align=right>Campaign:</th>
<td align=left>id=<%= creative.getCampaign().getId() %>, <%= creative.getCampaign().getName() %></td>
</tr>
<tr>
<th align=right>Advertiser ID:</th>
<td align=left><%= creative.getCampaign().getAdvertiser().getId() %></td>
</tr>
<% if (creative.getCampaign().isInstallTrackingEnabled() && creative.getCampaign().getApplicationID() != null) { %>
<tr>
<th align=right>Application ID:</th>
<td align=left><%= creative.getCampaign().getApplicationID() %></td>
</tr>
<% } %>
<% } %>
<tr>
<th align=right>Model ID:</th>
<td align=left><%= impression.getModelId() %><%
if (impression.getModelId() != null) {
    ModelDto model = domainCache.getModelById(impression.getModelId());
    out.print(" (" + model.getVendor().getName() + " " + model.getName() + ")");
}
%></td>
</tr>
<tr>
<th align=right>Country ID:</th>
<td align=left><%= impression.getCountryId() %><%
if (impression.getCountryId() != null) {
    CountryDto country = domainCache.getCountryById(impression.getCountryId());
    out.print(" (" + country.getIsoCode() + " - " + country.getName() + ")");
}
%></td>
</tr>
<tr>
<th align=right>Operator ID:</th>
<td align=left><%= impression.getOperatorId() %><%
if (impression.getOperatorId() != null) {
    OperatorDto operator = domainCache.getOperatorById(impression.getOperatorId());
    out.print(" (" + operator.getName() + ")");
}
%></td>
</tr>
<tr>
<th align=right>Age Range:</th>
<td align=left><%= impression.getAgeRange() %></td>
</tr>
<tr>
<th align=right>Gender:</th>
<td align=left><%= impression.getGender() %></td>
</tr>
<tr>
<th align=right>Geotarget ID:</th>
<td align=left><%= impression.getGeotargetId() %><%
if (impression.getGeotargetId() != null) {
    GeotargetDto geotarget = domainCache.getGeotargetById(impression.getGeotargetId());
    out.print(" (" + geotarget.getType() + ": " + geotarget.getName() + ")");
}
%></td>
</tr>
<tr>
<th align=right>IntegrationType ID:</th>
<td align=left><%= impression.getIntegrationTypeId() %><%
if (impression.getIntegrationTypeId() != null) {
    IntegrationTypeDto integrationType = domainCache.getIntegrationTypeById(impression.getIntegrationTypeId());
    out.print(" (" + integrationType.getSystemName() + ")");
}
%></td>
</tr>
<tr>
<th align=right>Postal Code ID:</th>
<td align=left><%= impression.getPostalCodeId() %></td>
</tr>
<tr>
<th align=right>Proxied Destination Url:</th>
<td align=left><%= impression.getPdDestinationUrl() %></td>
</tr>
<tr>
<th align=right>RTB Bid Price:</th>
<td align=left><%= impression.getRtbBidPrice() %></td>
</tr>
<tr>
<th align=right>RTB Settlement Price:</th>
<td align=left><%= impression.getRtbSettlementPrice() %></td>
</tr>
<tr>
<th align=right>Host:</th>
<td align=left><%= impression.getHost() %></td>
</tr>
<tr>
<th align=right>User TimeZone ID:</th>
<td align=left><%= impression.getUserTimeZoneId() %></td>
</tr>
<tr>
<th align=right>Strategy:</th>
<td align=left><%= impression.getStrategy() %></td>
</tr>
<tr>
<th align=right>Date of Birth:</th>
<td align=left><%= impression.getDateOfBirth() %></td>
</tr>
<tr>
<th align=right>Latitude:</th>
<td align=left><%= impression.getLatitude() %></td>
</tr>
<tr>
<th align=right>Longitude:</th>
<td align=left><%= impression.getLongitude() %></td>
</tr>
<tr>
<th align=right>Location Source:</th>
<td align=left><%= impression.getLocationSource() %></td>
</tr>
<tr>
<th align=right>Kryo Serialized Size:</th>
<td align=left><%= serializedSize %></td>
</tr>
</table>
</p>
<% } %>
<% } %>

<%@ include file="include/bottom.jsp" %>
