package com.adfonic.presentation.reporting;

import java.util.Formatter;
import java.util.Map;
import java.util.Map.Entry;

/** Uses Google Chart API to draw a map of today's geographic usage. */
public class GeoChart extends GoogleChart {
    private long max;
    private String label;
    
    /**
     * Expects a Map of String isoCodes, and Long impression count
     * @param impressionsPerCountry
     * @param width
     * @param height
     */
    public GeoChart(Map<String, Long> impressionsPerCountry, int width, int height) {
    	super(width, height);
    
    	StringBuffer sbCountryNames = new StringBuffer(); // two letter iso codes, no separators
    	StringBuffer sbCountryValues = new StringBuffer(); // comma-separated "heat" percentages
     	Formatter fbCountryValues = new Formatter(sbCountryValues);
    	max = 0;
    	if (impressionsPerCountry.size() > 0) {
    	    for (Entry<String, Long> e : impressionsPerCountry.entrySet()) {
    	        long temp = (Long) e.getValue();
    	        if (temp>max){ max = temp; }
            }
    	}
    	for (Entry<String, Long> entry : impressionsPerCountry.entrySet()) {
    	    sbCountryNames.append(entry.getKey()); // no separators
    	    if (sbCountryValues.length() > 0) {
    		sbCountryValues.append(",");
    	    }
    	    fbCountryValues.format("%.2f", 100 * (((Long) entry.getValue()) / (double) max));
    	}
    	fbCountryValues.close();
    
    	// Chart type = map
    	addParam("cht", "t");
    	addParam("chtm", "world");
    
    	// Colors to use; first is for unlisted areas, others relate to percentages 0,25,50,75,100
    	addParam("chco", "D6D7D9,6D6E71,A0D0E4,172C42,28AECC,E22A83");
    
    	// Fill the ocean with white
    	addParam("chf", "bg,s,FFFFFF");
    
    	addParam("chld", sbCountryNames.toString());
    	addParam("chd", "t:" + sbCountryValues.toString());
    
    	generateURL();
    }

    public long getMax() { 
        return max; 
    }
    
    public String getLabel() {
        if (max == 0) {
            label = "No impressions";
        }
        return label;
    }

}
