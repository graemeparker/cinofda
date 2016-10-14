<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ include file="include/defines.jsp" %>
<%
title = "Adserver Internal Tools";
%>
<%@ include file="include/top.jsp" %>

<p>
<a href="access_log_diagnostic.jsp">Access Log Diagnostic Tool</a>
<br/>
<a href="diagnostic.jsp">Diagnostic Tool</a>
<br/>
<a href="digger.jsp">Digger</a>
<br/>
<a href="impression.jsp">Impression Explorer</a>
<br/>
<a href="ip.jsp">IP Address Mobile/Operator Diagnostic</a>
<br/>
<a href="plugin_fill_rate.jsp">Plugin Fill Rate Tracking</a>
<br/>
<a href="plugins.jsp">Plugin Test Page</a>
<br/>
<a href="retargeting.jsp">Retargeting Data Explorer</a>
<br/>
<a href="rtb_diagnostic.jsp">RTB diagnostic(internal)</a>
<br/>
<a href="stoppages.jsp">Stoppages</a>
<br/>
<a href="statuschange.jsp">Status</a>
<br/>
<a href="ua.jsp">User-Agent Test Page</a>
<br/>
<a href="./geo.jsp">GEO</a>
<br/>
<a href="/adserver/adx">AdX Helper</a>

</p>
<a href="./cache/adspace.xhtml">Adserver Cache</a>
<br/>
<a href="./cache/ecpm.xhtml">ECPM</a>
<br/>
<a href="./monitor/index.jsp">Monitor Creative/Campaigns</a>
<br/>
<a href="./frequencyCap.xhtml">Frequency Cap</a>
<br/>
<a href="/adserver/counters">Counters</a>
<br/>
<a href="/adserver/bidebug">Bid debugger</a>
<br/>
<a href="/adserver/fisher">Bid fisher</a>
<br/>
<a href="/adserver/redis">Redis digger</a>
<br/>
<a href="/adserver/aspike">Aerospike digger</a>
<br/>
<a href="/adserver/adsquare">Adsquare digger</a>
<br/>
<a href="/adserver/factual">Factual digger</a>
<br/>

<%@ include file="include/bottom.jsp" %>
