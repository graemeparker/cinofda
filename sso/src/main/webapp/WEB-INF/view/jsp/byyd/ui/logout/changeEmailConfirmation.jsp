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

<jsp:directive.include file="../includes/top.jsp" />

<div class="content ovh">
	<form id="form-wrapper">

		<div class="row-title form-header">
			<h1 class="title">
				<spring:message code="changeemail.logout.header" />
			</h1>
		</div>

		<div>
			<p class="ptext" style="margin-top: 20px;">
				<spring:message code="changeemail.logout.message1" />
			</p>
			<p class="ptext" style="margin-top: 20px;">
				<spring:message code="changeemail.logout.message2" />
			</p>
			<p class="ptext" style="margin-top: 20px;">
				<spring:eval expression="@configurationBean.companyName"
					var="companyName" />
				<a
					href="<spring:eval expression="@configurationBean.tools2BaseUrl" />">
					<spring:message code="changeemail.logout.gotohomepage"
						arguments="${companyName}" />
				</a> <br /> <a
					href="<spring:eval expression="@configurationBean.tools2BaseUrl" /><spring:eval expression="@configurationBean.customerSupportLink" />">
					<spring:message code="changeemail.logout.contactsupport"
						arguments="${companyName}" />
				</a>
			</p>
		</div>
	</form>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />