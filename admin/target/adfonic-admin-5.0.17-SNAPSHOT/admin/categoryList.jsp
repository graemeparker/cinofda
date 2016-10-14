<%@ page contentType="text/plain" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<f:view>
<c:forEach var="category" items="#{categoryQuery.foundCategories}"><h:outputText value="<i>#{category}</i>" escape="false" /><f:verbatim>
</f:verbatim></c:forEach>
</f:view>
