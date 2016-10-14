package com.adfonic.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * Represents the destination the user arrives at via interaction with
 * an ad, in the simple case just a clickthrough URL.  Destinations are
 * unique per Advertiser and are immutable for tracking purposes.
 */
@Entity
@Table(name="DESTINATION")
public class Destination extends BusinessKey {
    private static final long serialVersionUID = 3L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;
    @Column(name="DESTINATION_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private DestinationType destinationType;
    @Column(name="DATA",length=1024,nullable=true)
    private String data;
    @OneToMany(fetch=FetchType.LAZY)
    @OrderBy("id")
    @JoinColumn(name="DESTINATION_ID",nullable=true)
    private List<BeaconUrl> beaconUrls;
    @Column(name="IS_DATA_FINAL_DESTINATION",nullable=false)
    private boolean dataIsFinalDestination;
    @Column(name="FINAL_DESTINATION",length=1024,nullable=true)
    private String finalDestination;

    {
    	this.dataIsFinalDestination = true;
    }
    
    Destination() {}

    /** Use factory method on advertiser to construct. */
    Destination(Advertiser advertiser, DestinationType destinationType, String data) {
    	this(advertiser, destinationType, data, true, null);
    }
    
    Destination(Advertiser advertiser, DestinationType destinationType, String data, boolean dataIsFinalDestination, String finalDestination) {
        this.advertiser = advertiser;
        this.destinationType = destinationType;
        // Sanitize the destination URL
        this.data = sanitizeUrl(data);
        this.beaconUrls = new ArrayList<BeaconUrl>();
        this.dataIsFinalDestination = dataIsFinalDestination;
        this.finalDestination = sanitizeUrl(finalDestination);
    }

    public long getId() { return id; };
    
    public Advertiser getAdvertiser() { return advertiser; }

    public DestinationType getDestinationType() { return destinationType; }
    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getData() { return data; }
    
    public void setData(String data) {
        // Sanitize the destination URL
        this.data = sanitizeUrl(data);
    }

    public List<BeaconUrl> getBeaconUrls() {
        return beaconUrls;
    }
    
    public void addBeaconUrl(BeaconUrl beaconUrl) {
        beaconUrl.initializeDestination(this);
        if(!beaconUrls.contains(beaconUrl)){
            beaconUrls.add(beaconUrl);
        }
    }
    
    public boolean isDataIsFinalDestination() {
		return dataIsFinalDestination;
	}

	public void setDataIsFinalDestination(boolean dataIsFinalDestination) {
		this.dataIsFinalDestination = dataIsFinalDestination;
	}

	public String getFinalDestination() {
		return finalDestination;
	}

	public void setFinalDestination(String finalDestination) {
		this.finalDestination = sanitizeUrl(finalDestination);
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
