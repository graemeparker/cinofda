<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content verify">
	<form id="form-wrapper">
		<div class="row-title" style="width: 500px">
			<h1 class="title">
				<spring:message code="verify.signup.error.head" />
			</h1>
		</div>
		<div class="form-row">
			<div class="ptext">
				<div style="margin-top: 10px">
					<spring:message code="verify.signup.error.mail"
						arguments="${verifyModel.email}" />
				</div>

				<div style="margin-top: 10px">
					<spring:message code="verify.signup.error.checkinbox" />
				</div>

				<spring:eval expression="@configurationBean.tools2BaseUrl"
					var="tools2BaseUrl" />
				<spring:eval expression="@configurationBean.customerSupportLink"
					var="customerSupportLink" />

				<div style="margin-top: 10px">
					<spring:message code="verify.signup.error.resend"
						arguments="${tools2BaseUrl},${customerSupportLink}" />
				</div>

				<div style="margin-top: 10px">
					<spring:message code="verify.signup.error.clicktry"
						arguments="${tools2BaseUrl},${customerSupportLink}" />
				</div>

				<div style="margin-top: 10px">
					<spring:message code="verify.signup.error.wrongemail"
						arguments="sign-up" />
				</div>
			</div>
		</div>
	</form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />

