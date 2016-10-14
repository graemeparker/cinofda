<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />
<div class="content verify">
	<form id="form-wrapper">
		<div class="row-title">
			<h1 class="title">
				<spring:message code="verify.signup.completed.head" />
			</h1>
		</div>
		<div class="form-row">
			<p class="ptext">
				<spring:message code="verify.signup.completed.confmessage" />
				<br> 
				<a href="<spring:eval expression="@configurationBean.tools2BaseUrl" />">
					<spring:eval expression="@configurationBean.companyName"
						var="companyName" /> <spring:message
						code="verify.signup.completed.loginmessage"
						arguments="${companyName}" 
					/>
				</a>
			</p>
		</div>
	</form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />