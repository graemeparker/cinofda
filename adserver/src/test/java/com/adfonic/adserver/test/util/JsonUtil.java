package com.adfonic.adserver.test.util;

import org.json.JSONObject;

public class JsonUtil {

	public static final String AD_IMPRESSION_ID = "adId";
	public static final String STATUS = "status";
	public static final String AD_FORMAT = "format";
	public static final String COMPONENETS = "components";
	public static final String DESTINATION = "destination";
	
	public static String getString(JSONObject jsonObject,String propertyName){
		return getString(jsonObject, propertyName, null);
	}
	public static String getString(JSONObject jsonObject,String propertyName,String defaultValue){
		String returnValue = defaultValue;
		try{
			returnValue = jsonObject.getString(propertyName);
		}catch(Exception ex){
			
		}
		System.out.println(propertyName+"="+returnValue);
		
		return returnValue;
	}
	
	public static JSONObject getJsonObject(JSONObject jsonObject,String propertyName){
		return getJsonObject(jsonObject, propertyName, null);
	}
	public static JSONObject getJsonObject(JSONObject jsonObject,String propertyName,JSONObject defaultValue){
		JSONObject returnValue = defaultValue;
		try{
			returnValue = jsonObject.getJSONObject(propertyName);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return returnValue;
	}
}
