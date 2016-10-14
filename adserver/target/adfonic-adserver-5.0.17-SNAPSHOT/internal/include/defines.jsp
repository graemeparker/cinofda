<%@page import="java.util.Map.Entry"%>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="com.adfonic.adserver.*" %>
<%@ page import="com.adfonic.domain.*" %>
<%@ page import="com.adfonic.domain.cache.dto.adserver.*" %>
<%@ page import="com.adfonic.domain.cache.dto.adserver.adspace.*" %>
<%@ page import="com.adfonic.domain.cache.dto.adserver.creative.*" %>
<%!
static final String MESSAGE = "MESSAGE";
static final String ERROR_MESSAGE = "ERROR_MESSAGE";
%>
<%
//ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(application);
ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
String title = null;

String _msg = (String)session.getAttribute(MESSAGE);
session.removeAttribute(MESSAGE);

String _errMsg = (String)session.getAttribute(ERROR_MESSAGE);
session.removeAttribute(ERROR_MESSAGE);
%>
<%!
static String[] daysOfWeekAbbrevs = new String[] {
    "Su",
    "Mo",
    "Tu",
    "We",
    "Th",
    "Fr",
    "Sa"
};

static String showHoursOfDay(int h) {
    if (h == Segment.ALL_HOURS) {
        return "ALL";
    }
    StringBuilder bld = new StringBuilder();
    Integer rangeStart = null;
    Integer lastSeen = null;
    for (int k = 0; k < 24; ++k) {
        if ((h & (1 << k)) != 0) {
            try {
                if (rangeStart == null) {
                    rangeStart = k;
                    continue; // start the new range
                }
                else if (lastSeen == (k - 1)) {
                    continue; // keep going in the range
                }
                else {
                    // finish the old range and start the new range
                    bld.append('-')
                        .append(String.valueOf(lastSeen));
                    rangeStart = k;
                    continue;
                }
            }
            finally {
                lastSeen = k;
            }
        }
        else if (rangeStart != null) {
            if (bld.length() > 0) {
                bld.append(", ");
            }
            bld.append(String.valueOf(rangeStart))
                .append('-')
                .append(String.valueOf(lastSeen));
            rangeStart = null;
        }
    }
    if (rangeStart != null) {
        if (bld.length() > 0) {
            bld.append(", ");
        }
        bld.append(String.valueOf(rangeStart))
            .append('-')
            .append(String.valueOf(lastSeen));
    }
    return bld.toString();
}
%>
