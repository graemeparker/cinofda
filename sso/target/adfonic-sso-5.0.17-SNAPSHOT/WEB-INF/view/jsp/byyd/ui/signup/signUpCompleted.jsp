<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content verify">
	<form:form id="sso-form" method="post" htmlEscape="true">
		<div class="form-row">
			<p class="ptext">
				<spring:message code="signup.result.emailmsg"
					arguments="${signupModel.email}" />
				<br />
				<spring:message code="signup.result.clicklink" />
				<br />
			</p>
		</div>
		<div class="form-row">
			<p class="ptext">
				<spring:message code="signup.result.doesnotarrive" />
				<a
					href="<spring:eval expression="@configurationBean.tools2BaseUrl" /><spring:eval expression="@configurationBean.customerSupportLink" />">
					<spring:message code="signup.result.contactus" />
				</a> 
				<br/>
				<spring:message code="signup.result.wrongemail"
					arguments="approved-advertiser-sign-up" />
			</p>
		</div>
		<div class="submit-row">
			<input type="hidden" name="execution" value="${flowExecutionKey}" />
			<input id="resendemail-btn" type="submit" name="_eventId_resend"
				accesskey="l"
				class="ui-button ui-widget ui-state-default ui-corner-all"
				value="<spring:message code="signup.result.button"/>" tabindex="2" />
		</div>
	</form:form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />