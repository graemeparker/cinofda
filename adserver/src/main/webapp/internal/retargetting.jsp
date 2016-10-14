<%@page import="com.adfonic.adserver.impl.DeviceLocationTargetingChecks"%>
<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="com.quova.data._1.Ipinfo" %>
<%@ page import="com.adfonic.adserver.impl.BasicTargetingEngineImpl" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.geo.*" %>
<%@ page import="com.adfonic.quova.QuovaClient" %>
<%@ page import="com.adfonic.util.IpAddressUtils" %>
<%@ include file="include/defines.jsp" %>
<%
QuovaClient quovaClient = appContext.getBean(QuovaClient.class);
DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

String ip = request.getParameter("ip");
if ("".equals(ip)) {
    ip = null;
}
if (ip != null) {
    ip = ip.trim();
}

Coordinates coordinates = null;
CountryDto country = null;
MobileIpAddressRangeDto range = null;
OperatorDto operator = null;
USState usState = null;
USZipCode usZipCode = null;
PostalCode ukPostalCode = null;
Dma dma = null;
TimeZone timeZone = null;
List<GeotargetDto> matchingGeotargets = null;
if (ip != null) {
    // Set up the fake targeting context
    TargetingContext context =
        appContext.getBean(TargetingContextFactory.class)
        .createTargetingContext(request, false);
    // Override the IP address
    context.setAttribute(Parameters.IP, ip);

    coordinates = context.getAttribute(TargetingContext.COORDINATES,
                                       Coordinates.class);
    
    country = context.getAttribute(TargetingContext.COUNTRY);
    
    range = context.getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE,
                                 MobileIpAddressRangeDto.class);
    if (range != null) {
        if (range.getOperatorId() != null) {
            operator = context.getDomainCache().getOperatorById(range.getOperatorId());
        }
    }

    if (coordinates != null) {
        Set<Long> geotargetIds = new HashSet<Long>();
        for (GeotargetDto geotarget : domainCache.getAllGeotargets()) {
            geotargetIds.add(geotarget.getId());
        }
        matchingGeotargets = DeviceLocationTargetingChecks.matchGeotargets(context, geotargetIds, domainCache, false); // stopAtFirstMatch
    }

    usState = context.getAttribute(TargetingContext.US_STATE);
    usZipCode = context.getAttribute(TargetingContext.US_ZIP_CODE);
    ukPostalCode = context.getAttribute(TargetingContext.UK_POSTAL_CODE);
    dma = context.getAttribute(TargetingContext.DMA);

    timeZone = context.getAttribute(TargetingContext.TIME_ZONE);
}

title = "IP Address Mobile/OperatorDto Diagnostic";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="<%= request.getRequestURI() %>">
<b>IP Address:</b>
<input type="text" size=20 name="ip" value="<%= ip == null ? "" : ip %>"/>
<input type="submit" value="Go"/>
</form>
</p>

<% if (ip != null) { %>
<p>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th align=right valign=middle>IP Address:</th>
<td><%= ip %> (<%= IpAddressUtils.ipAddressToLong(ip) %>)</td>
</tr>
<tr>
<th align=right valign=middle>Country:</th>
<td><%= country == null ? "NOT RESOLVED" : (country.getName() + " (" + country.getIsoCode() + ")") %></td>
</tr>
<tr>
<th align=right valign=middle>Coordinates:</th>
<td><% if (coordinates == null) {
    out.print("NOT RESOLVED");
}
else {
    out.print(coordinates.getLatitude() + " " +
              coordinates.getLongitude() + " (" +
              "<a target=\"_blank\" href=\"http://maps.google.com/maps?q=" +
              coordinates.getLatitude() + "+" + coordinates.getLongitude() +
              "\">show on map</a>)");
}
%></td>
</tr>
<tr>
<th align=right valign=middle>Operator:</th>
<td><%= operator == null ? "NOT RESOLVED" : (operator.getName() + " (id=" + operator.getId() + ")") %></td>
</tr>
<% if (range != null) { %>
<tr>
<th align=right valign=middle>Mobile IP Address Range:</th>
<td><%= IpAddressUtils.longToIpAddress(range.getStartPoint()) %> - <%= IpAddressUtils.longToIpAddress(range.getEndPoint()) %> (<%= range.getStartPoint() %> - <%= range.getEndPoint() %>)</td>
</tr>
<tr>
<th align=right valign=middle>Mobile IP Address Range Priority:</th>
<td><%= range.getPriority() %></td>
</tr>
<% } %>
<tr>
<th align=right valign=middle>Matching Geotargets:</th>
<td><% if (CollectionUtils.isEmpty(matchingGeotargets)) {
    out.print("NONE");
} else {
    out.println("<table border=1 cellpadding=2 cellspacing=0>");
    out.println("<tr><th>Type</th><th>Name</th></tr>");
    for (GeotargetDto geotarget : matchingGeotargets) {
        out.println("<tr><td>" + geotarget.getType() +
                    "</td><td>" + geotarget.getName() + "</td></tr>");
    }
    out.println("</table>");
} %></td>
</tr>
<tr>
<th align=right valign=middle>US State:</th>
<td><%= usState == null ? "NOT RESOLVED" : usState.name() %></td>
</tr>
<tr>
<th align=right valign=middle>US Zip Code:</th>
<td><%= usZipCode == null ? "NOT RESOLVED" : usZipCode.getZip() %></td>
</tr>
<tr>
<th align=right valign=middle>UK Postal Code:</th>
<td><%= ukPostalCode == null ? "NOT RESOLVED" : ukPostalCode.getPostalCode() %></td>
</tr>
<tr>
<th align=right valign=middle>DMA:</th>
<td><%= dma == null ? "NOT RESOLVED" : (dma.getCode() + " - " + dma.getName()) %></td>
</tr>
<tr>
<th align=right valign=middle>Time Zone:</th>
<td><%= timeZone == null ? "NOT RESOLVED" : timeZone.getID() %></td>
</tr>
</table>
</p>

<p>
<b>Quova GeoDirectory Server Results:</b>
<br/>
<%
try {
    long startTime = System.currentTimeMillis();
    Ipinfo ipInfo = quovaClient.getIpinfo(ip);
    long elapsed = System.currentTimeMillis() - startTime;
    out.println("Request/response time: " + elapsed + "ms<br/>");
    if (ipInfo == null) {
        out.println("No info available<br/>");
    } else {
        dumpIPInfo(ipInfo, out);
    }
} catch (Exception e) {
    out.println("<pre>" + ExceptionUtils.getFullStackTrace(e) + "</pre>");
}
%>                     
<% } %>

<%@ include file="include/bottom.jsp" %>
<%!
private static void dumpIPInfo(Ipinfo info, JspWriter out) throws java.io.IOException {
    out.println("<table border=1 cellpadding=2 cellspacing=0>");
    
    dumpTableRow("IP Type", info.getIpType(), out);
    
    String carrier = null;
    if (info.getNetwork() != null) {
        carrier = info.getNetwork().getCarrier();
    }
    dumpTableRow("Carrier", carrier, out);
    
    BigDecimal latitude = null;
    if (info.getLocation() != null && info.getLocation().getLatitude() != null) {
        latitude = info.getLocation().getLatitude().getValue();
    }
    dumpTableRow("Latitude", latitude, out);
    
    BigDecimal longitude = null;
    if (info.getLocation() != null && info.getLocation().getLongitude() != null) {
        longitude = info.getLocation().getLongitude().getValue();
    }
    dumpTableRow("Longitude", longitude, out);
    
    String stateCode = null;
    if (info.getLocation() != null && info.getLocation().getStateData() != null) {
        stateCode = info.getLocation().getStateData().getStateCode();
    }
    dumpTableRow("State Code", stateCode, out);
    
    String postalCode = null;
    if (info.getLocation() != null && info.getLocation().getCityData() != null) {
        postalCode = info.getLocation().getCityData().getPostalCode();
    }
    dumpTableRow("Postal Code", postalCode, out);
    
    String countryCode = null;
    if (info.getLocation() != null && info.getLocation().getCountryData() != null) {
        countryCode = info.getLocation().getCountryData().getCountryCode();
    }
    dumpTableRow("CountryDto Code", countryCode, out);
    
    BigDecimal timeZone = null;
    if (info.getLocation() != null && info.getLocation().getCityData() != null && info.getLocation().getCityData().getTimeZone() != null) {
        timeZone = info.getLocation().getCityData().getTimeZone().getValue();
    }
    dumpTableRow("Time Zone", timeZone, out);
    
    Integer dma = null;
    if (info.getLocation() != null && info.getLocation().getDma() != null) {
        dma = info.getLocation().getDma().getValue();
    }
    dumpTableRow("DMA", dma, out);
    
    out.println("</table>");
}

private static void dumpTableRow(String name, Object value, JspWriter out) throws java.io.IOException {
    out.print("<tr><th>");
    out.print(name);
    out.print("</th><td>");
    out.print(value == null || "".equals(value) ? "&nbsp;" : value.toString());
    out.println("</td></tr>");
}
%>
