package net.byyd.archive.model.v1;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;


public enum AdAction {
	UNFILLED_REQUEST("UR"), //     RTB_FAILED
    IMPRESSION("I"), // represents BEACON
    CLICK("C"), 
    INSTALL("IN"),
    BID_SERVED("BS"), 
    AD_SERVED("A"), // represents AD
    AD_FAILED("AF"), 
    CONVERSION("CS"), // arbitrary for now
    BID_FAILED("BF"),
    AD_SERVED_AND_IMPRESSION("AI"), 
    COMPLETED_VIEW("CV"),
    VIEW_Q1("Q1"),
    VIEW_Q2("Q2"),
    VIEW_Q3("Q3"),
    VIEW_Q4("Q4"),
    IMPRESSION_FAILED("IF"),
    CLICK_FAILED("CF"),
    RTB_LOST("RL"),
    RTB_WIN_FAILED("WF"),
    RTB_FAILED("RF"),
    NO_PUBLICATION("NP");

    private String shortName;
	
	AdAction(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	@JsonValue
	public String toString() {
		return shortName;
	}

	@JsonCreator
	public static AdAction create(String shortName) {
		AdAction ret = null;
		
		for(AdAction ad : values()) {
			if (ad.getShortName().equals(shortName)) {
				ret = ad;
				break;
			}
		}
		
		return ret;
	}
}
