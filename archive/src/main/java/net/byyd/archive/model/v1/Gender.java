package net.byyd.archive.model.v1;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public enum Gender {
	FEMALE("F"), MALE("M");

	private String shortName;

	private Gender(String shortName) {
		this.shortName = shortName;
	}

	@JsonValue
	public String getShortName() {
		return shortName;
	}
	
	@JsonCreator
	public static Gender create(String shortName) {
		Gender ret = null;
		
		for(Gender g : values()) {
			if (g.getShortName().equals(shortName)) {
				ret = g;
				break;
			}
		}
		
		return ret;
	}

	public static Gender from(String gender) {
		if (gender == null) {
			return null;
		}
		return "f".equals(gender.toLowerCase()) || "female".equals(gender.toLowerCase()) ? FEMALE : MALE;
	}
}
