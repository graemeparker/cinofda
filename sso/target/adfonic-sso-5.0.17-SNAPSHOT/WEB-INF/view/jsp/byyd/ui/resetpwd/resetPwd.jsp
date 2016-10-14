<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />
<div class="content">
	<form:form id="sso-form" method="post" commandName="verifyModel"
		htmlEscape="true">
		<div class="row-title form-header">
			<h1 class="title" style="margin-left: 140px;">
				<spring:message code="resetpwd.form.head" />
			</h1>
		</div>
		<div class="form-row">
			<label id="pwdLabel" for="password" style="width: 120px;"><spring:message
					code="resetpwd.form.password" /></label>
			<form:password
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				cssErrorClass="error" id="password" path="password" size="50"
				maxlength="32" tabindex="1" autocomplete="false" htmlEscape="true" />
			<div id="pwd-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 40px;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"></span>
			</div>
		</div>
		<div class="form-row">
			<label id="pwdRetypeLabel" for="passwordRetype" style="width: 120px;"><spring:message
					code="resetpwd.form.retype" /></label>
			<form:password
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				cssErrorClass="error" id="passwordRetype" path="passwordRetype"
				size="50" maxlength="32" tabindex="1" autocomplete="false"
				htmlEscape="true" />
		</div>
		<div class="submit-row">
			<input type="hidden" name="execution" value="${flowExecutionKey}" />

			<input id="login-btn" type="submit" name="_eventId_submit"
				accesskey="l"
				class="ui-button ui-widget ui-state-default ui-corner-all"
				value="<spring:message code="resetpwd.form.button"/>" tabindex="2"
				style="margin-left: 140px;" />
		</div>
		<div id="header-msg" class="ui-messages ui-widget">
			<form:errors path="*"
				cssClass="ui-messages-error ui-corner-all error" element="div"
				cssStyle="margin-left:40px;" />
		</div>
	</form:form>
</div>

<script type="text/javascript">
    $(function () {
        ADT.loginValidate.addValidation('password', 'notempty', 'pwd-msg', '<spring:message code="resetpwd.form.error.required" />');
    });
</script>

<jsp:directive.include file="../includes/bottom.jsp" />