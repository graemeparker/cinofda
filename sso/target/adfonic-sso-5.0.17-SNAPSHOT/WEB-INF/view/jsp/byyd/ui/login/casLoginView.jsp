<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ Licensed to Jasig under one or more contributor license
  ~ agreements. See the NOTICE file distributed with this work
  ~ for additional information regarding copyright ownership.
  ~ Jasig licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file
  ~ except in compliance with the License.  You may obtain a
  ~ copy of the License at the following location:
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:directive.include file="../includes/top.jsp" />

    <div class="content login-form">
	    <form:form method="post" id="sso-form" commandName="${commandName}" htmlEscape="true">
	     <div class="row-title form-header">
	         <h1 class="title">Log in</h1>
	         <a href="<spring:eval expression="@configurationBean.wordpressBaseUrl" />/sign-up" id="login-signup">New? Sign up!</a>
	     </div>
	        <div id="header-msg" class="ui-messages ui-widget">
	            <c:if test="${not pageContext.request.secure}">
	                <div id="msg" class="ui-messages-error ui-corner-all">
	                    <strong>Non-secure Connection</strong>
	                    <br />
	                    <span>You are currently accessing <spring:eval expression="@configurationBean.companyName" /> Tools over a non-secure connection.  Single Sign On WILL NOT WORK.  In order to have single sign on work, you MUST log in over HTTPS.</span>
	                </div>
	            </c:if>
	            <form:errors path="*" cssClass="ui-messages-error ui-corner-all error" element="div" />
	        </div>
	        <div class="form-row">
	            <label id="usernameLabel" for="username"><spring:message code="screen.welcome.label.netid" /></label>
	            <c:if test="${not empty sessionScope.openIdLocalId}">
	                <strong>${sessionScope.openIdLocalId}</strong>
	                <input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
	            </c:if>
	            <c:if test="${empty sessionScope.openIdLocalId}">
	                <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
	                <form:input cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
	                <div id="user-msg" class="ui-message-error ui-widget ui-corner-all none error">
	                    <span class="ui-message-error-icon"></span>
	                    <span class="ui-message-error-detail"></span>
	                </div>
	            </c:if>
	        </div>
	        <div class="form-row">
	            <label id="passwordLabel" for="password"><spring:message code="screen.welcome.label.password" /></label>
	            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
	            <form:password cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
	            <div id="pass-msg" class="ui-message-error ui-widget ui-corner-all none error">
	                <span class="ui-message-error-icon"></span>
	                <span class="ui-message-error-detail"></span>
	            </div>
	        </div>
	        <div class="keep-row">
	            <spring:message code="screen.welcome.label.rememberMe.accesskey" var="rememberMeAccessKey" />
	            <div id="remember_me" class="ui-chkbox ui-widget">
	                <div class="ui-helper-hidden-accessible">
	                    <input type="checkbox" name="rememberMe" id="rememberMe" value="false" tabindex="3" accesskey="${rememberMeAccessKey}" />
	                </div>
	                <div class="ui-chkbox-box ui-widget ui-corner-all ui-state-default">
	                    <span class="ui-chkbox-icon"></span>
	                </div>
	            </div>
	            <label id="keepLabel" for="rememberMe"><spring:message code="screen.welcome.label.rememberMe" /></label>
	        </div>
	        <div class="submit-row">
	            <input type="hidden" name="lt" value="${loginTicket}" />
	            <input type="hidden" name="execution" value="${flowExecutionKey}" />
	            <input type="hidden" name="_eventId" value="submit" />
	
	            <input id="login-btn" type="submit" name="submit" accesskey="l" class="ui-button ui-widget ui-state-default ui-corner-all" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" />
	            <a href="forgotten-password" class="footnote-link">Lost your login details?</a>
	        </div>
	    </form:form>
    </div>
	
	<script type="text/javascript">
        $(function () {
            ADT.loginValidate.addValidation('username', 'notempty', 'user-msg', '<spring:message code="required.username" />');
            ADT.loginValidate.addValidation('password', 'notempty', 'pass-msg', '<spring:message code="required.password" />');
        });
    </script>
<jsp:directive.include file="../includes/bottom.jsp" />