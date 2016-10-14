package com.adfonic.adserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;

import com.adfonic.test.AbstractAdfonicTest;

@Ignore
public class BaseAdserverTest extends AbstractAdfonicTest {
    
    protected final Map<String, String> nullmap = null;
    
    /**
     * Generate random id for Requests param like Id, ImressionId,User Id etc
     * @return
     */
    public static String genrateRandomIdForRequests(){
    	//No need to use StringBuilder or Buffer, it seems compiler is smart enough to generate
    	//code as if we have written following linesu sing StringBuilder.
    	return randomAlphaNumericString(8)+"-"+randomAlphaNumericString(4)+"-"+randomAlphaNumericString(4)+"-"+randomAlphaNumericString(4)+"-"+randomAlphaNumericString(12);
    }
    
	/**
	 * Convert a few Strings into List of String
	 * @param strings
	 * @return
	 */
	public static List<String> getStringList(String...strings ){
		List<String> list = new ArrayList<String>();
		for(String oneString:strings){
			list.add(oneString);
		}
		return list;
	}
    
	/**
	 * put a key value in map only if value is not null
	 * @param request
	 * @param key
	 * @param value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void putIfNotNull(Map map, String key, Object value){
		if (value != null) {
			map.put(key, value);
		}
	}

	/**
	 * put a key value in map only if value is not null
	 * @param request
	 * @param key
	 * @param value
	 */
	public static void putIfNotNull(JSONObject jsonObject, String key, Object value) throws JSONException {
		if (value != null) {
			jsonObject.put(key, value);
		}
	}
	
	protected void SleepForSeconds(long seconds){
		try {
			System.out.println("Sleeping for "+ seconds+ " seconds");
			Thread.sleep(1000*seconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	protected void SleepForMilliSeconds(long seconds){
		SleepForSeconds(seconds/1000);
	}
	
	protected Impression getImpression() {
		String impressionExternalId = randomAlphaNumericString(10);
		final Long adSpaceId = randomLong();
		final Impression impression = new Impression();
		impression.setAdSpaceId(adSpaceId);
		impression.setExternalID(impressionExternalId);
		impression.setCountryId(1L);
		impression.setHost("qa.it.aerospike.byyd-tech.com");
		return impression;
	}
	
	protected ParallelModeBidDetails getBidDetails(String ipAddress, Impression impression) {
		return new ParallelModeBidDetails(ipAddress, impression);
	}
}
