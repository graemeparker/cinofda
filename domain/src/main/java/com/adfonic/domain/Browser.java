package com.adfonic.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.*;

import com.adfonic.util.HttpRequestContext;

/**
 * Specifies targeting of a specific browser.  The browser
 * is identified by applying regular expressions against
 * request headers (typically, the User-Agent header).
 */
@Entity
@Table(name="BROWSER")
public class Browser extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="BROWSER_HEADER_MAP",joinColumns=@JoinColumn(name="BROWSER_ID"))
    @MapKeyColumn(name="HEADER",length=255,nullable=false)
    @MapKeyClass(String.class)
    @Column(name="VALUE",length=255,nullable=false)
    private Map<String, String> headerMap;
    
    @Column(name="BROWSER_ORDER", length=11, nullable=false)
    private int browserOrder;
    
    // Cache the Pattern objects so they can be efficiently reused
    private transient Map<String, Pattern> patternMap;
    private transient boolean patternMapGood = false;

    {
	headerMap = new HashMap<String, String>();
    }

    Browser() {}

    public Browser(String name) {
	this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Each header is mapped to a String that is a Java regular expression
     * for the field of that header.
     */
    public void setHeaderRegexp(String header, String regexp) {
	headerMap.put(header, regexp);
	patternMapGood = false;
    }

    public String getHeaderRegexp(String header) {
	return headerMap.get(header);
    }

    public int getBrowserOrder() { 
        return this.browserOrder;
    }
    
    public void setBrowserOrder(int browserOrder) {
        this.browserOrder = browserOrder;
    }
    
    private synchronized void createPatternMap() {
	if (!patternMapGood) {
	    patternMap = new HashMap<String, Pattern>();
	    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
		// Note: can throw unchecked PatternSyntaxException
		patternMap.put(entry.getKey(), 
			       Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE));
	    }
	    patternMapGood = true;
	}
    }

    /**
     * Checks that the headers in the context parameter match
     * the regular expression rules for this Browser.  Non-existent
     * headers always cause failure.
     */
    public boolean isMatch(HttpRequestContext context) {
        if (!patternMapGood) {
            createPatternMap();
        }

        for (Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
            String value = context.getHeader(entry.getKey());
            if (value == null) {
                // https://tickets.adfonic.com/browse/AF-549
                // https://tickets.adfonic.com/browse/AF-585
                // Treat an absent header as the empty string so it will match a
                // regex such as "^$" if we need to enforce "absent or empty."
                value = "";
            }
            if (!entry.getValue().matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }
}
