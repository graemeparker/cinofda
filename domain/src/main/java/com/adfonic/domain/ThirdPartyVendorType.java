package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "THIRD_PARTY_VENDOR_TYPE")
public class ThirdPartyVendorType extends BusinessKey implements Named {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "THIRD_PARTY_VENDOR_TYPE_ID")
	private long id;

	@Column(name = "THIRD_PARTY_VENDOR_TYPE_NAME", length = 255, nullable = false)
	private String name;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

}
