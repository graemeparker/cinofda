<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.util.HttpUtils" %>
<%@ include file="include/defines.jsp" %>
<%!
static final Pattern mainPattern = Pattern.compile("^([^\\s]+).+GET /ad/([^?]+)\\?([^\\s]+)\\s+HTTP/\\d.\\d (\\d+) \\d+ \\d+ \"([^\"]+)\"");
%>
<%
String accessLogLine = request.getParameter("accessLogLine");
if (accessLogLine != null) {
    accessLogLine = accessLogLine.replaceAll("[\r\n]", "");
}

String httpIpAddress = null;
String adSpaceExternalID = null;
Map<String,String> params = null;
Integer httpStatusCode = null;
String httpUserAgent = null;

boolean process = "true".equals(request.getParameter("process"));
if (process) {
    Matcher matcher = mainPattern.matcher(accessLogLine);
    if (!matcher.find()) {
        _errMsg = "You entered something that doesn't look like an adserver access log line.";
    }
    else {
        httpIpAddress = matcher.group(1);
        adSpaceExternalID = matcher.group(2);
        params = HttpUtils.decodeParams(matcher.group(3));
        httpStatusCode = Integer.parseInt(matcher.group(4));
        httpUserAgent = matcher.group(5);
        if (httpStatusCode != 200) {
            _errMsg = "The access log line you entered resulted in a non-200 HTTP status code: " + httpStatusCode;
        }
        else {
            // If we weren't supplied an explicit h.user-agent, then we'll
            // need to add that parameter ourselves, setting the value to
            // the User-Agent that got logged by tomcat (this is how adserver
            // derives the effective User-Agent when not specified).
            boolean suppliedUserAgent = false;
            for (String key : params.keySet()) {
                if (key.equalsIgnoreCase("h.user-agent")) {
                    suppliedUserAgent = true;
                    break;
                }
            }
            if (!suppliedUserAgent &&
                httpUserAgent != null && !httpUserAgent.equals("")) {
                params.put("h.user-agent", httpUserAgent);
            }

            // If we weren't passed an explicit r.ip, we'll need to add
            // that parameter ourselves, setting the value to the IP
            // address of the request (this is how adserver derives the
            // effective IP address when not specified).
            if (!params.containsKey("r.ip")) {
                params.put("r.ip", httpIpAddress);
            }
        }
    }
}

title = "Access Log Diagnostic Tool";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="post" action="<%= request.getRequestURI() %>">
<input type="hidden" name="process" value="true"/>
<b>Paste a line from adserver access logs here:</b> (no problem if it has line breaks)</br>
<textarea style="font-family:courier;font-size:7pt" name="accessLogLine" rows=6 cols=100><%= accessLogLine == null ? "" : accessLogLine %></textarea>
<br/>
<input type="submit" value="Process"/>
</form>
</p>

<% if (_errMsg == null && process) {
    AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
    AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();
    AdSpaceDto adSpace = adserverDomainCache.getAdSpaceByExternalID(adSpaceExternalID);
    %>
<p>
<b>Parsed Results:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<% if (adSpace == null) { %>
<tr>
<th>AdSpace.externalID</th>
<td colspan=2><a href="adspace_digger.jsp?adSpaceExternalId=<%= adSpaceExternalID %>"><%= adSpaceExternalID %></a></td>
</tr>
<% } else { %>
<tr>
<th>AdSpace</th>
<td colspan=2><a href="adspace_digger.jsp?adSpaceId=<%= adSpace.getId() %>"><%= adSpace.getName() %></a> (id=<%= adSpace.getId() %>)</td>
</tr>
<tr>
<th>Publication</th>
<td colspan=2><a href="publication_digger.jsp?publicationId=<%= adSpace.getPublication().getId() %>"><%= adSpace.getPublication().getName() %></a> (id=<%= adSpace.getPublication().getId() %>)</td>
</tr>
<% } %>
<tr>
<th rowspan="<%= params.size() %>">Parameters</th>
<%
boolean firstParam = true;
for (Map.Entry<String,String> entry : params.entrySet()) {
    if (firstParam) {
        firstParam = false;
    }
    else {
        out.println("<tr>");
    }
    %>
<td><%= entry.getKey() %></td>
<td><%
if ("h.user-agent".equalsIgnoreCase(entry.getKey())) {
    out.print("<a href=\"ua.jsp?ua=" + URLEncoder.encode(entry.getValue()) + "\">" + entry.getValue() + "</a>");
}
else {
    out.print(entry.getValue());
}
%></td>
</tr>
<% } %>
<tr>
<th>HTTP User-Agent</th>
<td colspan=2><%= httpUserAgent %></td>
</tr>
</table>
</p>

<p>
<b>Diagnostic Output</b>
</p>

<%
StringBuilder buf = new StringBuilder();
buf.append(request.getScheme()).append("://");
if (Pattern.compile("^(adfonic\\.net|(lon2|ch1)adserver\\d+(\\.adfonic\\.com)?)$").matcher(request.getServerName()).matches()) {
    // Production servers no longer listen on localhost:80, they only listen on port 8080.
    buf.append("localhost:8080");
} else {
    buf.append(request.getServerName());
    int port = request.getServerPort();
    if (port != 80) {
        buf.append(':')
            .append(String.valueOf(port));
    }
}
    buf.append(request.getContextPath())
        .append("/diag/")
        .append(adSpaceExternalID)
        .append("?")
        .append(HttpUtils.encodeParams(params));
    String url = buf.toString();
    HttpURLConnection conn =
        (HttpURLConnection)new URL(url).openConnection();
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(60000);
    conn.setAllowUserInteraction(false);
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    conn.setDoOutput(false);
    conn.setUseCaches(false);
    conn.setInstanceFollowRedirects(false);
    InputStream inputStream;
    if (conn.getResponseCode() < 400) {
        inputStream = conn.getInputStream();
    } else {
        inputStream = conn.getErrorStream();
    }
    try {
        out.print(IOUtils.toString(inputStream));
    }
    finally {
        IOUtils.closeQuietly(inputStream);
    }
%>
      
<% } %>
      
<%@ include file="include/bottom.jsp" %>
