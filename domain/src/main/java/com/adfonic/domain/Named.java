package com.adfonic.domain;

import java.util.Comparator;

public interface Named {
    String getName();

    Comparator<Named> COMPARATOR = new Comparator<Named>() {
	public int compare(Named lhs, Named rhs) {
	    if (lhs == null) {
		return (rhs == null) ? 0 : -1;
	    }
	    if (rhs == null) {
		return 1;
	    }
	    String lname = lhs.getName();
	    String rname = rhs.getName();
	    if (lname == null) {
		return (rname == null) ? 0 : -1;
	    }
	    if (rname == null) {
		return 1;
	    }
	    return lname.compareToIgnoreCase(rname);
	}
    };
}
