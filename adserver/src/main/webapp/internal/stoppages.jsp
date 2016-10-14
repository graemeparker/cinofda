<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="com.adfonic.adserver.StoppageManager" %>
<%@ include file="include/defines.jsp" %>
<%
StoppageManager stoppageManager = appContext.getBean(StoppageManager.class);

// Use TreeMap so we can sort the view
Map<Long,Stoppage> advertiserStoppages = new TreeMap<Long,Stoppage>(stoppageManager.getAdvertiserStoppages());
Map<Long,Stoppage> campaignStoppages = new TreeMap<Long,Stoppage>(stoppageManager.getCampaignStoppages());

title = "Stoppages";
%>
<%@ include file="include/top.jsp" %>

<table border=0 cellpadding=0 cellspacing=0>
<tr>
<td valign=top>

<p>
<b>Advertiser Stoppages:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Timestamp</th>
<th>ReactivateDate</th>
</tr>
<% for (Map.Entry<Long,Stoppage> entry : advertiserStoppages.entrySet()) {
Stoppage stoppage = entry.getValue();
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= new Date(stoppage.getTimestamp()) %></td>
<td><%= stoppage.getReactivateDate() == null ? "&nbsp;" : new Date(stoppage.getReactivateDate()).toString() %></td>
</tr>
<% } %>
</table>
</p>

</td>
<td width=40><img alt="" width=40 height=1 src=""/></td>
<td valign=top>

<p>
<b>Campaign Stoppages:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Timestamp</th>
<th>ReactivateDate</th>
</tr>
<% for (Map.Entry<Long,Stoppage> entry : campaignStoppages.entrySet()) {
Stoppage stoppage = entry.getValue();
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= new Date(stoppage.getTimestamp()) %></td>
<td><%= stoppage.getReactivateDate() == null ? "&nbsp;" : new Date(stoppage.getReactivateDate()).toString() %></td>
</tr>
<% } %>
</table>
</p>

</td>
</tr>
</table>

<%@ include file="include/bottom.jsp" %>
