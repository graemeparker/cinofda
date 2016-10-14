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

    <div class="content login-success ovh">
	    <form id="login-form-wrapper">
	        <div class="row-title form-header">
	            <h1 class="title" id="login-title"><spring:message code="screen.success.header" /></h1>
	        </div>
	
	        <div class="logout-content">
	            <span><spring:message code="screen.success.success" /></span>
	        <br />
	            <span><spring:message code="screen.success.security" /></span>
	        </div>
	
	    </form>
    </div>

<jsp:directive.include file="../includes/bottom.jsp" />

