<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ include file="include/defines.jsp" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%
AdSpaceDto adSpace = null;

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

Long adSpaceId = null;
String adSpaceExternalId = request.getParameter("adSpaceExternalId");
if (adSpaceExternalId != null) {
    adSpace = adserverDomainCache.getAdSpaceByExternalID(adSpaceExternalId);
    if (adSpace != null) {
        adSpaceId = adSpace.getId();
    }
} else if (request.getParameter("adSpaceId") != null) {
    try {
        adSpaceId = Long.valueOf(request.getParameter("adSpaceId"));
    } catch (Exception ignored) {}
    if (adSpaceId != null) {
        adSpace = adserverDomainCache.getAdSpaceById(adSpaceId);
    }
}
if (adSpace == null) {
    session.setAttribute(ERROR_MESSAGE, "AdSpace not found");
    response.sendRedirect("digger.jsp");
    return;
}

PublicationDto pub = adSpace.getPublication();

title = "AdSpace Digger: " + pub.getName() + " / " + adSpace.getName() + " (id=" + adSpaceId + ")";
%>
<%@ include file="include/top.jsp" %>

<style type="text/css">
<!--
td {
font-family: Arial;
font-size: 9pt;
}
//-->
</style>

<p>
<b>AdSpace Info:</b> (cached as of domain reload)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Id</th>
<td><%= adSpace.getId() %></td>
</tr>
<tr>
<th>Name</th>
<td><%= adSpace.getName() %></td>
</tr>
<tr>
<th>External Id</th>
<td><%= adSpace.getExternalID() %></td>
</tr>
<tr>
<th>Publication Id</th>
<td><%= pub.getId() %></td>
</tr>
<tr>
<th>Publication Name</th>
<td><a href="publication_digger.jsp?publicationId=<%= pub.getId() %>"><%= pub.getName() %></a></td>
</tr>
<tr>
<th>Unfilled Action</th>
<td><%= adSpace.getUnfilledAction() %></td>
</tr>
<tr>
<th>Color Scheme</th>
<td><%= adSpace.getColorScheme() %></td>
</tr>
<tr>
<th>Formats</th>
<td><%
if (adSpace.getFormatIds().isEmpty()) {
    out.print("NONE");
} else {
    for (Long formatId : adSpace.getFormatIds()) {
        out.println(domainCache.getFormatById(formatId).getSystemName() + "<br/>");
    }
}
%></td>
</tr>
<tr>
<th>Bundle</th>
<td><%= pub.getBundleName() %></td>
</tr>
</table>
</p>

<p>
<b>Eligible Creatives:</b> (cached as of domain reload)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Priority</th>
<th>Id</th>
<th>Name</th>
<th>Campaign</th>
<th>Format</th>
<th colspan=2>Destination</th>
<th>Language</th>
</tr>
<% for (AdspaceWeightedCreative oneAdspaceWeightedCreative : adserverDomainCache.getEligibleCreatives(adSpace.getId())) {
     int priority = oneAdspaceWeightedCreative.getPriority();
     for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
         CreativeDto creative = adserverDomainCache.getCreativeById(oneCreativeId);
     %>
<tr>
<td align=center><%= priority %></td>
<td align=center><%= creative.getId() %></td>
<td><%= creative.getName() %></td>
<td><a href="campaign_digger.jsp?campaignId=<%= creative.getCampaign().getId() %>"><%= creative.getCampaign().getName() %></a></td>
<td align=center><%= domainCache.getFormatById(creative.getFormatId()).getSystemName() %></td>
<td align=center><%= creative.getDestination().getDestinationType() %></td>
<td><%
if (creative.getDestination().getData().startsWith("http")) {
    out.print("<a target=\"_blank\" href=\"");
    out.print(creative.getDestination().getData());
    out.print("\">");
}
out.print(creative.getDestination().getData());
if (creative.getDestination().getData().startsWith("http")) {
    out.print("</a>");
}
%></td>
<td align=center><%= domainCache.getLanguageById(creative.getLanguageId()).getName() %></td>
</tr>
     <% } %>
<% } %>
</table>
</p>

<p><a href="digger.jsp">Back to the Digger Menu</a></p>

<%@ include file="include/bottom.jsp" %>
