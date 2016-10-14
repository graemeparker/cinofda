<%@page import="com.adfonic.domain.cache.ext.AdserverDomainCache"%>
<%@page import="com.adfonic.domain.cache.AdserverDomainCacheManager"%>
<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
response.setContentType("text/html");
%>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ include file="include/defines.jsp" %>
<%
Map adfonicParams = new HashMap();

if (StringUtils.isNotBlank(request.getParameter("ipAddressCustom"))) {
    put(adfonicParams, "r.ip", request.getParameter("ipAddressCustom"));
}
else {
    put(adfonicParams, "r.ip", request.getParameter("ipAddress"));
}

put(adfonicParams, "s.test", request.getParameter("testMode"));
put(adfonicParams, "u.gender", request.getParameter("gender"));
put(adfonicParams, "r.type", request.getParameter("medium"));
put(adfonicParams, "o.country", request.getParameter("country"));
put(adfonicParams, "o.postalCode", request.getParameter("postalCode"));
put(adfonicParams, "o.state", request.getParameter("state"));
put(adfonicParams, "o.dma", request.getParameter("dma"));
put(adfonicParams, "u.timezone", request.getParameter("timeZone"));
put(adfonicParams, "u.latitude", request.getParameter("latitude"));
put(adfonicParams, "u.longitude", request.getParameter("longitude"));
put(adfonicParams, "r.id", request.getParameter("trackingIdentifier"));

put(adfonicParams, "d.dpid", request.getParameter("dpid"));
put(adfonicParams, "d.ifa", request.getParameter("ifa"));
put(adfonicParams, "d.hifa", request.getParameter("hifa"));
put(adfonicParams, "d.odin-1", request.getParameter("odin1"));
put(adfonicParams, "d.openudid", request.getParameter("openudid"));
put(adfonicParams, "d.android", request.getParameter("android"));
put(adfonicParams, "d.udid", request.getParameter("udid"));
put(adfonicParams, "d.atid", request.getParameter("atid"));
put(adfonicParams, "d.adid", request.getParameter("adid"));
put(adfonicParams, "d.adid_md5", request.getParameter("adid_md5"));
put(adfonicParams, "d.gouid", request.getParameter("gouid"));
put(adfonicParams, "d.idfa", request.getParameter("idfa"));
put(adfonicParams, "d.idfa_md5", request.getParameter("idfa_md5"));

if ("dob".equals(request.getParameter("ageType"))) {
    put(adfonicParams, "u.dob", request.getParameter("dob"));
} else if ("age".equals(request.getParameter("ageType"))) {
    put(adfonicParams, "u.age", request.getParameter("age"));
} else if ("range".equals(request.getParameter("ageType"))) {
    put(adfonicParams, "u.ageLow", request.getParameter("ageRangeLow"));
    put(adfonicParams, "u.ageHigh", request.getParameter("ageRangeHigh"));
}
put(adfonicParams, "u.lang", request.getParameter("language"));
put(adfonicParams, "t.colorScheme", request.getParameter("colorScheme"));
    
// Additional tags to be used in union with tags pre-assigned to the ad space
put(adfonicParams, "p.tags", request.getParameter("tags"));

// Pass all HTTP request headers as "h.*" parameters
Enumeration names = request.getHeaderNames();
while (names.hasMoreElements()) {
    String name = (String)names.nextElement();
    if ("Cookie".equalsIgnoreCase(name)) {
        continue; // Don't forward cookies
    }
    String value = request.getHeader(name);
    put(adfonicParams, "h." + name.toLowerCase(), value);
}

// Override the User-Agent with the passed-in value
if (StringUtils.isNotBlank(request.getParameter("userAgentCustom"))) {
    put(adfonicParams, "h.user-agent", request.getParameter("userAgentCustom"));
} else {
    put(adfonicParams, "h.user-agent", request.getParameter("userAgent"));
}

// Pass r.client if specified
if (StringUtils.isNotBlank(request.getParameter("integrationTypeCustom"))) {
    put(adfonicParams, "r.client", request.getParameter("integrationTypeCustom"));
} else {
    put(adfonicParams, "r.client", request.getParameter("integrationType"));
}

AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

String adspaceExternalId = request.getParameter("adSpaceId");
String inputAdspaceId = request.getParameter("adSpaceIdInput");
if(inputAdspaceId != null && !inputAdspaceId.trim().equals("")){
    try{
        Long id = Long.parseLong(inputAdspaceId);
        AdSpaceDto adSpaceDto = adserverDomainCache.getAdSpaceById(id);
        adspaceExternalId = adSpaceDto.getExternalID();
    }catch(Exception ex){
        adspaceExternalId = inputAdspaceId;
    }
    
}
//Check here as Controller will return 404 for unknown/invalid adspace ids
if(adserverDomainCache.getAdSpaceByExternalID(adspaceExternalId)==null) {
    out.println("AdSpace '"+adspaceExternalId+ "' not found in adserver cache");
    return;
}

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

buf.append(request.getContextPath()).append("/diag/").append(adspaceExternalId).append("?");
boolean firstParam = true;
for (Iterator iter = adfonicParams.keySet().iterator(); iter.hasNext(); ) {
    String name = (String)iter.next();
    String value = (String)adfonicParams.get(name);
    if (firstParam) {
        firstParam = false;
    } else {
        buf.append('&');
    }
    buf.append(name).append('=');
    try {
        buf.append(URLEncoder.encode(value, "utf-8"));
    } catch (UnsupportedEncodingException e) {
        throw new UnsupportedOperationException(e);
    }
}

title = "Diagnostic Tool Results";
%>
<%@ include file="include/top.jsp" %>
<%
String url = buf.toString();
InputStream inputStream = null;
HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
conn.setConnectTimeout(10000);
conn.setReadTimeout(60000);
conn.setAllowUserInteraction(false);
conn.setRequestMethod("GET");
conn.setDoInput(true);
conn.setDoOutput(false);
conn.setUseCaches(false);
conn.setInstanceFollowRedirects(false);
if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
    // No ad available
    out.println("No ad available, HTTP response code: " + conn.getResponseCode());
    return;
}

inputStream = conn.getInputStream();
try {
    out.print(IOUtils.toString(inputStream));
} finally {
    IOUtils.closeQuietly(inputStream);
}
%>
<%@ include file="include/bottom.jsp" %>
<%!
static String md5(String key) {
    java.security.MessageDigest md;
    try {
        md = java.security.MessageDigest.getInstance("MD5");
    } catch (java.security.GeneralSecurityException e) {
        throw new RuntimeException(e);
    }
    byte[] digest = md.digest(key.getBytes());
    StringBuffer buf = new StringBuffer();
    for (int k = 0; k < digest.length; ++k) {
        buf.append(Integer.toHexString((digest[k] & 0xf0) >> 4));
        buf.append(Integer.toHexString(digest[k] & 0x0f));
    }
    return buf.toString().toUpperCase();
}

static void put(Map<String,String> map, String key, String value) {
    if (StringUtils.isBlank(value)) {
        map.remove(key);
    } else {
        map.put(key, value);
    }
}
%>