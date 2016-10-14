<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content confirmation">
	<form:form id="sso-form" method="post" htmlEscape="true">
		<div class="form-header">
			<h1 class="title">
				<spring:message code="forgottenpwd.result.head" />
			</h1>
		</div>
		<div class="form-row">
			<p class="ptext">
				<spring:message code="forgottenpwd.result.emailresent.message" />
			</p>
		</div>
		<div class="submit-row resend">
			<input type="hidden" name="execution" value="${flowExecutionKey}" />

			<input id="resendemail-btn" type="submit" name="_eventId_resend"
				accesskey="l"
				class="ui-button ui-widget ui-state-default ui-corner-all"
				value="<spring:message code="forgottenpwd.result.button"/>"
				tabindex="2" />
		</div>
	</form:form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />