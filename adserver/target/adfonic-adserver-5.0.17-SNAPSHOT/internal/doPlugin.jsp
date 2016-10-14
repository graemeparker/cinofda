<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
response.setContentType("text/html");
%>
<%
Map adfonicParams = new HashMap();

if (request.getParameter("ipAddressCustom") != null &&
        !"".equals(request.getParameter("ipAddressCustom"))) {
    put(adfonicParams, "r.ip", request.getParameter("ipAddressCustom"));
}
else{
  put(adfonicParams, "r.ip", request.getParameter("ipAddress"));
}
put(adfonicParams, "s.test", request.getParameter("testMode"));
put(adfonicParams, "u.gender", request.getParameter("gender"));

if (request.getParameter("medium") != null &&
    !"".equals(request.getParameter("medium"))) {
    // TODO: change this
    put(adfonicParams, "r.type", request.getParameter("medium"));
}
if (request.getParameter("country") != null &&
    !"".equals(request.getParameter("country"))) {
    put(adfonicParams, "o.country", request.getParameter("country"));
}
if (request.getParameter("postalCode") != null &&
    !"".equals(request.getParameter("postalCode"))) {
    put(adfonicParams, "o.postalCode", request.getParameter("postalCode"));
}
if (request.getParameter("state") != null &&
    !"".equals(request.getParameter("state"))) {
    put(adfonicParams, "o.state", request.getParameter("state"));
}
if (request.getParameter("dma") != null &&
    !"".equals(request.getParameter("dma"))) {
    put(adfonicParams, "o.dma", request.getParameter("dma"));
}
if (request.getParameter("timeZone") != null &&
    !"".equals(request.getParameter("timeZone"))) {
    put(adfonicParams, "u.timezone", request.getParameter("timeZone"));
}
if (request.getParameter("latitude") != null &&
    !"".equals(request.getParameter("latitude"))) {
    put(adfonicParams, "u.latitude", request.getParameter("latitude"));
}
if (request.getParameter("longitude") != null &&
    !"".equals(request.getParameter("longitude"))) {
    put(adfonicParams, "u.longitude", request.getParameter("longitude"));
}
if (request.getParameter("trackingIdentifier") != null &&
    !"".equals(request.getParameter("trackingIdentifier"))) {
    put(adfonicParams, "r.id", request.getParameter("trackingIdentifier"));
}
if ("dob".equals(request.getParameter("ageType"))) {
    put(adfonicParams, "u.dob", request.getParameter("dob"));
}
else if ("age".equals(request.getParameter("ageType"))) {
    put(adfonicParams, "u.age", request.getParameter("age"));
}
else if ("range".equals(request.getParameter("ageType"))) {
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
    if ("Cookie".equalsIgnoreCase(name) || "Referer".equalsIgnoreCase(name) || "Accept".equalsIgnoreCase(name)) {
        continue; // Don't forward cookies
    }
    String value = request.getHeader(name);
    put(adfonicParams, "h." + name.toLowerCase(), value);
}

// Override the User-Agent with the passed-in value
if (request.getParameter("userAgentCustom") != null &&
        !"".equals(request.getParameter("userAgentCustom"))) {
    put(adfonicParams, "h.user-agent", request.getParameter("userAgentCustom"));
}
else{
    put(adfonicParams, "h.user-agent", request.getParameter("userAgent"));
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
buf.append(request.getContextPath())
    .append("/plugin/")
    .append(request.getParameter("adSpaceExternalID"))
    .append("/")
    .append(request.getParameter("creativeExternalID"))
    .append("?");
boolean firstParam = true;
for (Iterator iter = adfonicParams.keySet().iterator(); iter.hasNext(); ) {
    String name = (String)iter.next();
    String value = (String)adfonicParams.get(name);
    if (firstParam) {
        firstParam = false;
    }
    else {
        buf.append('&');
    }
    buf.append(name)
        .append('=');
    try {
        buf.append(URLEncoder.encode(value, "utf-8"));
    }
    catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
}
String url = buf.toString();
InputStream inputStream = null;
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
if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
    // No ad available
    out.println("No ad available, HTTP response code: " +
                conn.getResponseCode());
    return;
}

inputStream = conn.getInputStream();
try {
    out.print(IOUtils.toString(inputStream));
}
finally {
    try {
        inputStream.close();
    }
    catch (java.io.IOException e) {
        throw new RuntimeException(e);
    }
}
%>
<%!
static String md5(String key) {
    java.security.MessageDigest md;
    try {
        md = java.security.MessageDigest.getInstance("MD5");
    }
    catch (java.security.GeneralSecurityException e) {
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
    if (value == null) {
        map.remove(key);
    }
    else if (value.equals("")) {
        map.remove(key);
    }
    else {
        map.put(key, value);
    }
}
%>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
