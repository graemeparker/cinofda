package com.adfonic.dto.campaign.campaignbid;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class BidDeductionDto extends BusinessKeyDTO {

	private static final long serialVersionUID = 1L;

	@Source("payerIsByyd")
	private Boolean payerIsByyd;

	@Source("thirdPartyVendor")
	private ThirdPartyVendorDto thirdPartyVendor;

	@Source("thirdPartyVendorFreeText")
	private String thirdPartyVendorFreeText;

	@Source("amount")
	private BigDecimal amount;

	@Source("startDate")
	private Date startDate;

	@Source("endDate")
	private Date endDate;

	public Boolean getPayerIsByyd() {
		return payerIsByyd;
	}

	public void setPayerIsByyd(Boolean payerIsByyd) {
		this.payerIsByyd = payerIsByyd;
	}

	public ThirdPartyVendorDto getThirdPartyVendor() {
		return thirdPartyVendor;
	}

	public void setThirdPartyVendor(ThirdPartyVendorDto thirdPartyVendor) {
		this.thirdPartyVendor = thirdPartyVendor;
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "BidDeductionDto [payerIsByyd=" + payerIsByyd + ", thirdPartyVendor=" + String.valueOf(thirdPartyVendor)
				+ ", thirdPartyVendorFreeText=" + String.valueOf(thirdPartyVendorFreeText) + ", amount=" + amount + ", startDate="
				+ startDate + ", endDate=" + endDate + "]";
	}

}
