<%@page import="com.adfonic.dmp.cache.KeyManager"%>
<%@page import="com.adfonic.retargeting.redis.DeviceDataRedisReader"%> 

<%
    response.setHeader("Expires", "0");
    response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.retargeting.redis.DeviceData" %>

<%@ include file="include/defines.jsp" %>
<%

    DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
    DomainCache domainCache = domainCacheMgr.getCache();

    Long deviceIdentifierTypeId = null;
    try {
        deviceIdentifierTypeId = Long.parseLong(request.getParameter("deviceIdentifierTypeId"));
    } catch (Exception ignored) {}

    String deviceIdentifier = request.getParameter("deviceIdentifier");
    DeviceDataRedisReader dmpCache = appContext.getBean(DeviceDataRedisReader.class);

    String json = null;
    Set<Long> eligibleAudienceId = null;
    if (deviceIdentifierTypeId != null && StringUtils.isNotBlank(deviceIdentifier)) {
        json = dmpCache.getDataAsJson(KeyManager.getKey(deviceIdentifier, deviceIdentifierTypeId));
    }

    title = "Retargeting Data Explorer";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="<%= request.getRequestURI() %>">
    <b>Device Identifier:</b>
    <select name="deviceIdentifierTypeId">
        <% for (DeviceIdentifierTypeDto dit : domainCache.getAllDeviceIdentifierTypes()) { %>
        <option <%= deviceIdentifierTypeId != null && deviceIdentifierTypeId.equals(dit.getId()) ? "selected " : "" %>value="<%= dit.getId() %>"><%= dit.getSystemName() %></option>
        <% } %>
    </select>
    <input type="text" size=40 name="deviceIdentifier" value="<%= StringUtils.defaultString(deviceIdentifier) %>"/>
    <input type="submit" value="Go"/>
</form>
</p>

<% if (deviceIdentifierTypeId != null && StringUtils.isNotBlank(deviceIdentifier)) { %>
<% if (json == null) { %>
<p>
    <b>No retargeting data found.</b>
</p>
<% } else { %>
<p>
   <%= json %>
</p>

<% } %>


<% } %>

<%@ include file="include/bottom.jsp" %>
