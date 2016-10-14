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

<!DOCTYPE html>
<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
    <title><spring:eval expression="@configurationBean.companyName" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="shortcut icon" href="<c:url value="/themes/byyd/images/favicon.ico" />" />
    <link type="text/css" rel="stylesheet" href="<c:url value="/themes/byyd/css/primefaces.css" />" />
    <link type="text/css" rel="stylesheet" href="<c:url value="/themes/byyd/css/style.css" />" />
    <link type="text/css" rel="stylesheet" href="<c:url value="/themes/byyd/css/login.css" />" />
    <link type="text/css" rel="stylesheet" href="<c:url value="/themes/byyd/css/simplePassMeter.css?v=23610" />" />
    <!--[if lt IE 9]><script type="text/javascript" src="<c:url value="/js/html5shiv.js" />"></script><![endif]-->
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
    <script type="text/javascript" src="<c:url value="/themes/byyd/js/login.js" />"></script>
    <script type="text/javascript" src="<c:url value="/themes/byyd/js/jquery.simplePassMeter-0.6.js?v=23610" />"></script>
</head>
<body class="byyd">
    <div id="page" class="login">
        <header class="top">
            <nav>
                <div id="sub-menu" class="ui-menu ui-menubar ui-widget ui-widget-content ui-corner-all ui-helper-clearfix row">
                    <ul class="ui-menu-list ui-helper-reset">
                        <li class="ui-menuitem ui-widget ui-corner-all">
                            <a class="ui-menuitem-link ui-corner-all" href="<spring:eval expression="@configurationBean.developerBaseUrl" />" target="_blank">
                                <span class="ui-menuitem-text">Developer Docs</span>
                            </a>
                        </li>
                        <li class="ui-menuitem ui-widget ui-corner-all">
                            <a class="ui-menuitem-link ui-corner-all" href="<spring:eval expression="@configurationBean.tools2BaseUrl" /><spring:eval expression="@configurationBean.salesSupportLink" />">
                                <span class="ui-menuitem-text">Contact</span>
                            </a>
                        </li>
                        <c:set var="loginUrl" scope="page" value="${param.service}" />

                        <c:choose>
                        	<c:when test="${fn:contains(loginUrl,'j_spring_cas_security_check')}">
                        		  <c:set var="loginUrl" scope="page" value="${fn:substringBefore(loginUrl,'j_spring_cas_security_check')}"/>
                        	</c:when>
                        	<c:otherwise>
                        		<c:set var="loginUrl" scope="page" value="${param.service}"/>
                        	</c:otherwise>
                        </c:choose>

                        <li class="ui-menuitem ui-widget ui-corner-all">
                            <a class="ui-menuitem-link ui-corner-all" href="<c:out value="${loginUrl}" default="login"/>">
                                <span class="ui-menuitem-text">Log In</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
            <div class="row">
                <a href="<spring:eval expression="@configurationBean.wordpressBaseUrl" />" id="j_idt21"><img id="logo" src="<c:url value="/themes/byyd/images/external-logo.png" />" alt="<spring:eval expression="@configurationBean.companyName" /> logo" /></a>
            </div>
        </header>
