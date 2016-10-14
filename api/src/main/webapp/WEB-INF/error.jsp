<%
boolean isXML=false;
try{
	isXML=((java.util.Map)request.getAttribute(org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("format").equals("xml");
}catch(Exception e){//Any issue? - settle for json
}
String handler="/WEB-INF/error"
  + (isXML? "XML": "JSON")
  +".jsp";
//Integer code=(Integer)request.getAttribute("javax.servlet.error.status_code");
// Just return 500. Above was 500 anyway.
Integer code=Integer.valueOf(500);

//String description=(String)request.getAttribute("javax.servlet.error.message");
//Dont give out the exception to the user
String description="Internal Server Error";
%><jsp:forward page="<%=handler%>">
  <jsp:param name="code" value="<%=code%>"/>
  <jsp:param name="description" value="<%=description%>"/>
</jsp:forward>
