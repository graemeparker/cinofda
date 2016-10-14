<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content verify">

	<form id="sso-form">
		<div class="row-title form-header">
			<h1 class="title">
				<spring:message code="verify.error.head" />
			</h1>
		</div>
		<div id="header-msg" class="ui-messages ui-widget">
			<form:errors path="*"
				cssClass="ui-messages-error ui-corner-all error" element="div" />
		</div>
		<div class="form-row">
			<p class="ptext">
				<spring:eval expression="@configurationBean.tools2BaseUrl"
					var="tools2BaseUrl" />
				<spring:message code="verify.error.loginmessage"
					arguments="${tools2BaseUrl}" />
				<br>
				<spring:eval expression="@configurationBean.customerSupportLink"
					var="customerSupportLink" />
				<spring:message code="verify.error.supportmessage"
					arguments="${tools2BaseUrl},${customerSupportLink}" />
			</p>
		</div>
	</form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />
