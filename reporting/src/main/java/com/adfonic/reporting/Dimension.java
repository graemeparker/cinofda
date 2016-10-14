package com.adfonic.reporting;

import com.adfonic.domain.*;

public enum Dimension {
    Publisher(Company.class, Publisher.class, Publication.class, AdSpace.class),
    Advertiser(Company.class, Advertiser.class, Campaign.class, Creative.class),
    Location(Region.class, Country.class),
    Device(Vendor.class, Model.class),
    Geotarget(Geotarget.class);

    private Class[] levels;

    private Dimension(Class... levels) {
	this.levels = levels;
    }

    public int getDepth(Class clazz) {
	if (clazz != null) {
	    for (int i = 0; i < levels.length; i++) {
		if (levels[i].equals(clazz)) {
		    return i;
		}
	    }
	}
	return -1;
    }

    public int getDepth(Object object) {
	if (object == null) return -1;
	return getDepth(object.getClass());
    }
}