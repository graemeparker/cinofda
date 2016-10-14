<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="com.adfonic.data.cache.util.PropertiesFactory" %>
<%@ page import="com.adfonic.data.cache.util.Properties" %>

<%
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    PropertiesFactory propertiesFactory = (PropertiesFactory) appContext.getBean("propertiesFactory");
    
    Properties properties = propertiesFactory.getProperties();
%>

<html>

    <head>
        <title>DataCache Details</title>
    </head>

    <body>
        <table border="1">
            <tr>
                <td><b>PARAMETER</b></td>
                <td><b>VALUE</b></td>
            </tr>
            <tr>
                <td>Hostname</td>
                <td><%=propertiesFactory.getHostname()%></td>
            </tr>
            <tr>
                <td>Shard</td>
                <td><%=propertiesFactory.getShard()%></td>
            </tr>
            <tr>
                <td>Location</td>
                <td><%=propertiesFactory.getLocation()%></td>
            </tr>
            <tr>
                <td>load.cacheDelegator.ecpm.compute</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.ecpm.compute"))%></td>
            </tr> 
            <tr>
                <td>load.cacheDelegator.categories</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.categories"))%></td>
            </tr> 
            <tr>
                <td>load.cacheDelegator.creatives</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.creatives"))%></td>
            </tr> 
            <tr>
                <td>load.cacheDelegator.adspaces</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.adspaces"))%></td>
            </tr> 
            <tr>
                <td>use.cacheDelegator.ecpm.compute</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.ecpm.compute"))%></td>
            </tr> 
            <tr>
                <td>use.cacheDelegator.categories</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.categories"))%></td>
            </tr> 
            <tr>
                <td>use.cacheDelegator.creatives</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.creatives"))%></td>
            </tr> 
            <tr>
                <td>use.cacheDelegator.adspaces</td>
                <td><%=Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.adspaces"))%></td>
            </tr> 
        </table>
    </body>
</html>
