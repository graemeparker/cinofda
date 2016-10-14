<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content reset-pass">
	<form:form id="sso-form" method="post" commandName="forgottenPwdModel"
		htmlEscape="true">
		<div class="row-title form-header">
			<h1 class="title">
				<spring:message code="forgottenpwd.form.head" />
			</h1>
			<p class="ptext">
				<spring:message code="forgottenpwd.form.message" />
			</p>
		</div>
		<div class="form-row">
			<label id="emailLabel" for="emailname">
				<spring:message code="forgottenpwd.form.email.label" />
			</label>
			<form:input
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				cssErrorClass="error" id="email" path="email" size="50"
				maxlength="255" tabindex="1" autocomplete="false" htmlEscape="true" 
			/>
			<div id="email-msg"
				class="ui-message-error ui-widget ui-corner-all none error">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"></span>
			</div>
		</div>
		<div class="form-row text">
			<p class="ptext">
				<a
					href="<spring:eval expression="@configurationBean.tools2BaseUrl" /><spring:eval expression="@configurationBean.customerSupportLink" />">
					<spring:message code="forgottenpwd.form.contactus" />
				</a>
			</p>
		</div>
		<div class="submit-row">
			<input type="hidden" name="execution" value="${flowExecutionKey}" />

			<input id="login-btn" type="submit" name="_eventId_submit"
				accesskey="l"
				class="ui-button ui-widget ui-state-default ui-corner-all"
				value="<spring:message code="forgottenpwd.form.button"/>"
				tabindex="2" />
		</div>
		<div id="header-msg" class="ui-messages ui-widget">
			<form:errors path="*"
				cssClass="ui-messages-error ui-corner-all error" element="div" />
		</div>
	</form:form>
</div>

<script type="text/javascript">
        $(function () {
            ADT.loginValidate.addValidation('email', 'notempty', 'email-msg', '<spring:message code="forgottenpwd.form.required.email" />');
        });
    </script>

<jsp:directive.include file="../includes/bottom.jsp" />