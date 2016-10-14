package com.adfonic.presentation.util;

import org.apache.commons.lang.StringUtils;

public class GenericDaoImpl {
    
    // Helper methods for producing proc call messages with parameters
    
    protected Object procCallNoParam(String procName) {
        return procCallWithOutParam(procName, null, null);
    }

    protected Object procCallWithOneParam(String procName, Object param) {
        Object[] idArray = { param };
        return procCallWithOutParam(procName, idArray, null);
    }

    protected Object procCallWithOutParam(String procName, Object[] params, String outParamsStr) {
        String postFix = (outParamsStr == null) ? ");" : outParamsStr;
        StringBuilder sb = new StringBuilder("call " + procName + "(");
        if (params != null) {
            String sep = StringUtils.EMPTY;
            for (Object param : params) {
                sb.append(sep);
                if (param == null) {
                    sb.append("null");
                } else if (StringUtils.isEmpty(param.toString())) {
                    sb.append("''");
                } else if (param instanceof String) {
                    sb.append("'" + param + "'");
                } else {
                    sb.append(param.toString());
                }
                sep = ", ";
            }
        }
        return sb.append(postFix).toString();
    }
}
