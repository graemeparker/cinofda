<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8"%>

<jsp:directive.include file="../includes/top.jsp" />
<div class="content">
	<form id="form-wrapper">
		<div class="row-title form-header">
			<h1 class="title">
				<spring:message code="resetpwd.result.head" />
			</h1>
		</div>
		<div class="form-row">
			<p class="ptext">
				<spring:eval expression="@configurationBean.tools2BaseUrl"
					var="tools2BaseUrl" />
				<spring:message code="resetpwd.result.loginmessage"
					arguments="${tools2BaseUrl}" />
			</p>
		</div>
	</form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />