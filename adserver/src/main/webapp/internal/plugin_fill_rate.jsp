<%response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");%>
<%@ include file="include/defines.jsp" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="com.adfonic.adserver.plugin.*" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%
	PluginFillRateTracker tracker = appContext.getBean(PluginFillRateTracker.class);
DecimalFormat countFormat = new DecimalFormat("#,###,###");

if ("start".equals(request.getParameter("cmd"))) {
    tracker.start();
    response.sendRedirect(request.getRequestURI());
    return;
} else if ("stop".equals(request.getParameter("cmd"))) {
    tracker.stop();
    response.sendRedirect(request.getRequestURI());
    return;
}

SortedSet<String> pluginNames = new TreeSet<String>();

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

for (CreativeDto creative : adserverDomainCache.getPluginCreatives()) {
    pluginNames.add(adserverDomainCache.getPluginCreativeInfo(creative).getPluginName());
}

if ("true".equals(request.getParameter("saveAdserverPlugins"))) {
    for (String pluginName : pluginNames) {
        AdserverPluginDto adserverPlugin = domainCache.getAdserverPluginBySystemName(pluginName);
        adserverPlugin.setEnabled("true".equals(request.getParameter(adserverPlugin.getSystemName() + "-enabled")));
        adserverPlugin.setExpectedResponseTimeMillis(Long.parseLong(request.getParameter(adserverPlugin.getSystemName() + "-ert")));
    }
    session.setAttribute(MESSAGE, "Your changes have been saved.");
    response.sendRedirect(request.getRequestURI());
    return;
}

boolean enabled = tracker.isEnabled();

title = "Plugin Fill Rate Tracking";
%>
<%@ include file="include/top.jsp" %>

<p>
<b>Current Tracker Status:</b> <b><%= enabled ? "<font color=green>RUNNING</font>" : "<font color=red>STANDBY</font>" %></b>
&nbsp;&nbsp;
<% if (enabled) { %>
<b><a href="<%= request.getRequestURI() %>?cmd=stop">STOP</a></b>
<% } else { %>
<b><a href="<%= request.getRequestURI() %>?cmd=start">START</a></b>
<% } %>
<% if (tracker.getDateStarted() != null) { %>
<br/>
<b>Date Started:</b> <%= tracker.getDateStarted() %>
<% } %>
<% if (tracker.getDateStopped() != null) { %>
<br/>
<b>Date Stopped:</b> <%= tracker.getDateStopped() %>
<% } %>
</p>

<p>
<b><%= enabled ? "Latest " : "" %>Fill Rate Data:</b><br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Plugin</th>
<% for (PluginFillRateTracker.Outcome outcome : PluginFillRateTracker.Outcome.values()) { %>
<th><%= outcome %></th>
<% } %>
<th>Total</th>
</tr>
<% for (Map.Entry<String, PluginFillRateTracker.PluginStats> entry : tracker.getPluginStatsByPluginName().entrySet()) {
String pluginName = entry.getKey();
PluginFillRateTracker.PluginStats stats = entry.getValue();
long total = stats.getTotalCount();
%>
<tr>
<th><%= pluginName %></th>
<% for (PluginFillRateTracker.Outcome outcome : PluginFillRateTracker.Outcome.values()) {
long count = stats.getCount(outcome);
double rate;
if (total == 0) {
    rate = 0;
} else {
    rate = (double)count / (double)total;
}
%>
<th><%= countFormat.format(count) %> (<%= NumberFormat.getPercentInstance().format(rate) %>)</th>
<% } %>
<th><%= countFormat.format(total) %></th>
</tr>
<% } %>
</table>
</p>

<p>
<b>NOTE:</b> All of the "outcomes" listed above are distinct and mutually exclusive.
</p>

<hr/>

<p>
<b>AdserverPluginDto Control (ONLY for this server)</b>
<br/>
<font color="red"><b>DO NOT...I repeat...DO NOT MESS WITH THIS UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING!!!</b></font>
<br/>
<form method="post" action="<%= request.getRequestURI() %>">
<input type="hidden" name="saveAdserverPlugins" value="true"/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>SystemName</th>
<th>Enabled</th>
<th>ExpectedResponseTime</th>
</tr>
<% for (String pluginName : pluginNames) {
AdserverPluginDto adserverPlugin = domainCache.getAdserverPluginBySystemName(pluginName);
%>
<tr>
<td><%= adserverPlugin.getSystemName() %></td>
<td align=center>
<input name="<%= adserverPlugin.getSystemName() %>-enabled" type="radio" value="true"<% if (adserverPlugin.isEnabled()) { %> checked<% } %>/>Yes
<input name="<%= adserverPlugin.getSystemName() %>-enabled" type="radio" value="false"<% if (!adserverPlugin.isEnabled()) { %> checked<% } %>/>No
</td>
<td><input size=6 type="text" name="<%= adserverPlugin.getSystemName() %>-ert" value="<%= adserverPlugin.getExpectedResponseTimeMillis() %>"/> ms</td>
</tr>
<% } %>
</table>
<input type="submit" value="Save Changes"/>
</form>
</p>

<%@ include file="include/bottom.jsp" %>
