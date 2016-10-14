<%response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");%>
<%@ include file="include/defines.jsp" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@page import="com.adfonic.geo.USState"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.adfonic.domain.cache.ext.AdserverDomainCache" %>
<%@ page import="com.adfonic.domain.cache.AdserverDomainCacheManager" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%!static Map<String,String> userAgents = new TreeMap<String,String>();
static {

userAgents.put("iPhone (iOS 3.1.3)", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16");
userAgents.put("HTC Droid (Android 2.1-update1)", "Mozilla/5.0 (Linux; U; Android 2.1-update1; en-us; Droid Build/ESE81) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
userAgents.put("Cricket MSGM8/A300", "Cricket-A300/1.0 UP.Browser/6.3.0.7 (GUI) MMP/2.0");
userAgents.put("HTC Droid X (Android 2.1-update1)", "Mozilla/5.0 (Linux; U; Android 2.1-update1; en-us; DROIDX Build/VZW) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
userAgents.put("HTC Droid Incredible (Android 2.2)", "Mozilla/5.0 (Linux; U; Android 2.2; nl-nl; Desire_A8181 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
userAgents.put("Fly 2080", "MAUI_WAP_Browser");
userAgents.put("HTC Desire (Android 2.1-update1, en-us)", "Mozilla/5.0 (Linux; U; Android 2.1-update1; en-us; HTC Desire Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
userAgents.put("HTC Incredible/ADR6300 (Android 2.1-update1)", "Mozilla/5.0 (Linux; U; Android 2.1-update1; en-us; ADR6300 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
userAgents.put("Huawei M750", "HUAWEI-M750/001.00 ACS-NetFront/3.2");
userAgents.put("Motorola MB200/Cliq", "Mozilla/5.0 (Linux; U; Android 1.5; en-us; MB200 Build/CUPCAKE) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1");
userAgents.put("Samsung SCH-R350", "sam-r350 UP.Browser/6.2.3.8 (GUI) MMP/2.0");
userAgents.put("Samsung SCH-R450 Messager (Metro PCS)", "sam-r450 UP.Browser/6.2.3.8 (GUI) MMP/2.0");
userAgents.put("Samsung R560", "sam-r560 UP.Browser/6.2.3.8 (GUI) MMP/2.0");
userAgents.put("iPhone (iOS 3.0)", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5A345 Safari/525.20");
userAgents.put("iPhone (iOS 4.0)", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.4 Mobile/8A260b Safari/531.21.10");
userAgents.put("iPod (iOS 3.1.3)", "Mozilla/5.0 (iPod; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16");
userAgents.put("iPod Touch (iOS 3.1.2)", "Mozilla/5.0 (iPod touch; U; CPU iPhone OS 3_1_2 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7D11 Safari/528.18");
userAgents.put("iPod Touch (iOS 3.1.3)", "Mozilla/5.0 (iPod touch; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16");
userAgents.put("iPod Touch (iOS 4.0)", "Mozilla/5.0 (iPod touch; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.4 Mobile/8A260b Safari/531.21.10");
userAgents.put("iPad (iOS 3.2)", "Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10");
userAgents.put("BlackBerry 9500", "BlackBerry9500/4.7.0.109 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/133");
userAgents.put("Motorola RAZR V3", "MOT-V3/0E.40.3CR MIB/2.2.1 Profile/MIDP-2.0 Configuration/CLDC-1.0");
userAgents.put("Cydia (iPhone)", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Cydia/1.0.2953-59 Version/4.0 Mobile/7A341 Safari/528.16");
userAgents.put("NOKIA Lumia 810 (Windows Phone 8)", "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 810)");
userAgents.put("HTC Radar C110e (Windows Phone 7.5)", "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; HTC; Radar C110e)");
userAgents.put("NOKIA Lumia 928 (Windows Phone 8.1)", "Mozilla/5.0 (Windows Phone 8.1; ARM; Trident/7.0; Touch; rv:11; IEMobile/11.0; NOKIA; Lumia 928) like Gecko");

}
static class IpMapping implements Comparable<IpMapping> {
    String ipAddress;
    String operatorName;
    CountryDto country;
    public int compareTo(IpMapping other) {
        int x;

        if (this.operatorName.startsWith("NON-OPERATOR")) {
            if (other.operatorName.startsWith("NON-OPERATOR")) {
                // fall through to country check
            }
            else {
                return 1;
            }
        }
        else if (other.operatorName.startsWith("NON-OPERATOR")) {
            return -1;
        }
        
        if (this.country != null) {
            if (other.country == null) {
                return -1;
            }
            else if ("US".equals(this.country.getIsoCode()) &&
                     !"US".equals(other.country.getIsoCode())) {
                return -1;
            }
            else if (!"US".equals(this.country.getIsoCode()) &&
                     "US".equals(other.country.getIsoCode())) {
                return 1;
            }
            x = this.country.getName().compareTo(other.country.getName());
            if (x != 0) {
                return x;
            }
        }
        if ((x = this.operatorName.compareTo(other.operatorName)) != 0) {
            return x;
        }
        return this.ipAddress.compareTo(other.ipAddress);
    }
}%>
<%
    AdserverDomainCacheManager adserverDomainCacheMgr = appContext.getBean(AdserverDomainCacheManager.class);
AdserverDomainCache adserverDomainCache = adserverDomainCacheMgr.getCache();

DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();
/*
Map<String,String> adSpaces = new TreeMap<String,String>();
for (AdSpaceDto adSpace : adserverDomainCache.getAllAdSpaces()) {
    adSpaces.put(adSpace.getPublication().getName() + "/" +
                 adSpace.getName() + " (id=" +
                 adSpace.getId() + ")",
                 adSpace.getExternalID());
}
*/

Map<String,String> countries = new TreeMap<String,String>();
for (Map.Entry<String,CountryDto> entry : domainCache.getCountriesByIsoCode().entrySet()) {
    countries.put(entry.getValue().getName(), entry.getKey());
}

Map<String,String> languages = new TreeMap<String,String>();
for (Map.Entry<String,LanguageDto> entry : domainCache.getLanguagesByIsoCode().entrySet()) {
    languages.put(entry.getValue().getName(), entry.getKey());
}

List<IpMapping> ipMappings = new ArrayList<IpMapping>();
for (int k = 0; k < ipMappingStrings.length; ) {
    IpMapping ipMapping = new IpMapping();
    ipMapping.ipAddress = ipMappingStrings[k++];
    ipMapping.operatorName = ipMappingStrings[k++];
    ipMapping.country = domainCache.getCountryByIsoCode(ipMappingStrings[k++]);
    ipMappings.add(ipMapping);
}
Collections.sort(ipMappings);

List<IntegrationTypeDto> integrationTypes = new ArrayList<IntegrationTypeDto>();
integrationTypes.addAll(domainCache.getAllIntegrationTypes());
Collections.sort(integrationTypes, new Comparator<IntegrationTypeDto>() {
        public int compare(IntegrationTypeDto a, IntegrationTypeDto b) {
            return a.getSystemName().compareTo(b.getSystemName());
        }
    });

title = "Diagnostic Tool";
%>
<%@ include file="include/top.jsp" %>

<table border=0 cellpadding=2 cellspacing=0>
<form method="get" action="doDiagnostic.jsp">
<input type="hidden" name="run" value="true"/>

<tr>
<th align=right>&nbsp;</th>
<td align=left><input type="submit" value="Go"/></td>
</tr>

<tr>
<th align=right>AdSpace Id or External Id:</th>
<td align=left><input type="text" name="adSpaceIdInput" value=""/></td>
</tr>

<tr>
<th align=right>Medium Override:</th>
<td align=left><select name="medium">
<option value="" selected>(use pub type)</option>
<% for (Medium medium : Medium.values()) { %>
<option value="<%= medium %>"><%= medium %></option>
<% } %>
</select></td>
</tr>

<tr>
<th align=right>Device:</th>
<td align=left><select name="userAgent">
<% for (Map.Entry<String,String> entry : userAgents.entrySet()) { %>
<option value="<%= entry.getValue() %>"><%= entry.getKey() %></option>
<% } %>
</select>
&nbsp;
<b>Custom User-Agent:</b>
<input type="text" size=70 name="userAgentCustom" value=""/>
</td>
</tr>

<tr>
<th align=right>IP Address:</th>
<td align=left><select name="ipAddress">
<% for (IpMapping ipMapping : ipMappings) { %>
<option value="<%= ipMapping.ipAddress %>"><%= ipMapping.country == null ? "unknown" : ipMapping.country.getName() %> - <%= ipMapping.operatorName %> - <%= ipMapping.ipAddress %></option>
<% } %>
</select>
&nbsp;
<b>Custom IP Address:</b>
<input type="text" size=15 name="ipAddressCustom" value=""/> (XXX.XXX.XXX.XXX)
</td>
</tr>

<tr>
<th align=right>Geoloc Overrides:</th>
<td align=left><table border=0 cellpadding=2 cellspacing=0>
<tr>
<th align=right>Country:</th>
<td align=left><select name="country">
<option value="" selected>(derive from IP)</option>
<% for (Map.Entry<String,String> entry : countries.entrySet()) { %>
<option value="<%= entry.getValue() %>"><%= entry.getKey() %> (<%= entry.getValue() %>)</option>
<% } %>
</select></td>
</tr>
<tr>
<th align=right>Zip/PostalCode:</th>
<td align=left><input type="text" name="postalCode" size=6 value=""/></td>
</tr>

<tr>
<th align=right>US State:</th>
<td align=left><select name="state">
<option value="" selected>(derive from IP)</option>
<% for (USState state : USState.values()) { %>
<option value="<%= state %>"><%= state %> (<%= state.getName() %>)</option>
<% } %>
</select></td>
</tr>

<tr>
<th align=right>DMA ID:</th>
<td align=left><input type="text" name="dma" size=6 value=""/></td>
</tr>

<tr>
<th align=right>TimeZone:</th>
<td align=left><input type="text" name="timeZone" size="10" value=""/></td>
</tr>

</table>
</td>
</tr>

<tr>
<th align=right>Gender:</th>
<td align=left><select name="gender">
<option value="" selected>Unspecified</option>
<option value="f">Female</option>
<option value="m">Male</option>
</select></td>
</tr>

<tr>
<th align=right>Age:</th>
<td align=left><table border=0 cellpadding=1 cellspacing=0>
<tr>
<th align=left><input type="radio" name="ageType" value="none" checked/>
Unspecified</th>
<td>&nbsp;</td>
</tr>
<tr>
<th align=left><input type="radio" name="ageType" value="age"/>
Exact Age:</th>
<td><input type="text" name="age" size=4 value="35"/></td>
</tr>
<tr>
<th align=left><input type="radio" name="ageType" value="dob"/>
Date of Birth:</th>
<td><input type="text" size=12 name="dob" value="19740509"/> (format: yyyyMMdd)</td>
</tr>
<tr>
<th align=left><input type="radio" name="ageType" value="range"/>
Age Range:</th>
<td><input type="text" size=4 name="ageRangeLow" value="18"/>
to <input type="text" size=4 name="ageRangeHigh" value="35"/></td>
</tr>
</table></td>
</tr>

<tr>
<th align=right>Coordinates (lat/lon):</th>
<td align=left><input type="text" size=14 name="latitude" value=""/> / <input type="text" size=14 name="longitude" value=""/>(XX.XXXXXXXXXX)</td>
</tr>

<tr>
<th align=right>Language:</th>
<td align=left><select name="language">
<option value="" selected>Unspecified</option>
<% for (Map.Entry<String,String> entry : languages.entrySet()) { %>
<option value="<%= entry.getValue() %>"><%= entry.getKey() %> (<%= entry.getValue() %>)</option>
<% } %>
</select></td>
</tr>

<tr>
<th align=right>Color Scheme:</th>
<td align=left><select name="colorScheme">
<option value="" selected>Unspecified</option>
<% for (AdSpace.ColorScheme colorScheme : AdSpace.ColorScheme.values()) { %>
<option value="<%= colorScheme %>"><%= colorScheme %></option>
<% } %>
</select> (for text ads/icons)</td>
</tr>

<tr>
<th align=right>Test Mode:</th>
<td align=left><select name="testMode">
<option value="0">No</option>
<option value="1" selected>Yes</option>
</select></td>
</tr>

<tr>
<th align=right>TrackingIdentifier:</th>
<td align=left><input type="text" size=20 name="trackingIdentifier" value=""/> (this is used as "r.id")</td>
</tr>

<tr>
<th align=right>DPID:</th>
<td align=left><input type="text" size=20 name="dpid" value=""/> (this is used as "d.dpid")</td>
</tr>

<tr>
<th align=right>IFA:</th>
<td align=left><input type="text" size=20 name="ifa" value=""/> (this is used as "d.ifa")</td>
</tr>

<tr>
<th align=right>Hashed IFA:</th>
<td align=left><input type="text" size=20 name="hifa" value=""/> (this is used as "d.hifa")</td>
</tr>

<tr>
<th align=right>ODIN-1:</th>
<td align=left><input type="text" size=20 name="odin1" value=""/> (this is used as "d.odin-1")</td>
</tr>

<tr>
<th align=right>OpenUDID:</th>
<td align=left><input type="text" size=20 name="openudid" value=""/> (this is used as "d.openudid")</td>
</tr>

<tr>
<th align=right>Android Device ID:</th>
<td align=left><input type="text" size=20 name="android" value=""/> (this is used as "d.android")</td>
</tr>

<tr>
<th align=right>UDID:</th>
<td align=left><input type="text" size=20 name="udid" value=""/> (this is used as "d.udid")</td>
</tr>
<tr>
<th align=right>ATID:</th>
<td align=left><input type="text" size=20 name="atid" value=""/> (this is used as "d.atid")</td>
</tr>
<tr>
<th align=right>ADID:</th>
<td align=left><input type="text" size=20 name="adid" value=""/> (this is used as "d.adid")</td>
</tr>
<tr>
<th align=right>ADID_MD5:</th>
<td align=left><input type="text" size=20 name="adid_md5" value=""/> (this is used as "d.adid_md5")</td>
</tr>
<tr>
<th align=right>GOUID:</th>
<td align=left><input type="text" size=20 name="gouid" value=""/> (this is used as "d.gouid")</td>
</tr>
<tr>
<th align=right>IDFA:</th>
<td align=left><input type="text" size=20 name="idfa" value=""/> (this is used as "d.idfa")</td>
</tr>
<tr>
<th align=right>IDFA_MD5:</th>
<td align=left><input type="text" size=20 name="idfa_md5" value=""/> (this is used as "d.idfa_md5")</td>
</tr>
<tr>
<th align=right>IntegrationType:</th>
<td align=left><select name="integrationType">
<option selected value="">Unspecified</option>
<% for (IntegrationTypeDto integrationType : integrationTypes) { %>
<option value="<%= integrationType.getSystemName() %>"><%= integrationType.getSystemName() %></option>
<% } %>
</select>
&nbsp;
<b>Custom IntegrationType:</b>
<input type="text" size=15 name="integrationTypeCustom" value=""/>
</td>
</tr>

</form>
</table>

<%@ include file="include/bottom.jsp" %>
<%!static final String[] ipMappingStrings = new String[] {
"32.128.108.158", "AT&T Mobility USA", "US",
"32.128.120.132", "AT&T Mobility USA", "US",
"32.128.162.186", "AT&T Mobility USA", "US",
"32.128.213.181", "AT&T Mobility USA", "US",
"32.128.27.190", "AT&T Mobility USA", "US",
"32.128.94.169", "AT&T Mobility USA", "US",
"32.129.135.222", "AT&T Mobility USA", "US",
"32.129.137.59", "AT&T Mobility USA", "US",
"32.129.219.139", "AT&T Mobility USA", "US",
"32.132.254.219", "AT&T Mobility USA", "US",
"32.134.75.3", "AT&T Mobility USA", "US",
"32.135.66.150", "AT&T Mobility USA", "US",
"32.135.70.180", "AT&T Mobility USA", "US",
"32.135.73.152", "AT&T Mobility USA", "US",
"32.136.11.138", "AT&T Mobility USA", "US",
"32.136.130.23", "AT&T Mobility USA", "US",
"32.136.139.234", "AT&T Mobility USA", "US",
"32.136.141.20", "AT&T Mobility USA", "US",
"32.136.171.133", "AT&T Mobility USA", "US",
"32.136.180.157", "AT&T Mobility USA", "US",
"32.136.183.219", "AT&T Mobility USA", "US",
"32.136.201.179", "AT&T Mobility USA", "US",
"32.136.202.210", "AT&T Mobility USA", "US",
"32.136.206.84", "AT&T Mobility USA", "US",
"32.136.27.32", "AT&T Mobility USA", "US",
"32.136.53.139", "AT&T Mobility USA", "US",
"32.137.117.157", "AT&T Mobility USA", "US",
"32.137.192.232", "AT&T Mobility USA", "US",
"32.137.38.86", "AT&T Mobility USA", "US",
"32.137.41.43", "AT&T Mobility USA", "US",
"32.137.49.114", "AT&T Mobility USA", "US",
"32.138.174.91", "AT&T Mobility USA", "US",
"32.138.237.231", "AT&T Mobility USA", "US",
"32.138.65.29", "AT&T Mobility USA", "US",
"32.138.75.117", "AT&T Mobility USA", "US",
"32.139.202.145", "AT&T Mobility USA", "US",
"32.139.66.38", "AT&T Mobility USA", "US",
"32.140.144.147", "AT&T Mobility USA", "US",
"32.140.145.166", "AT&T Mobility USA", "US",
"32.140.160.19", "AT&T Mobility USA", "US",
"32.140.54.111", "AT&T Mobility USA", "US",
"32.140.74.186", "AT&T Mobility USA", "US",
"32.140.98.42", "AT&T Mobility USA", "US",
"32.141.159.159", "AT&T Mobility USA", "US",
"32.141.16.191", "AT&T Mobility USA", "US",
"32.141.21.116", "AT&T Mobility USA", "US",
"32.141.245.79", "AT&T Mobility USA", "US",
"32.141.55.254", "AT&T Mobility USA", "US",
"32.141.70.90", "AT&T Mobility USA", "US",
"32.142.145.37", "AT&T Mobility USA", "US",
"32.142.176.106", "AT&T Mobility USA", "US",
"32.142.189.27", "AT&T Mobility USA", "US",
"32.142.221.121", "AT&T Mobility USA", "US",
"32.142.243.250", "AT&T Mobility USA", "US",
"32.142.45.28", "AT&T Mobility USA", "US",
"32.142.46.190", "AT&T Mobility USA", "US",
"32.142.75.171", "AT&T Mobility USA", "US",
"32.144.108.109", "AT&T Mobility USA", "US",
"32.144.127.133", "AT&T Mobility USA", "US",
"32.144.162.60", "AT&T Mobility USA", "US",
"32.144.183.174", "AT&T Mobility USA", "US",
"32.144.216.86", "AT&T Mobility USA", "US",
"32.144.80.152", "AT&T Mobility USA", "US",
"32.145.179.208", "AT&T Mobility USA", "US",
"32.145.72.243", "AT&T Mobility USA", "US",
"32.145.97.228", "AT&T Mobility USA", "US",
"32.146.113.77", "AT&T Mobility USA", "US",
"32.146.184.16", "AT&T Mobility USA", "US",
"32.147.195.213", "AT&T Mobility USA", "US",
"32.150.114.226", "AT&T Mobility USA", "US",
"32.150.150.171", "AT&T Mobility USA", "US",
"32.150.161.217", "AT&T Mobility USA", "US",
"32.150.208.39", "AT&T Mobility USA", "US",
"32.150.209.122", "AT&T Mobility USA", "US",
"32.150.23.236", "AT&T Mobility USA", "US",
"32.150.85.1", "AT&T Mobility USA", "US",
"32.151.200.74", "AT&T Mobility USA", "US",
"32.151.222.78", "AT&T Mobility USA", "US",
"32.151.40.203", "AT&T Mobility USA", "US",
"32.151.44.146", "AT&T Mobility USA", "US",
"32.152.56.12", "AT&T Mobility USA", "US",
"32.155.134.144", "AT&T Mobility USA", "US",
"32.155.170.7", "AT&T Mobility USA", "US",
"32.155.173.188", "AT&T Mobility USA", "US",
"32.155.205.212", "AT&T Mobility USA", "US",
"32.155.250.195", "AT&T Mobility USA", "US",
"32.155.51.41", "AT&T Mobility USA", "US",
"32.155.65.183", "AT&T Mobility USA", "US",
"32.155.91.210", "AT&T Mobility USA", "US",
"32.156.100.162", "AT&T Mobility USA", "US",
"32.156.126.155", "AT&T Mobility USA", "US",
"32.156.133.15", "AT&T Mobility USA", "US",
"32.156.138.225", "AT&T Mobility USA", "US",
"32.156.191.25", "AT&T Mobility USA", "US",
"32.156.2.62", "AT&T Mobility USA", "US",
"32.156.222.75", "AT&T Mobility USA", "US",
"32.156.225.225", "AT&T Mobility USA", "US",
"32.156.61.44", "AT&T Mobility USA", "US",
"32.156.82.120", "AT&T Mobility USA", "US",
"32.157.174.243", "AT&T Mobility USA", "US",
"32.157.178.100", "AT&T Mobility USA", "US",
"32.158.238.84", "AT&T Mobility USA", "US",
"32.158.34.7", "AT&T Mobility USA", "US",
"32.158.9.70", "AT&T Mobility USA", "US",
"32.159.239.249", "AT&T Mobility USA", "US",
"32.159.96.159", "AT&T Mobility USA", "US",
"32.162.209.191", "AT&T Mobility USA", "US",
"32.162.245.23", "AT&T Mobility USA", "US",
"32.163.179.119", "AT&T Mobility USA", "US",
"32.163.240.231", "AT&T Mobility USA", "US",
"32.163.248.152", "AT&T Mobility USA", "US",
"32.163.98.164", "AT&T Mobility USA", "US",
"32.166.230.240", "AT&T Mobility USA", "US",
"32.169.172.216", "AT&T Mobility USA", "US",
"32.169.236.179", "AT&T Mobility USA", "US",
"32.169.255.242", "AT&T Mobility USA", "US",
"32.169.78.163", "AT&T Mobility USA", "US",
"32.170.114.156", "AT&T Mobility USA", "US",
"32.170.134.166", "AT&T Mobility USA", "US",
"32.173.120.210", "AT&T Mobility USA", "US",
"32.173.202.177", "AT&T Mobility USA", "US",
"32.173.41.249", "AT&T Mobility USA", "US",
"32.173.96.102", "AT&T Mobility USA", "US",
"32.174.54.60", "AT&T Mobility USA", "US",
"32.174.55.214", "AT&T Mobility USA", "US",
"32.175.161.125", "AT&T Mobility USA", "US",
"32.175.202.214", "AT&T Mobility USA", "US",
"71.222.113.140", "Qwest Wireless", "US",
"97.119.44.3", "Qwest Wireless", "US",
"166.132.164.157", "AT&T Mobility USA", "US",
"166.133.19.122", "AT&T Mobility USA", "US",
"166.133.9.220", "AT&T Mobility USA", "US",
"166.134.38.244", "AT&T Mobility USA", "US",
"166.135.135.235", "AT&T Mobility USA", "US",
"166.135.139.79", "AT&T Mobility USA", "US",
"166.135.173.113", "AT&T Mobility USA", "US",
"166.135.182.106", "AT&T Mobility USA", "US",
"166.135.65.75", "AT&T Mobility USA", "US",
"166.135.7.200", "AT&T Mobility USA", "US",
"166.188.123.52", "AT&T Mobility USA", "US",
"166.188.64.233", "AT&T Mobility USA", "US",
"166.188.85.145", "AT&T Mobility USA", "US",
"166.189.165.193", "AT&T Mobility USA", "US",
"166.189.25.224", "AT&T Mobility USA", "US",
"166.190.115.238", "AT&T Mobility USA", "US",
"166.190.115.69", "AT&T Mobility USA", "US",
"166.191.159.222", "AT&T Mobility USA", "US",
"166.191.198.104", "AT&T Mobility USA", "US",
"166.191.41.23", "AT&T Mobility USA", "US",
"166.192.135.199", "AT&T Mobility USA", "US",
"166.192.28.106", "AT&T Mobility USA", "US",
"166.193.169.189", "AT&T Mobility USA", "US",
"166.193.212.246", "AT&T Mobility USA", "US",
"166.193.217.242", "AT&T Mobility USA", "US",
"166.194.123.5", "AT&T Mobility USA", "US",
"166.195.116.104", "AT&T Mobility USA", "US",
"166.195.13.2", "AT&T Mobility USA", "US",
"166.195.139.90", "AT&T Mobility USA", "US",
"166.195.67.136", "AT&T Mobility USA", "US",
"166.195.73.19", "AT&T Mobility USA", "US",
"166.195.91.37", "AT&T Mobility USA", "US",
"166.196.16.214", "AT&T Mobility USA", "US",
"166.197.141.203", "AT&T Mobility USA", "US",
"166.197.37.71", "AT&T Mobility USA", "US",
"166.198.2.68", "AT&T Mobility USA", "US",
"166.198.233.71", "AT&T Mobility USA", "US",
"166.199.36.49", "AT&T Mobility USA", "US",
"208.54.14.107", "T-Mobile US", "US",
"208.54.14.110", "T-Mobile US", "US",
"208.54.14.116", "T-Mobile US", "US",
"208.54.14.40", "T-Mobile US", "US",
"208.54.14.46", "T-Mobile US", "US",
"208.54.14.82", "T-Mobile US", "US",
"208.54.14.84", "T-Mobile US", "US",
"208.54.14.88", "T-Mobile US", "US",
"208.54.14.94", "T-Mobile US", "US",
"208.54.4.58", "T-Mobile US", "US",
"208.54.4.66", "T-Mobile US", "US",
"208.54.4.74", "T-Mobile US", "US",
"208.54.83.53", "T-Mobile US", "US",
"208.54.83.57", "T-Mobile US", "US",
"208.54.83.69", "T-Mobile US", "US",
"208.54.83.73", "T-Mobile US", "US",
"208.54.83.74", "T-Mobile US", "US",
"208.54.83.75", "T-Mobile US", "US",
"208.54.90.56", "T-Mobile US", "US",
"208.54.90.58", "T-Mobile US", "US",
"208.54.90.72", "T-Mobile US", "US",
"208.54.94.108", "T-Mobile US", "US",
"208.54.94.16", "T-Mobile US", "US",
"208.54.94.17", "T-Mobile US", "US",
"208.54.94.49", "T-Mobile US", "US",
"208.54.94.56", "T-Mobile US", "US",
"208.54.94.72", "T-Mobile US", "US",
"208.54.94.86", "T-Mobile US", "US",
"208.54.94.93", "T-Mobile US", "US",
"66.94.27.11", "T-Mobile US", "US",
"66.94.9.52", "T-Mobile US", "US",
"173.66.36.52", "Verizon Wireless", "US",
"68.163.189.234", "Verizon Wireless", "US",
"70.107.232.82", "Verizon Wireless", "US",
"71.108.13.239", "Verizon Wireless", "US",
"71.109.157.137", "Verizon Wireless", "US",
"71.121.238.82", "Verizon Wireless", "US",
"71.167.193.145", "Verizon Wireless", "US",
"71.167.194.187", "Verizon Wireless", "US",
"71.167.217.13", "Verizon Wireless", "US",
"71.173.250.78", "Verizon Wireless", "US",
"71.188.135.21", "Verizon Wireless", "US",
"72.79.227.168", "Verizon Wireless", "US",
"96.246.107.136", "Verizon Wireless", "US",
"98.116.33.121", "Verizon Wireless", "US",
"121.219.151.172", "Telstra Mobile", "AU",
"121.219.59.202", "Telstra Mobile", "AU",
"124.176.150.107", "Telstra Mobile", "AU",
"143.238.217.140", "Telstra Mobile", "AU",
"58.165.11.15", "Telstra Mobile", "AU",
"58.169.36.61", "Telstra Mobile", "AU",
"212.224.184.149", "Mobistar", "BE",
"212.224.186.137", "Mobistar", "BE",
"212.224.186.39", "Mobistar", "BE",
"212.224.190.49", "Mobistar", "BE",
"189.92.11.234", "Claro Brazil", "BR",
"189.92.116.145", "Claro Brazil", "BR",
"189.95.60.0", "Claro Brazil", "BR",
"201.220.232.23", "Telefonica Movil Chile", "CL",
"87.252.132.131", "T-Mobile Croatia", "HR",
"217.77.165.33", "Vodafone Czech Republic", "CZ",
"217.77.165.45", "Vodafone Czech Republic", "CZ",
"217.77.165.46", "Vodafone Czech Republic", "CZ",
"217.77.165.49", "Vodafone Czech Republic", "CZ",
"89.24.5.18", "T-Mobile Czech Republic", "CZ",
"88.195.81.137", "TeliaSonera Finland", "FI",
"62.201.129.226", "Bouygues Telecom", "FR",
"193.253.141.64", "Orange France", "FR",
"193.253.141.80", "Orange France", "FR",
"82.127.67.119", "Orange France", "FR",
"83.197.68.128", "Orange France", "FR",
"86.212.23.170", "Orange France", "FR",
"86.214.55.53", "Orange France", "FR",
"86.215.21.143", "Orange France", "FR",
"86.215.80.80", "Orange France", "FR",
"90.17.53.215", "Orange France", "FR",
"90.43.19.31", "Orange France", "FR",
"90.44.45.169", "Orange France", "FR",
"90.61.175.41", "Orange France", "FR",
"92.130.189.31", "Orange France", "FR",
"80.125.165.180", "SFR", "FR",
"80.125.165.182", "SFR", "FR",
"80.125.165.186", "SFR", "FR",
"80.125.165.187", "SFR", "FR",
"80.125.176.116", "SFR", "FR",
"80.187.100.120", "T-Mobile Germany", "DE",
"80.187.100.175", "T-Mobile Germany", "DE",
"80.187.100.176", "T-Mobile Germany", "DE",
"80.187.100.203", "T-Mobile Germany", "DE",
"80.187.100.66", "T-Mobile Germany", "DE",
"80.187.96.171", "T-Mobile Germany", "DE",
"80.187.96.24", "T-Mobile Germany", "DE",
"203.218.214.117", "PCCW", "HK",
"219.77.46.107", "PCCW", "HK",
"219.79.179.92", "PCCW", "HK",
"219.79.99.111", "PCCW", "HK",
"89.223.156.70", "Vodafone Hungary", "HU",
"123.237.3.250", "Reliance", "IN",
"114.31.134.103", "Vodafone India", "IN",
"114.31.149.204", "Vodafone India", "IN",
"27.107.93.6", "Some Indian Carrier", "IN",
"27.107.102.72", "Some Indian Carrier", "IN",
"27.107.134.93", "Some Indian Carrier", "IN",
"27.107.192.79", "Some Indian Carrier", "IN",
"27.107.236.231", "Some Indian Carrier", "IN",
"125.161.54.175", "Telkom", "ID",
"125.161.68.89", "Telkom", "ID",
"125.162.101.21", "Telkom", "ID",
"125.162.109.109", "Telkom", "ID",
"125.162.96.198", "Telkom", "ID",
"114.121.144.120", "Telkomsel", "ID",
"202.3.213.129", "Telkomsel", "ID",
"86.45.197.202", "Eircom", "IE",
"217.201.224.207", "TIM", "IT",
"217.201.54.63", "TIM", "IT",
"217.202.108.174", "TIM", "IT",
"217.202.160.19", "TIM", "IT",
"217.202.213.190", "TIM", "IT",
"217.203.137.7", "TIM", "IT",
"217.203.165.178", "TIM", "IT",
"83.224.155.97", "Vodafone Italy", "IT",
"83.224.156.15", "Vodafone Italy", "IT",
"83.224.157.34", "Vodafone Italy", "IT",
"83.225.248.44", "Vodafone Italy", "IT",
"83.225.57.234", "Vodafone Italy", "IT",
"91.81.128.142", "Vodafone Italy", "IT",
"79.170.228.205", "Moldcell", "MD",
"79.170.228.85", "Moldcell", "MD",
"79.170.229.47", "Moldcell", "MD",
"84.241.202.203", "T-Mobile Netherlands", "NL",
"94.157.150.175", "T-Mobile Netherlands", "NL",
"94.157.83.19", "T-Mobile Netherlands", "NL",
"62.140.137.125", "Vodafone Netherlands", "NL",
"62.140.137.157", "Vodafone Netherlands", "NL",
"124.197.109.186", "M1", "SG",
"119.234.0.12", "SingTel", "SG",
"119.234.0.27", "SingTel", "SG",
"119.234.0.9", "SingTel", "SG",
"213.151.218.130", "Orange Slovakia", "SK",
"213.151.218.131", "Orange Slovakia", "SK",
"213.151.218.137", "Orange Slovakia", "SK",
"213.151.218.138", "Orange Slovakia", "SK",
"213.151.218.139", "Orange Slovakia", "SK",
"213.151.236.164", "Orange Slovakia", "SK",
"213.229.197.224", "Mobitel Slovenia", "SI",
"194.224.26.130", "Movistar ", "ES",
"195.55.47.60", "Movistar ", "ES",
"213.99.42.126", "Movistar ", "ES",
"213.55.130.205", "Orange Switzerland", "CH",
"213.43.119.122", "Turkcell", "TR",
"213.43.182.230", "Turkcell", "TR",
"149.254.200.236", "T-Mobile UK", "GB",
"186.8.9.211", "Telefonica Moviles Uruguay", "UY",
"217.74.245.89", "MTS", "RU",
"89.214.144.135", "TMN", "PT",
"93.122.133.83", "Orange Romania", "RO",
"93.122.203.196", "Orange Romania", "RO",
"93.122.206.145", "Orange Romania", "RO",
"93.122.255.200", "Orange Romania", "RO",
"79.154.66.174", "Movistar ", "ES",
"79.155.173.66", "Movistar ", "ES",
"80.27.101.65", "Movistar ", "ES",
"80.27.102.206", "Movistar ", "ES",
"80.27.102.215", "Movistar ", "ES",
"80.37.80.203", "Movistar ", "ES",
"80.38.98.58", "Movistar ", "ES",
"80.58.205.101", "Movistar ", "ES",
"81.35.150.75", "Movistar ", "ES",
"81.36.217.99", "Movistar ", "ES",
"81.37.215.53", "Movistar ", "ES",
"81.39.108.247", "Movistar ", "ES",
"83.34.34.194", "Movistar ", "ES",
"83.36.138.93", "Movistar ", "ES",
"83.56.231.6", "Movistar ", "ES",
"88.26.163.192", "Movistar ", "ES",
"88.7.141.212", "Movistar ", "ES",
"88.7.218.98", "Movistar ", "ES",
"77.67.168.149", "Turkcell", "TR",
"77.67.174.238", "Turkcell", "TR",
"77.67.179.102", "Turkcell", "TR",
"86.108.169.107", "Turkcell", "TR",
"92.41.12.211", "3 UK", "GB",
"196.207.39.254", "Vodacom South Africa", "ZA",
"98.65.235.15", "NON-OPERATOR, DSL in Georgetown, KY", "US",
"67.32.178.228", "NON-OPERATOR, DSL in Georgetown, KY", "US",
"69.89.7.27", "NON-OPERATOR, test.adfonic.com", "US",
"95.172.8.2", "NON-OPERATOR, admin.adfonic.com", "GB",
"127.0.0.1", "NON-OPERATOR, 127.0.0.1", "US",
};%>