<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="com.adfonic.adserver.StoppageManager" %>
<%@ include file="include/defines.jsp" %>
<%
StatusChangeManager statusChangeManager = appContext.getBean(StatusChangeManager.class);

// Use TreeMap so we can sort the view
Map<Long,AdSpace.Status> adspaceStatusChangeMap = new TreeMap<Long,AdSpace.Status>(statusChangeManager.getAdSpaceStatusMap());
Map<Long,Publication.Status> publicationStatusChangeMap = new TreeMap<Long,Publication.Status>(statusChangeManager.getPublicationStatusMap());
Map<Long,Creative.Status> creativeStatusChangeMap = new TreeMap<Long,Creative.Status>(statusChangeManager.getCreativeStatusMap());
Map<Long,Campaign.Status> campaignStatusChangeMap = new TreeMap<Long,Campaign.Status>(statusChangeManager.getCampaignStatusMap());

title = "Status Change";
%>
<%@ include file="include/top.jsp" %>

<table border=0 cellpadding=0 cellspacing=0>
<tr>
<td valign=top>

<p>
<b>Creative Status:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Status</th>
</tr>
<% for (Map.Entry<Long,Creative.Status> entry : creativeStatusChangeMap.entrySet()) {
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</table>
</p>

</td>
<td width=40><img alt="" width=40 height=1 src=""/></td>
<td valign=top>

<p>
<b>Campaign Status:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Status</th>
</tr>
<% for (Map.Entry<Long,Campaign.Status> entry : campaignStatusChangeMap.entrySet()) {
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</table>
</p>

</td>
</tr>


<tr>
<td valign=top>

<p>
<b>Adspace Status:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Status</th>
</tr>
<% for (Map.Entry<Long,AdSpace.Status> entry : adspaceStatusChangeMap.entrySet()) {
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</table>
</p>

</td>
<td width=40><img alt="" width=40 height=1 src=""/></td>
<td valign=top>

<p>
<b>Publication Status:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Id</th>
<th>Status</th>
</tr>
<% for (Map.Entry<Long,Publication.Status> entry : publicationStatusChangeMap.entrySet()) {
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</table>
</p>

</td>
</tr>
</table>

<%@ include file="include/bottom.jsp" %>
