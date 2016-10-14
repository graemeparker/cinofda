package com.adfonic.reporting.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.adfonic.reporting.service.parameter.Filter;
import com.adfonic.reporting.service.parameter.Group;
import com.adfonic.reporting.service.parameter.Procedure;

public class ReportUtil {
	
	private static final String REPORT = "report";
	private static final String SEPARATOR = "_";
	
	private static boolean groupByChannel(List<Group> group) {
		return group.contains(Group.CHANNEL);
	}
	
	private static boolean groupByIab(List<Group> group) {
		return group.contains(Group.IAB);
	}
	
	private static boolean groupByInventorySource(List<Group> group) {
		return group.contains(Group.INV_SOURCE);
	}
	
	private static boolean groupByPlatform(List<Group> group) {
		return group.contains(Group.PLATFORM);
	}
	
	private static boolean groupByVendor(List<Group> group) {
		return group.contains(Group.VENDOR);
	}
	
	private static boolean groupByLocation(List<Group> group) {
		return group.contains(Group.LOCATION);
	}
	
	private static boolean groupByRegion(List<Group> group) {
		return group.contains(Group.REGION);
	}
	
	public static String getProcedureName(String baseProcedureName, List<Group> group, Filter filter) {
		String procedure = baseProcedureName;
		if (group != null & filter != null) {
			if(groupByChannel(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.CHANNEL.getName());
			}
			if(groupByIab(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.IAB.getName());
			}
			if(groupByInventorySource(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.INV_SOURCE.getName());
			}
			if(groupByPlatform(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.PLATFORM.getName());
			}
			if(groupByVendor(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.VENDOR.getName());
			}
			
			if(groupByLocation(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.LOCATION.getName());
			}
			
			if(groupByRegion(group)) {
				procedure = procedure.concat(SEPARATOR).concat(Group.REGION.getName());
			}
			
			procedure = procedure.concat(SEPARATOR).concat(REPORT);
			switch (filter) {
			case TOTAL: 
				// Show daily statistics for all campaigns
				procedure = procedure.concat(SEPARATOR).concat(Filter.TOTAL.getName());
				break;
			case DAILY: 
				//Show detailed daily statistics for period
				procedure = procedure.concat(SEPARATOR).concat(Filter.DAILY.getName());
				break;
			case HOURLY: 
				//Show detailed statistics by hour for start date
				procedure = procedure.concat(SEPARATOR).concat(Filter.HOURLY.getName());
				break;
			default:
				//Show totals for period only
				procedure = procedure.concat(SEPARATOR).concat(Filter.DETAIL.getName());
				break;
			}
		} else {
			procedure = procedure.concat(SEPARATOR).concat(REPORT).concat(SEPARATOR).concat(Procedure.SUMMARY.getName());
		}
		
		return procedure;
	}
	
	public static <T extends Enum<T>> List<String> getEnumAsList(Class<T> clz) {
	     try {
	        List<String> res = new LinkedList<String>();
	        Method getDisplayValue = clz.getMethod("getParameter");

	        for (Object e : clz.getEnumConstants()) {
	            res.add((String) getDisplayValue.invoke(e));

	        }
	        return res;
	    } catch (Exception ex) {
	        throw new RuntimeException(ex);
	    }
	}
	
	public static Map<String,String> getQueryStringParameters(String queryString) {
		String[] parameters = queryString.split("&");
		Map<String,String> parametersMap = new HashMap<String,String>();
		for (int i = 0; i < parameters.length; i++) {
			String[] parameter = parameters[i].split("=");
			if(parameter.length == 2) {
				parametersMap.put(parameter[0].toLowerCase(), parameter[1].toLowerCase());
			} else {
				throw new IllegalArgumentException(String.format(ExceptionUtil.EMPTY_VALUE, parameter));
			}
		}
		return parametersMap;
	}
	
	public static Annotation getAnnotation(Annotation[] annotations, Class<? extends Annotation> clazz) {
		for(Annotation annotation : annotations) {
			if(annotation.annotationType().equals(clazz)) {
				return annotation;
			}
		}
		return null;
	}

	public static List<Filter> getFilters(String query) {
		String[] parameters = query.split("&");
		List<Filter> filters = new ArrayList<>();
		for(String param: parameters) {
			String[] parameter = param.split("=");
			if(parameter.length == 2 && parameter[0].toLowerCase().equals("filter")) {
				filters.add(Filter.valueOf(parameter[1]));
			}
		}
		return filters;
	}

	public static List<Group> getGroups(String query) {
		String[] parameters = query.split("&");
		List<Group> groups = new ArrayList<>();
		for(String param: parameters) {
			String[] parameter = param.split("=");
			if(parameter.length == 2 && parameter[0].toLowerCase().equals("group")) {
				groups.add(Group.valueOf(parameter[1]));
			}
		}
		return groups;
	}
}
