package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "THIRD_PARTY_VENDOR")
public class ThirdPartyVendor extends BusinessKey implements Named {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "THIRD_PARTY_VENDOR_ID")
	private long id;

	@Column(name = "THIRD_PARTY_VENDOR_NAME", length = 255, nullable = false)
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "THIRD_PARTY_VENDOR_TYPE_ID", nullable = false)
	private ThirdPartyVendorType thirdPartyVendorType;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public ThirdPartyVendorType getThirdPartyVendorType() {
		return thirdPartyVendorType;
	}

}
