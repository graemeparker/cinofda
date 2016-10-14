package com.adfonic.domain.cache.dto.adserver.creative;

import java.math.BigDecimal;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class BidDeductionDto extends BusinessKeyDto {
	private static final long serialVersionUID = 1L;

	private boolean payerIsByyd;
	private Long thirdPartyVendorId;
	private String thirdPartyVendorFreeText;
	private BigDecimal amount;

	public boolean isPayerIsByyd() {
		return payerIsByyd;
	}

	public void setPayerIsByyd(boolean payerIsByyd) {
		this.payerIsByyd = payerIsByyd;
	}

	public Long getThirdPartyVendorId() {
		return thirdPartyVendorId;
	}

	public void setThirdPartyVendorId(Long thirdPartyVendorId) {
		this.thirdPartyVendorId = thirdPartyVendorId;
	}

	public String getThirdPartyVendorFreeText() {
		return thirdPartyVendorFreeText;
	}

	public void setThirdPartyVendorFreeText(String thirdPartyVendorFreeText) {
		this.thirdPartyVendorFreeText = thirdPartyVendorFreeText;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "BidDeductionDto [id=" + getId() + ", payerIsByyd=" + payerIsByyd
				+ ", thirdPartyVendorId=" + thirdPartyVendorId
				+ ", thirdPartyVendorFreeText=" + thirdPartyVendorFreeText
				+ ", amount=" + amount + "]";
	}
}
