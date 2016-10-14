<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ page contentType="text/html; charset=UTF-8"%>
<jsp:directive.include file="../includes/top.jsp" />
<div class="content sign-up">
	<spring:eval expression="@configurationBean.companyName"
		var="companyName" />
	<form:form id="sso-form" method="post" commandName="signupModel"
		htmlEscape="true">
		<!--  Email  -->
		<div class="row-title form-header">
			<h1 class="title">
				<spring:message code="signup.form.head" />
			</h1>
			<p class="ptext">
				<spring:message code="signup.form.title.message" />
			</p>
		</div>
		<div class="form-row">
			<label id="emailLabel" for="email"><spring:message
					code="signup.form.email.label" /></label>
			<form:input
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="email" path="email" size="50" maxlength="255" tabindex="1"
				autocomplete="false" htmlEscape="true" />
			<div id="email-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="email"
						value="email"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(email)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Password  -->
		<div class="form-row">
			<label id="passwordLabel" for="password"><spring:message
					code="signup.form.password.label" /></label>
			<form:password
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="password" path="password" size="50" maxlength="32" tabindex="2"
				autocomplete="false" htmlEscape="true" />
			<div id="passwordMeter" style="margin-left: 50px;"></div>
			<div id="password-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="password"
						value="password"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(password)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Password Retype  -->
		<div class="form-row">
			<label id="passwordRetypeLabel" for="passwordRetype"><spring:message
					code="signup.form.repeatpwd.label" /></label>
			<form:password
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="passwordRetype" path="passwordRetype" size="50" maxlength="32"
				tabindex="3" autocomplete="false" htmlEscape="true" />
			<div id="passwordRetype-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="passwordRetype"
						value="passwordRetype"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(passwordRetype)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  First Name  -->
		<div class="form-row">
			<label id="firstNameLabel" for="firstName"><spring:message
					code="signup.form.firstname.label" /></label>
			<form:input
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="firstName" path="firstName" size="50" maxlength="255"
				tabindex="4" autocomplete="false" htmlEscape="true" />
			<div id="firstName-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="firstName"
						value="firstName"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(firstName)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Last Name  -->
		<div class="form-row">
			<label id="lastNameLabel" for="lastName"><spring:message
					code="signup.form.lastname.label" /></label>
			<form:input
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="lastName" path="lastName" size="50" maxlength="255" tabindex="5"
				autocomplete="false" htmlEscape="true" />
			<div id="lastName-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="lastName"
						value="lastName"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(lastName)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Company Name  -->
		<div class="form-row">
			<label id="companyLabel" for="company"><spring:message
					code="signup.form.companyname.label" /></label>
			<form:input
				cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
				id="company" path="company" size="50" maxlength="255" tabindex="6"
				autocomplete="false" htmlEscape="true" />
			<div id="company-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="company"
						value="company"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(company)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Country Name  -->
		<div class="form-row drop-down">
			<label id="countryLabel" for="country"><spring:message
					code="signup.form.country.label" /></label>
			<spring:message code="signup.form.country.item.pleaseselect.value"
				var="pleaseSelectValue" />
			<spring:message code="signup.form.country.item.pleaseselect.label"
				var="pleaseSelectLabel" />
			<spring:message code="signup.form.country.item.gb.value"
				var="gbValue" />
			<spring:message code="signup.form.country.item.gb.label"
				var="gbLabel" />
			<spring:message code="signup.form.country.item.us.value"
				var="usValue" />
			<spring:message code="signup.form.country.item.us.label"
				var="usLabel" />
			<form:select path="country" cssClass="selection" tabindex="7">
				<form:option value="${pleaseSelectValue}"
					label="${pleaseSelectLabel}" />
				<form:option value="${gbValue}" label="${gbLabel}" />
				<form:option value="${usValue}" label="${usLabel}" />
				<form:options items="${countryList}" itemLabel="name"
					itemValue="isoCode" />
			</form:select>
			<div id="country-msg"
				class="ui-message-error ui-widget ui-corner-all none error"
				style="margin-left: 180px; width: 200px; text-align: right;">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="country"
						value="country"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(country)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  TimeZone  -->
		<div class="form-row drop-down">
			<label id="timezoneLabel" for="timezone"><spring:message
					code="signup.form.timezone.label" /></label>
			<form:select path="timezone" cssClass="selection" tabindex="8">
				<form:options items="${timezonesList}" itemLabel="description"
					itemValue="id" />
			</form:select>
		</div>
		
		<!--  Default Currency  -->
        <div class="form-row drop-down">
            <label id="defaultCurrencyLabel" for="defaultCurrency"><spring:message code="signup.form.defaultcurrency.label" /></label>
            <form:select path="defaultCurrency"  cssClass="selection" tabindex="8">
                <form:options items="${defaultCurrencyCodesList}" itemLabel="toCurrencyCode" itemValue="id" />
            </form:select>
        </div>

		<!--  AccountType  -->
		<div class="form-row drop-down">
			<label><spring:message code="signup.form.intent.label"/></label>
			<div class="select-box">
				<div>
					<form:radiobutton path="accountType" value="PUBLISHER" tabindex="9" />
					<span><spring:message code="signup.form.accounttype.label.publisher" /></span>
				</div>
				<div>
					<form:radiobutton path="accountType" value="ADVERTISER" />
					<span><spring:message
							code="signup.form.accounttype.label.advertiser" /></span>
				</div>
				<a href="#" onclick="openPopUp();return false;"> <spring:message
						code="signup.form.accounttype.label.helpme" />
				</a>
			</div>
			<div id="accountType-msg"
				class="ui-message-error ui-widget ui-corner-all none error">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="accountType"
						value="accountType"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(accountType)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Where did you hear about us  -->
		<div class="form-row drop-down">
			<label id="hearAboutLabel" for="hearAbout"><spring:message
					code="signup.form.hearabout.label" /></label>

			<spring:message
				code="signup.form.hearabout.option.pleaseselect.label"
				var="hearPleaseSelectValue" />
			<form:select path="hearAbout" cssClass="selection"
				onchange="showReferralTypeOther(this.value,true)" tabindex="10">
				<form:option value="" label="${hearPleaseSelectValue}" />
				<form:options items="${hearAboutPlacesList}" itemLabel="label"
					itemValue="value" />
			</form:select>
			<div id="hearAbout-msg"
				class="ui-message-error ui-widget ui-corner-all none error">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set var="hearAbout"
						value="hearAbout"></c:set> <c:forEach var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(hearAbout)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Other hear about us (hide and unhide) -->
		<div id="hearAboutOtherDiv1" class="form-row drop-down">
			<div id="hearAboutOtherDiv2">
				<label id="hearAboutOtherLabel" for="hearAboutOther"><spring:message
						code="signup.form.hearabout.telluswhere.label" /></label>
				<form:input
					cssClass="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all"
					id="hearAboutOther" path="hearAboutOther" size="50" maxlength="255"
					tabindex="11" autocomplete="false" htmlEscape="true" />
				<div id="hearAboutOther-msg"
					class="ui-message-error ui-widget ui-corner-all none error">
					<span class="ui-message-error-icon"></span> <span
						class="ui-message-error-detail"> <c:set
							var="hearAboutOther" value="hearAboutOther"></c:set> <c:forEach
							var="message"
							items="${flowRequestContext.messageContext.getMessagesBySource(hearAboutOther)}">
							<span>${message.text}</span>
						</c:forEach>
					</span>
				</div>
			</div>
		</div>

		<!--  Keep me informed  -->
		<div class="form-row drop-down">
			<label id="keepMeInformedLabel" for="keepMeInformed"><spring:message
					code="signup.form.keepmeinformed.label" /></label>
			<div class="select-box">
				<form:checkbox path="keepMeInformed" value="true" tabindex="12" />
				&nbsp;
				<spring:message code="signup.form.keepmeinformed.label.message"
					arguments="${companyName}" />
			</div>
		</div>

		<!--  Captcha  -->
		<script>
			var RecaptchaOptions = {
				theme : 'clean',
				custom_theme_widget: 'recaptcha_widget',
				tabindex : 13
			};
		</script>
		<div class="form-row drop-down">
			<div id="recaptcha_widget_div">
				<div id="recaptcha_imaage"></div>
				<div class="recaptcha_only_if_incorrect_sol" style="color: red">Incorrect
					please try again</div>
				<span class="recaptcha_only_if_image">Enter the words above:</span>
				<span class="recaptcha_only_if_audio">Enter the numbers you
					hear:</span>
				<div>
					<a href="javascript:Recaptcha.reload()">Get another CAPTCHA</a>
				</div>
				<div class="recaptcha_only_if_image">
					<a href="javascript:Recaptcha.switch_type('audio')">Get an
						audio CAPTCHA</a>
				</div>
				<div class="recaptcha_only_if_audio">
					<a href="javascript:Recaptcha.switch_type('image')">Get an
						image CAPTCHA</a>
				</div>
				<div>
					<a href="javascript:Recaptcha.showhelp()">Help</a>
				</div>
			</div>
			<script type="text/javascript"
				src="${pageContext.request.scheme}://www.google.com/recaptcha/api/challenge?k=<spring:eval expression="@configurationBean.recaptchaPublickey" />">
		    </script>
			<noscript>
				<iframe
					src="${pageContext.request.scheme}://www.google.com/recaptcha/api/noscript?k=<spring:eval expression="@configurationBean.recaptchaPublickey" />"
					height="300" width="500"></iframe>
				<br />
				<form:textarea id="captchaChallenge" path="captchaChallenge"
					rows="3" cols="30" />
				<form:hidden id="captchaUserResponse" path="captchaUserResponse" />
			</noscript>
			<div id="captchaChallenge-msg"
				class="ui-message-error ui-widget ui-corner-all none error">
				<span class="ui-message-error-icon"></span> <span
					class="ui-message-error-detail"> <c:set
						var="captchaChallenge" value="captchaChallenge"></c:set> <c:forEach
						var="message"
						items="${flowRequestContext.messageContext.getMessagesBySource(captchaChallenge)}">
						<span>${message.text}</span>
					</c:forEach>
				</span>
			</div>
		</div>

		<!--  Terms & Conditions  -->
		<div class="form-row drop-down confirmation-label">
			<spring:eval expression="@configurationBean.wordpressBaseUrl" var="wordpressUrl" />
			<spring:eval expression="@configurationBean.termsCondsLink" var="termsCondsLink" />
			<spring:eval expression="@configurationBean.privacyPolicyLink" var="privPolLink" />
			<p>
				<spring:message code="signup.form.confirmation.label"
					arguments="${companyName},${wordpressUrl},${termsCondsLink},${wordpressUrl},${privPolLink}" />
			</p>
		</div>

		<!--  Submit  -->
		<div class="submit-row drop-down">
			<input type="hidden" name="execution" value="${flowExecutionKey}" />

			<input id="login-btn" type="submit" name="_eventId_submit"
				accesskey="l"
				class="ui-button ui-widget ui-state-default ui-corner-all"
				value="<spring:message code="signup.form.button"/>" tabindex="14"
			/>
		</div>
	</form:form>
<script type="text/javascript">
      $(function () {
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(email)}">jQuery('#email-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(password)}">jQuery('#password-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(passwordRetype)}">jQuery('#passwordRetype-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(firstName)}">jQuery('#firstName-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(lastName)}">jQuery('#lastName-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(company)}">jQuery('#company-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(country)}">jQuery('#country-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(accountType)}">jQuery('#accountType-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(hearAbout)}">jQuery('#hearAbout-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(hearAboutOther)}">jQuery('#hearAboutOther-msg').show();</c:if>
     	<c:if test="${not empty flowRequestContext.messageContext.getMessagesBySource(captchaChallenge)}">jQuery('#captchaChallenge-msg').show();</c:if>
      });
     
     function openPopUp(){
   	   window.open("resources/html/helpMeChooseContent.html", 
   			 	   "popUp", 
   			 	   'width=510,height=419');
     }
     
     jQuery(function() {
         jQuery('#password').simplePassMeter({
           'container': '#passwordMeter',
           'showOnValue': true,
           'requirements': {
               'minLength': {'value': 6},
               'matchField': {'value': '#passwordRetype', 'message': '<spring:message code="signup.form.password.error.notmatch" />'}
               }
         });
     });
     
     function showReferralTypeOther(value, doFocus) {
         if (value=='<spring:message code="signup.form.hearabout.option.other.value" />') {
           jQuery('#hearAboutOtherDiv1').show();
           jQuery('#hearAboutOtherDiv2').show();
           if (doFocus) {
             jQuery('#hearAboutOther').focus();
           }
         } else {
           jQuery('#hearAboutOtherDiv1').hide();
           jQuery('#hearAboutOtherDiv2').hide();
         }
       }
     showReferralTypeOther(jQuery('#hearAbout').val());
 </script>
</div>


<jsp:directive.include file="../includes/bottom.jsp" />
