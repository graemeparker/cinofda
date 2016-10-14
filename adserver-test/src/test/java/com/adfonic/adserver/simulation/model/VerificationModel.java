package com.adfonic.adserver.simulation.model;

import java.util.List;

public class VerificationModel<T> {

	private boolean exactMatch;

	private List<T> require;
	private List<T> allow;
	private List<T> deny;

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	public List<T> getRequire() {
		return require;
	}

	public void setRequire(List<T> require) {
		this.require = require;
	}

	public List<T> getAllow() {
		return allow;
	}

	public void setAllow(List<T> allow) {
		this.allow = allow;
	}

	public List<T> getDeny() {
		return deny;
	}

	public void setDeny(List<T> deny) {
		this.deny = deny;
	}
}
