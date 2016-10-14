<%response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");%>
<%@ include file="include/defines.jsp" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%
	String publicationName = request.getParameter("publicationName");
Long publicationId = null;
if (request.getParameter("publicationId") != null) {
    try {
        publicationId = Long.valueOf(request.getParameter("publicationId"));
    } catch (Exception ignored) {}
}
else if (publicationName == null) {
    response.sendRedirect("digger.jsp");
    return;
}

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();

PublicationDto pub = null;
List<AdSpaceDto> adSpaces = new ArrayList<AdSpaceDto>();
for (AdSpaceDto adSpace : adserverDomainCache.getAllAdSpaces()) {
    Long pk = adSpace.getPublication().getId();
    if (pub == null) {
        if ((publicationId != null && publicationId.equals(pk)) || (publicationName != null && publicationName.equals(adSpace.getPublication().getName()))) {
            pub = adSpace.getPublication();
            if (publicationId == null) {
                publicationId = pk;
            }
            publicationName = pub.getName();
            adSpaces.add(adSpace);
        }
    } else if (pk.equals(publicationId)) {
        adSpaces.add(adSpace);
    }
}
if (pub == null) {
    session.setAttribute(ERROR_MESSAGE, "Publication not found");
    response.sendRedirect("digger.jsp");
    return;
}

Collections.sort(adSpaces, new Comparator<AdSpaceDto>() {
        public int compare(AdSpaceDto a1, AdSpaceDto a2) {
            if (a1.getName() == null) {
                return 1;
            } else if (a2.getName() == null) {
                return -1;
            }
            int x = a1.getName().compareTo(a2.getName());
            if (x == 0) {
                return new Long(a1.getId()).compareTo(a2.getId());
            }
            return x;
        }
    });

title = "Publication Digger: " + pub.getName() + " (id=" + publicationId + ")";
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
<b>Publication Info:</b> (cached as of domain reload)
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Id</th>
<td><%= pub.getId() %></td>
</tr>
<tr>
<th>Name</th>
<td><%= pub.getName() %></td>
</tr>
<tr>
<th>External Id</th>
<td><%= pub.getExternalID() %></td>
</tr>
<tr>
<th>Publisher Id</th>
<td><%= pub.getPublisher().getId() %></td>
</tr>
<tr>
<th>Company Id</th>
<td><%= pub.getPublisher().getCompany().getId() %></td>
</tr>
<tr>
<th>Publication Type</th>
<td><%= domainCache.getPublicationTypeById(pub.getPublicationTypeId()).getSystemName() %></td>
</tr>
<tr>
<th>Bundle</th>
<td><%= pub.getBundleName() %></td>
</tr>
<tr>
<th>Install Tracking Disabled</th>
<td><%= pub.isInstallTrackingDisabled() %></td>
</tr>
<tr>
<th>Languages</th>
<td><%
if (pub.getLanguageIds().isEmpty()) {
    out.print("NONE");
} else {
    for (Long languageId : pub.getLanguageIds()) {
        LanguageDto language = domainCache.getLanguageById(languageId);
        out.println(language.getName() + "<br/>");
    }
}
%></td>
</tr>
<tr>
<th>Transparent Network</th>
<td><%
TransparentNetworkDto tn = pub.getTransparentNetwork();
if (tn == null) {
    out.print("NONE");
} else {
    out.println("id=" + tn.getId() + "<br/>");
    out.println("closed=" + tn.isClosed());
}
%></td>
</tr>
</table>
</p>

<p>
<b>AdSpaces:</b>
<br/>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>Id</th>
<th>Name</th>
<th>External Id</th>
<th>Priority</th>
<th># of Creatives</th>
</tr>
<% for (AdSpaceDto adSpace : adSpaces) {
	AdspaceWeightedCreative[] map = adserverDomainCache.getEligibleCreatives(adSpace.getId());
%>
<tr>
<td rowspan=<%= map.length %> align=center><a href="adspace_digger.jsp?adSpaceId=<%= adSpace.getId() %>"><%= adSpace.getId() %></a></td>
<td rowspan=<%= map.length %> align=left><%= adSpace.getName() %></td>
<td rowspan=<%= map.length %> align=center><%= adSpace.getExternalID() %></td>
<%
boolean firstEntry = true;
for (AdspaceWeightedCreative oneAdspaceWeightedCreative : map) {
    if (firstEntry) { firstEntry = false; } else { out.print("</tr><tr>"); }
    out.println("<td align=center>" + oneAdspaceWeightedCreative.getPriority() + "</td><td align=center>" + oneAdspaceWeightedCreative.getCreativeIds().length + "</td>");
}
%>
</tr>
<% } %>
</table>
</p>

<p><a href="digger.jsp">Back to the Digger Menu</a></p>

<%@ include file="include/bottom.jsp" %>
