<%response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="com.adfonic.adserver.rtb.NoBidException"%>
<%@ page import="com.adfonic.adserver.rtb.RtbBidEventAdapter" %>
<%@ page import="com.adfonic.adserver.rtb.RtbLogic" %>
<%@ page import="com.adfonic.adserver.rtb.nativ.ByydResponse"%>
<%@ page import="com.adfonic.adserver.rtb.nativ.ByydImp"%>
<%@ page import="com.adfonic.adserver.rtb.nativ.ByydRequest"%>
<%@ include file="include/defines.jsp" %>
<%!private static class Bid {
    private final com.adfonic.adserver.rtb.nativ.ByydBid bid;
    private final Impression impression;
    private final SelectedCreative selectedCreative;

    private Bid(com.adfonic.adserver.rtb.nativ.ByydBid bid, Impression impression, SelectedCreative selectedCreative) {
        this.bid = bid;
        this.impression = impression;
        this.selectedCreative = selectedCreative;
    }
}
    
private static final class OurListener extends RtbBidEventAdapter {
    private final Map<ByydImp,Object> bidResults = new LinkedHashMap<ByydImp,Object>();
    private String bidRequestRejectedReason;
    private TimeLimit timeLimitExpired;
    private TargetingContext targetingContext;

    @Override
    public void bidRequestRejected(TargetingContext context, ByydRequest bidRequest, String reason) {
        this.targetingContext = context;
        bidRequestRejectedReason = reason;
    }
    
    @Override
    public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, String reason) {
        this.targetingContext = context;
        bidResults.put(imp, reason);
    }

    @Override
    public void bidMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, com.adfonic.adserver.rtb.nativ.ByydBid bid, Impression impression, SelectedCreative selectedCreative) {
        this.targetingContext = context;
        bidResults.put(imp, new Bid(bid, impression, selectedCreative));
    }

    @Override
    public void timeLimitExpired(TargetingContext context, ByydRequest bidRequest, TimeLimit timeLimit) {
        this.targetingContext = context;
        timeLimitExpired = timeLimit;
    }
}%>
<%
    String publisherExternalId = StringUtils.defaultString(request.getParameter("publisherExternalId"));
String jsonBidRequest = StringUtils.defaultString(request.getParameter("jsonBidRequest"));

OurListener listener = new OurListener();
Object bidResponse = null;
Throwable thrown = null;
if ("true".equals(request.getParameter("run"))) {
    RtbLogic rtbLogic = appContext.getBean(RtbLogic.class);
    ObjectMapper objMapper=new ObjectMapper();
    try{
        bidResponse = rtbLogic.getBidResponse(publisherExternalId, objMapper.readValue(jsonBidRequest, ByydRequest.class), request, listener, null, "/rtb/win/");
    } catch (NoBidException nbe){
        thrown = nbe;
    }catch (Throwable e){
        thrown = e;
    }
}

title = "RTB Diagnostic Tool";
%>
<%@ include file="include/top.jsp" %>

<%
    if (thrown != null) {
%>
<p>
<b>Will NOT Bid. Exception trace below:</b>
<table border=1 bgcolor="#D3D3D3" cellpadding=2 cellspacing=0><tr><td>
<pre>
<%=ExceptionUtils.getStackTrace(thrown)%>
</pre>
</td></tr></table>
</p>
<%
    } else if (bidResponse != null) {
%>
<p>
<b>RTB Bid Response:</b>
<br/>
<table border=1 cellpadding=2 cellspacing=0><tr><td>
<pre><%=new ObjectMapper().defaultPrettyPrintingWriter().writeValueAsString(bidResponse)%></pre>
</td></tr></table>
</p>
<p>
<%
    if (listener.timeLimitExpired != null) {
%>
<b>TIME LIMIT EXPIRED: <%=listener.timeLimitExpired.getDuration()%></b>
<br/>
<%
    }
%>
<%
    if (listener.bidRequestRejectedReason != null) {
%>
<font style="color:red"><b>Bid Request Rejected: <%=listener.bidRequestRejectedReason%></b></font>
<br/>
<%
    } else {
%>
<b>Bid Results:</b>
<table border=1 cellpadding=2 cellspacing=0>
<tr>
<th>impid</th>
<th>Result</th>
</tr>
<%
    for (Map.Entry<ByydImp,Object> entry : listener.bidResults.entrySet()) {
%>
<tr>
<td><%= (String)entry.getKey().getImpid() %></td>
<td>
<% if (entry.getValue() instanceof Bid) {
Bid bid = (Bid)entry.getValue();
%>
Bid made, Impression id=<%= bid.impression.getExternalID() %>, selected Creative: id=<%= bid.selectedCreative.getCreative().getId() %>, name=<%= bid.selectedCreative.getCreative().getName() %>
<pre><%= new ObjectMapper().defaultPrettyPrintingWriter().writeValueAsString(bid.bid) %></pre>
<% } else { %>
Bid Not Made: <%= entry.getValue() %>
<% } %>
</td>
</tr>
<% } %>
</table>
<% } %>
</p>

<p>
<b>Derived Attributes:</b>
<br/>
<table border=1 cellpadding=1 cellspacing=0>
<tr>
<th>Attribute</th>
<th>Value</th>
</tr>
<% for (Map.Entry<String,Object> entry : listener.targetingContext.getAttributes().entrySet()) {
    if (entry.getKey().startsWith("_a.")) {
        continue; // skip the "did we derive attribute xxx?" flags
    }
%>
<tr>
<td><%= entry.getKey() %></td>
<td><%= entry.getValue() %></td>
</tr>
<% } %>
</table>
</p>
<% } %>

<table border=0 cellpadding=2 cellspacing=0>
<form method="post" action="<%= request.getRequestURI() %>">
<input type="hidden" name="run" value="true"/>

<tr>
<th align=right>Publisher External ID:</th>
<td align=left><input name="publisherExternalId" size=50 value="<%= publisherExternalId %>"/></td>
</tr>

<tr>
<th align=right>RTB *INTERNAL* BidRequest (in JSON):</th>
<td align=left><textarea name="jsonBidRequest" rows=20 cols=60><%= jsonBidRequest %></textarea></td>
</tr>

<tr>
<th align=right>&nbsp;</th>
<td align=left><input type="submit" value="Go"/></td>
</tr>

</form>
</table>

<%@ include file="include/bottom.jsp" %>
