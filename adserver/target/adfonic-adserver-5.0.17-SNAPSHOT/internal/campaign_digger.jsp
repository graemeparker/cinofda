<%@page import="com.adfonic.domain.Segment.DayOfWeek"%>
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
String campaignName = request.getParameter("campaignName");
Long campaignId = null;
if (request.getParameter("campaignId") != null) {
    try {
        campaignId = Long.valueOf(request.getParameter("campaignId"));
    } catch (Exception ignored) {}
} else if (campaignName == null) {
    response.sendRedirect("digger.jsp");
    return;
}

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

List<CreativeDto> creativesFound = new ArrayList<CreativeDto>();
List<AdSpaceDto> eligibleForAdSpaces = new ArrayList<AdSpaceDto>();
CampaignDto campaign = null;
for (CreativeDto creative : adserverDomainCache.getAllCreatives()) {
    if (campaign == null) {
        // We're still looking for the campaign by id or by name
        if ((campaignId != null && campaignId.equals(creative.getCampaign().getId())) || creative.getCampaign().getName().equalsIgnoreCase(campaignName)) {
            // Found it
            campaign = creative.getCampaign();
            if (campaignId == null) {
                campaignId = campaign.getId();
            }
            creativesFound.add(creative);
        }
    } else if (campaignId != null && campaignId.equals(creative.getCampaign().getId())) {
        creativesFound.add(creative);
    }
}

if (campaign == null) {
    session.setAttribute(ERROR_MESSAGE, "Campaign not found");
    response.sendRedirect("digger.jsp");
    return;
}

for (AdSpaceDto adSpace : adserverDomainCache.getAllAdSpaces()) {
	AdspaceWeightedCreative[] eligibleCreatives = adserverDomainCache.getEligibleCreatives(adSpace.getId());
    if (eligibleCreatives == null) {
        continue;
    }
    boolean eligibleForThisAdSpace = false;
    for (AdspaceWeightedCreative oneAdspaceWeightedCreative : eligibleCreatives) {
        for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
        	CreativeDto creative = adserverDomainCache.getCreativeById(oneCreativeId);
            if (campaignId.equals(creative.getCampaign().getId())) {
                eligibleForThisAdSpace = true;
                break;
            }
        }
        if (eligibleForThisAdSpace) {
            break;
        }
    }
    if (eligibleForThisAdSpace) {
        eligibleForAdSpaces.add(adSpace);
    }
}

Collections.sort(eligibleForAdSpaces, new Comparator<AdSpaceDto>() {
        public int compare(AdSpaceDto a1, AdSpaceDto a2) {
            int x = a1.getPublication().getName().compareTo(a2.getPublication().getName());
            if (x == 0) {
                if (a1.getName() == null) {
                    return 1;
                } else if (a2.getName() == null) {
                    return -1;
                }
                x = a1.getName().compareTo(a2.getName());
                if (x == 0) {
                    return new Long(a1.getId()).compareTo(a2.getId());
                }
            }
            return x;
        }
    });

title = "Campaign Digger: " + campaign.getName() + " (id=" + campaignId + ")";
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
<b>Campaign Info:</b> (cached as of domain reload)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Id</th>
<td><%= campaign.getId() %></td>
</tr>
<tr>
<th>Name</th>
<td><%= campaign.getName() %></td>
</tr>
<tr>
<th>External Id</th>
<td><%= campaign.getExternalID() %></td>
</tr>
<tr>
<th>Start Date</th>
<td><%= campaign.getStartDate() %></td>
</tr>
<tr>
<th>End Date</th>
<td><%= campaign.getEndDate() %></td>
</tr>
<tr>
<th>Disable LanguageDto Match</th>
<td><%= campaign.getDisableLanguageMatch() %></td>
</tr>
<tr>
<th>Boost Factor</th>
<td><%= campaign.getBoostFactor() %></td>
</tr>
<tr>
<th>Frequency Cap</th>
<td><%= campaign.getCapImpressions() == null ? "default" : ("" + campaign.getCapImpressions() + " / " + campaign.getCapPeriodSeconds() + " sec") %></td>
</tr>
<tr>
<th>Current Bid</th>
<td><%
CampaignBidDto bid = campaign.getCurrentBid();
if (bid == null) {
    out.print("none");
} else {
    BidType bidType = bid.getBidType();
    out.println("BidType=" + bidType.getName() + ", adAction=" + bidType.getAdAction() + ", quantity=" + bidType.getQuantity() + "<br/>");
    out.println("amount=" + bid.getAmount() + "<br/>");
}
%></td>
</tr>
</table>
</p>

<p>
<b>Creatives for This Campaign:</b> (determined active per domain reload)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Id</th>
<!--
<th>External Id</th>
-->
<th>Name</th>
<th>Format</th>
<th colspan=2>Destination</th>
<th>Language</th>
<th>Priority</th>
<th>Segment</th>
</tr>
<% for (CreativeDto creative : creativesFound) {
SegmentDto segment = creative.getSegment();
%>
<tr>
<td align=center><%= creative.getId() %></td>
<!--
<td><%= creative.getExternalID() %></td>
-->
<td><%= creative.getName() %></td>
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
<td align=center><%= creative.getPriority() %></td>
<td>
id=<%= segment.getId() %><br/>
<% if(segment.isEveryDayEveryHourTargeted()){ %>
DayParting=24X7<br>
<% }else{
	for(DayOfWeek oneDayOfWeek:DayOfWeek.values()){
		%>
		<%=oneDayOfWeek.name() %> = <%= showHoursOfDay(segment.getDayToHourMap().get(oneDayOfWeek.ordinal() + 1)) %>
		<br>
<%	}
%>

<% } %>
countries=<%
if (segment.getCountryIds().isEmpty()) {
    out.print("ALL");
} else {
    boolean first = true;
    for (Long countryId : segment.getCountryIds()) {
        CountryDto country = domainCache.getCountryById(countryId);
        if (first) { first = false; } else { out.print(", "); }
        out.print(country.getName());
    }
} %><br/>
mobile operators=<%
if (segment.getMobileOperatorIds().isEmpty()) {
    out.print("ALL");
} else {
    boolean first = true;
    for (Long operatorId : segment.getMobileOperatorIds()) {
        OperatorDto operator = domainCache.getOperatorById(operatorId);
        if (first) { first = false; } else { out.print(", "); }
        out.print(operator.getName());
    }
} %><br/>
is mobile operators list whitelist = <%= segment.getMobileOperatorListIsWhitelist() %><br/>
isp operators=<%
if (segment.getIspOperatorIds().isEmpty()) {
    out.print("ALL");
} else {
    boolean first = true;
    for (Long operatorId : segment.getIspOperatorIds()) {
        OperatorDto operator = domainCache.getOperatorById(operatorId);
        if (first) { first = false; } else { out.print(", "); }
        out.print(operator.getName());
    }
} %><br/>
is isp operators list whitelist = <%= segment.getIspOperatorListIsWhitelist() %><br/>
genderMix=<%= segment.getGenderMix() %><br/>
ageRange=<%= segment.getMinAge() %> to <%= segment.getMaxAge() %><br/>
vendors=<%
if (segment.getVendorIds().isEmpty()) {
    out.print("ALL");
} else {
    boolean first = true;
    for (Long vendorId : segment.getVendorIds()) {
        //VendorDto vendor = domainCache.getVendorById(vendorId);
        if (first) { first = false; } else { out.print(", "); }
        out.print(vendorId);
    }
} %><br/>
models=<%
if (segment.getModelIds().isEmpty()) {
    out.print("ALL");
} else {
    out.println("<br/>");
    for (Long modelId : segment.getModelIds()) {
        ModelDto model = domainCache.getModelById(modelId);
        out.println(model.getVendor().getName() + " " + model.getName() + "<br/>");
    }
} %><br/>
platforms=<%
if (segment.getPlatformIds().isEmpty()) {
    out.print("ALL");
} else {
    out.println("<br/>");
    for (Long platformId : segment.getPlatformIds()) {
        PlatformDto platform = domainCache.getPlatformById(platformId);
        out.println(platform.getSystemName() + "<br/>");
    }
} %>
</td>
</tr>
<% } %>
</table>
</p>

<p>
<b>Eligible for AdSpaces:</b> (at least one creative)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th colspan=2 align=center>Publication</th>
<th colspan=3 align=center>AdSpace</th>
</tr>
<tr>
<th>Id</th>
<th>Name</th>
<th>Id</th>
<th>Name</th>
<th>External Id</th>
</tr>
<% for (AdSpaceDto adSpace : eligibleForAdSpaces) {
PublicationDto pub = adSpace.getPublication();
%>
<tr>
<td align=center><a href="publication_digger.jsp?publicationId=<%= pub.getId() %>"><%= pub.getId() %></a></td>
<td align=left><%= pub.getName() %></td>
<td align=center><a href="adspace_digger.jsp?adSpaceId=<%= adSpace.getId() %>"><%= adSpace.getId() %></a></td>
<td align=left><%= adSpace.getName() %></td>
<td align=center><%= adSpace.getExternalID() %></td>
</tr>
<% } %>
</table>
</p>

<p><a href="digger.jsp">Back to the Digger Menu</a></p>

<%@ include file="include/bottom.jsp" %>
