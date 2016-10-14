package com.adfonic.presentation.reporting;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/** Uses Google Chart API to draw a donut chart of geographic usage. */
public class DonutChart extends GoogleChart {
    
    /**
     * Expects a Map of String isoCodes, and Long impression count
     * @param impressionsPerCountry
     * @param width
     * @param height
     */
    public DonutChart(Map<String, Long> impressionsPerCountry, int width, int height) {
    	super(width, height);

    	StringBuffer sbCountryNames = new StringBuffer(); // two letter iso codes, pipe delimited
    	StringBuffer sbCountryValues = new StringBuffer(); 
        double agg = 0;
        int count = 0;
        for(Entry<String, Long> e : impressionsPerCountry.entrySet()) {
            count++;
            if (count > 4) {
                agg += (double) e.getValue();
            }
        }
        Iterator<Entry<String, Long>> iterator = impressionsPerCountry.entrySet().iterator();
        int index = 0;
        while(iterator.hasNext() && index < 5) {
            index++;
            sbCountryNames.append("|");
    	    Entry<String, Long> entry = iterator.next();
            sbCountryNames.append(entry.getKey()); 
    	    if (sbCountryValues.length() > 0) {
    	        sbCountryValues.append(",");  
    	    }
    	    sbCountryValues.append((double) entry.getValue());
    	}
        
        // Colours to use; first is for the 'piehole', others relate to pie slices
        String colourString = "FFFFFF,6D6E71|A0D0E4|172C42|28AECC|E22A83";
        
     	if (count > 4) {
     	    sbCountryNames.append("|");
     	    sbCountryNames.append("Other");
     	    sbCountryValues.append(","); 
     	    sbCountryValues.append(agg);
     	    // Add a grey colour for the 'Other' countries aggregated into one pie slice
     	    colourString += "|B2B3B7";
     	}

    	// Chart type = pie complex
    	addParam("cht", "pc");
    

        addParam("chco", colourString);
    
    	String chartDataString = sbCountryValues.toString();
    	String chartLabel = "";
    	if (chartDataString.equals("0.0")) {
    	     chartLabel += "No impressions";
    	} else {
    	    chartLabel += sbCountryNames.toString();
    	}
    	addParam("chdl", chartLabel);
    	addParam("chd", "t:0|" + chartDataString);
    	addParam("chl", "0|" + formatLabelString(chartDataString));
    	addParam("chdlp", "b");
    	
    	generateURL();
    }

    public String formatLabelString(String chartDataString) {
        String[] tokenizedString = chartDataString.split(",");
    	StringBuffer labelString = new StringBuffer();
    	NumberFormat formatter = NumberFormat.getInstance();
    	formatter.setMaximumFractionDigits(2);
    	for (String dataValue : tokenizedString) {
    	    if (labelString.length() > 0) {
    	        labelString.append("|");
    	    }
            Double dataValueAsNumber = Double.valueOf(dataValue);
            if (dataValueAsNumber > 1000) {
                String formattedValue = formatter.format(dataValueAsNumber / 1000);
                labelString.append(formattedValue);
                labelString.append("k");
            } else {
                labelString.append(StringUtils.remove(dataValue, ".0"));
            }
        }
        return labelString.toString();
    }

}
