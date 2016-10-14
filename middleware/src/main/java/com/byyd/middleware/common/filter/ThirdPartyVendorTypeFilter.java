package com.byyd.middleware.common.filter;

import com.byyd.middleware.iface.dao.LikeSpec;

public class ThirdPartyVendorTypeFilter {

	private String name;
	private LikeSpec nameLikeSpec;
	private boolean nameCaseSensitive;

	public ThirdPartyVendorTypeFilter setName(String name, boolean nameCaseSensitive) {
		return this.setName(name, null, nameCaseSensitive);
	}

	public ThirdPartyVendorTypeFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
		this.name = name;
		this.nameLikeSpec = nameLikeSpec;
		this.nameCaseSensitive = nameCaseSensitive;
		return this;
	}

	public String getName() {
		return name;
	}

	public LikeSpec getNameLikeSpec() {
		return nameLikeSpec;
	}

	public boolean isNameCaseSensitive() {
		return nameCaseSensitive;
	}

}
