<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ include file="include/defines.jsp" %>
<%
title = "Digger";
%>
<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="campaign_digger.jsp">
Campaign by Id: <input type="text" name="campaignId" size=8 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<p>
<form method="get" action="campaign_digger.jsp">
Campaign by Name: <input type="text" name="campaignName" size=20 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<p>
<form method="get" action="publication_digger.jsp">
Publication by Id: <input type="text" name="publicationId" size=8 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<p>
<form method="get" action="publication_digger.jsp">
Publication by Name: <input type="text" name="publicationName" size=20 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<p>
<form method="get" action="adspace_digger.jsp">
AdSpace by Id: <input type="text" name="adSpaceId" size=8 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<p>
<form method="get" action="adspace_digger.jsp">
AdSpace by External Id: <input type="text" name="adSpaceExternalId" size=40 value=""/>
<input type="submit" value="Dig"/>
</form>
</p>

<%@ include file="include/bottom.jsp" %>
