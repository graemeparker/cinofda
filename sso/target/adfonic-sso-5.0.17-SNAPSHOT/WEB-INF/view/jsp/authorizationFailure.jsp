<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="byyd/ui/includes/top.jsp" />
<%@ page isErrorPage="true" %>

<div class="content verify">
    <form id="form-wrapper">
        <div class="row-title" style="width: 500px">
            <h1 class="title">
                ${pageContext.errorData.statusCode} - <spring:message code="screen.blocked.header" />
            </h1>
        </div>
        <div class="form-row">
            <div class="ptext">
                <div style="margin-top: 10px">
                    <h2>
	                    <c:choose>
					        <c:when test="${not empty casAcessDeniedKey}">
					            <p><spring:message code="${casAcessDeniedKey}" /></p>
					        </c:when>
					    </c:choose>
					</h2>
				    <p><%=request.getAttribute("javax.servlet.error.message")%></p>
				    <p><spring:message code="AbstractAccessDecisionManager.accessDenied"/></p>
                </div>
            </div>
        </div>
    </form>
</div>

<jsp:directive.include file="byyd/ui/includes/bottom.jsp" />
