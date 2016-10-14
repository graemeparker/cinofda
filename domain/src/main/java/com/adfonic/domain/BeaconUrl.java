package com.adfonic.domain;

import javax.persistence.*;

/**
 * Represents the destination the user arrives at via interaction with
 * an ad, in the simple case just a clickthrough URL.  Destinations are
 * unique per Advertiser and are immutable for tracking purposes.
 */
@Entity
@Table(name="BEACON_URL")
public class BeaconUrl extends BusinessKey {
    private static final long serialVersionUID = 3L;

    @Id @GeneratedValue @Column(name="BEACON_URL_ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DESTINATION_ID",nullable=false)
    private Destination destination;
    @Column(name="URL",length=1024,nullable=true)
    private String url;

    BeaconUrl() {}

    /** Use factory method on advertiser to construct. */
    public BeaconUrl(String url) {
        // Sanitize the destination URL
        this.url = sanitizeUrl(url);
        this.destination = null;
    }
    
    BeaconUrl(Destination destination, String url) {
    	this.destination = destination;
    	// Sanitize the destination URL
    	this.url = sanitizeUrl(url);
    	
    }

    public long getId() { return id; };
    
    public Destination getAdvertiser() { return destination; }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        // Sanitize the beacon URL
        this.url = sanitizeUrl(url);
    }
    
    /** Called by destination.addBeaconUrl() */
    void initializeDestination(Destination destination) {
        if (this.destination != null && this.destination != destination) {
            throw new IllegalArgumentException("Can't change the destination on an existing beacon url");
        }
        this.destination = destination;
    }
    
	private static final String[] CLICK_TOKENS = {
        "%publication%",
        "%dpid%",
        "%odin-1%",
        "%openudid%",
        "%creative%",
        "%campaign%",
        "%advertiser%",
        "%pid",
        "%click%",
        "%timestamp%"
    };

    /**
     * Sanitize a destination URL (for clickthrough, beacon, what have you)
     * as entered by the advertiser, ensuring that any click tokens we support
     * are properly lowercased as adserver will expect them to be.
     */
    public static String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()){
            return url;
        }
        String result = url;
        for (String token : CLICK_TOKENS) {
            result = result.replaceAll("(?i)" + token, token);
        }
        return result;
    }
}
