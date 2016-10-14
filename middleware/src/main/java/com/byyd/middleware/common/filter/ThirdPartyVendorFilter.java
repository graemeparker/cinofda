package com.byyd.middleware.common.filter;

import java.util.Collection;

import com.byyd.middleware.iface.dao.LikeSpec;

public class ThirdPartyVendorFilter {

	private String name;
	private LikeSpec nameLikeSpec;
	private boolean nameCaseSensitive;
	private Collection<Long> thirdPartyVendorTypeIds;

	public ThirdPartyVendorFilter setName(String name, boolean nameCaseSensitive) {
		return this.setName(name, null, nameCaseSensitive);
	}

	public ThirdPartyVendorFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
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

	public Collection<Long> getThirdPartyVendorTypeIds() {
		return thirdPartyVendorTypeIds;
	}

	public ThirdPartyVendorFilter setThirdPartyVendorTypeIds(Collection<Long> thirdPartyVendorTypeIds) {
		this.thirdPartyVendorTypeIds = thirdPartyVendorTypeIds;
		return this;
	}
}
