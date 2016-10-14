<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />

<div class="content verify">
	<form id="form-wrapper">
		<div class="row-title" style="width: 500px">
			<h1 class="title">
				<spring:message code="verify.changemail.completed.head" />
			</h1>
		</div>

		<div class="form-row">
			<div class="ptext">
				<div style="margin-top: 10px">
					<spring:message code="verify.changemail.completed.thanks" />
				</div>
				<div style="margin-top: 10px">
					<spring:message code="verify.changemail.completed.accountenabled" />
				</div>
				<div style="margin-top: 10px">
					<spring:eval expression="@configurationBean.tools2BaseUrl"
						var="tools2BaseUrl" />
					<spring:message code="verify.changemail.completed.login"
						arguments="${tools2BaseUrl}" />
				</div>
			</div>
		</div>
	</form>
</div>
<script type="text/javascript">
	setTimeout('Redirect()', 10000);
	function Redirect() {
		location.href = '<spring:eval expression="@configurationBean.tools2BaseUrl" />';
	}
</script>

<jsp:directive.include file="../includes/bottom.jsp" />
