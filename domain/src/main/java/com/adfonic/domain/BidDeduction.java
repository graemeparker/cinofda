package com.adfonic.domain;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;

@Entity
@Table(name = "BID_DEDUCTION")
public class BidDeduction extends BusinessKey {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "BID_DEDUCTION_ID")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_ID", nullable = false)
	private Campaign campaign;

	@Column(name = "PAYER_IS_BYYD", nullable = false)
	private Boolean payerIsByyd;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "THIRD_PARTY_VENDOR_ID", nullable = true)
	private ThirdPartyVendor thirdPartyVendor;

	@Column(name = "THIRD_PARTY_VENDOR_FREE_TEXT", length = 255, nullable = true)
	private String thirdPartyVendorFreeText;

	@Column(name = "AMOUNT", nullable = false)
	private BigDecimal amount;

	@Column(name = "START_DATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Column(name = "END_DATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	public BidDeduction() {
		this.startDate = new Date();
		try {
			this.endDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/31/9999");
		} catch (ParseException e) {
		}
	}
	
	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public Boolean getPayerIsByyd() {
		return payerIsByyd;
	}

	public void setPayerIsByyd(Boolean payerIsByyd) {
		this.payerIsByyd = payerIsByyd;
	}

	public ThirdPartyVendor getThirdPartyVendor() {
		return thirdPartyVendor;
	}

	public void setThirdPartyVendor(ThirdPartyVendor thirdPartyVendor) {
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

	public boolean hasFieldChanged(BidDeduction newBidDeduction) {
		return ObjectUtils.notEqual(this.getPayerIsByyd(), newBidDeduction.getPayerIsByyd()) ||
				ObjectUtils.notEqual(this.getThirdPartyVendor(), newBidDeduction.getThirdPartyVendor()) ||
				ObjectUtils.notEqual(this.getThirdPartyVendorFreeText(), newBidDeduction.getThirdPartyVendorFreeText()) ||
				this.getAmount().compareTo(newBidDeduction.getAmount()) != 0;
	}
	

}
