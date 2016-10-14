<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.util.HttpRequestContext" %>
<%@ include file="include/defines.jsp" %>
<%
TargetingContextFactory tcf = appContext.getBean(TargetingContextFactory.class);
TargetingContext tc = tcf.createTargetingContext();
PreProcessor preProcessor = appContext.getBean(PreProcessor.class);

final String userAgent = request.getParameter("ua");

String effectiveUA = null;
Map<String,String> deviceProps = null;
ModelDto model = null;
boolean blacklisted = false;
if (StringUtils.isNotBlank(userAgent)) {
    effectiveUA = preProcessor.getModifiedUserAgent(userAgent);
    try {
        preProcessor.checkUserAgentAgainstBlacklist(effectiveUA);
        blacklisted = false;
    }
    catch (BlacklistedException e) {
        blacklisted = true;
    }
    tc.setUserAgent(effectiveUA);
    model = tc.getAttribute(TargetingContext.MODEL, ModelDto.class);
    deviceProps = tc.getAttribute(TargetingContext.DEVICE_PROPERTIES, Map.class);
}

title = "User-Agent Test Page";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="<%= request.getRequestURI() %>">
<b>User-Agent:</b>
<input type="text" size=80 name="ua" value="<%= StringUtils.defaultString(userAgent) %>"/>
<input type="submit" value="Go"/>
</form>
</p>

<% if (StringUtils.isNotBlank(userAgent)) { %>
<p>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th align=right valign=middle>Supplied User-Agent:</th>
<td><%= userAgent %></td>
</tr>
<tr>
<th align=right valign=middle>Effective User-Agent:</th>
<td><%= effectiveUA %></th>
</tr>
<tr>
<th align=right valign=middle>Blacklisted:</th>
<td><%= blacklisted ? "<font color=\"#FF0000\"><b>YES</b></font>" : "no" %></td>
</tr>
<tr>
<th align=right valign=middle>Model:</th>
<td><% if (model == null) { %>NOT FOUND<% } else { %>
id=<%= model.getId() %><br/>
vendor=<%= model.getVendor().getName() %><br/>
name=<%= model.getName() %><br/>
externalID=<%= model.getExternalID() %>
<% } %>
</td>
</tr>
<tr>
<th align=right valign=top>Device Properties:</th>
<td><% if (deviceProps == null) { %>NOT FOUND<% } else { %>
<% for (Map.Entry<String,String> entry : deviceProps.entrySet()) { %>
<%= entry.getKey() %> = <%= entry.getValue() %><br/>
<% } %>
<% } %>
</td>
</tr>
</table>
</p>

<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Browser</th>
<th>Match</th>
<th colspan=2>Header Pattern(s)</th>
</tr>
<%
HttpRequestContext httpRequestContext = new HttpRequestContext() {
        public String getHeader(String header) {
            if ("User-Agent".equalsIgnoreCase(header)) {
                return userAgent;
            } else {
                return null;
            }
        }
    };
List<BrowserDto> sortedBrowsers = new ArrayList<BrowserDto>(appContext.getBean(DomainCacheManager.class).getCache().getAllBrowsers());
Collections.sort(sortedBrowsers, new Comparator<BrowserDto>() {
        public int compare(BrowserDto a, BrowserDto b) {
            return a.getName().compareTo(b.getName());
        }
    });
for (BrowserDto browser : sortedBrowsers) {
int numHeaderPatterns = browser.getHeaderPatternMap().size();
boolean match = browser.isMatch(httpRequestContext);
%>
<tr<%= match ? " bgcolor=\"#00FF00\"" : "" %>>
<td rowspan=<%= numHeaderPatterns %>><%= browser.getName().replaceAll(">", "&gt;").replaceAll("<", "&lt;") %></td>
<td rowspan=<%= numHeaderPatterns %>><%= match ? "<b>YES</b>" : "no" %></td>
<%
boolean firstHeader = true;
for (Map.Entry<String,Pattern> entry : browser.getHeaderPatternMap().entrySet()) {
if (firstHeader) { firstHeader = false; } else { out.println("<tr>"); }
%>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</tr>
<% } %>
</table>

<% } %>

<%@ include file="include/bottom.jsp" %>
