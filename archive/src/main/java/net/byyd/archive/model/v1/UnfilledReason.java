package net.byyd.archive.model.v1;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public enum UnfilledReason {
	UNKNOWN("UN"),
	EXCEPTION("EX"),
	NO_DEVICE_PROPS("ND"),
	NO_MODEL("NM"), 
	NO_CREATIVES("NC"), 
	FREQUENCY_CAP("FC"), 
	NOT_MOBILE_DEVICE("NMD"), 
	NO_USER_AGENT("UUA"), 
	TIMEOUT("TO"), 
	PUB_TYPE_MODEL_MISMATCH("PTM"), 
	PRIVATE_NETWORK("PM");
	
	private String shortName;

	UnfilledReason(String shortName) {
		this.shortName = shortName;
	}
	
	@JsonValue
	public String getShortName() {
		return shortName;
	}
	
	@JsonCreator
	public static UnfilledReason create(String shortName) {
		UnfilledReason ret = null;
		
		for(UnfilledReason ur : values()) {
			if (ur.getShortName().equals(shortName)) {
				ret = ur;
				break;
			}
		}
		
		return ret;
	}
}
