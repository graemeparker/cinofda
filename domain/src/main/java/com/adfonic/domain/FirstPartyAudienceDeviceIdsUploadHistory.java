package com.adfonic.domain;

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

@Entity
@Table(name = "FIRST_PARTY_AUDIENCE_DEVICE_ID_UPLOAD_HISTORY")
public class FirstPartyAudienceDeviceIdsUploadHistory extends BusinessKey {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue @Column(name="ID")
	private long id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FIRST_PARTY_AUDIENCE_ID",nullable=false)
	private FirstPartyAudience firstPartyAudience;
    @Column(name="DATE_TIME_UPLOADED",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTimeUploaded;
    @Column(name="FILENAME",nullable=false)
    private String filename;
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DEVICE_IDENTIFIER_TYPE_ID",nullable=false)
	private DeviceIdentifierType deviceIdentifierType;
	// Total number of records in the file
	@Column(name="TOTAL_NUM_RECORDS",nullable=false)
	private Long totalNumRecords;
	// Total number of records validated through regex and other means
	@Column(name="NUM_VALIDATED_RECORDS",nullable=false)
	private Long numValidatedRecords;
	// Total numbers of recoerds actually inserted by the stored proc, could be less than numValidatedRecords because of dupes
	@Column(name="NUM_INSERTED_RECORDS",nullable=false)
	private Long numInsertedRecords;
	
	FirstPartyAudienceDeviceIdsUploadHistory() {}
	
	public FirstPartyAudienceDeviceIdsUploadHistory(
			FirstPartyAudience firstPartyAudience,
			String filename,
			DeviceIdentifierType deviceIdentifierType,
			Long totalNumRecords,
			Long numValidatedRecords,
			Long numInsertedRecords) {
		super();
		this.firstPartyAudience = firstPartyAudience;
		this.filename = filename;
		this.deviceIdentifierType = deviceIdentifierType;
		this.totalNumRecords = totalNumRecords;
		this.numValidatedRecords = numValidatedRecords;
		this.numInsertedRecords = numInsertedRecords;
		this.dateTimeUploaded = new Date();
	}
	
	public long getId() {
 		return id;
 	}

	public FirstPartyAudience getFirstPartyAudience() {
		return firstPartyAudience;
	}

	public void setFirstPartyAudience(FirstPartyAudience firstPartyAudience) {
		this.firstPartyAudience = firstPartyAudience;
	}

	public Date getDateTimeUploaded() {
		return dateTimeUploaded;
	}

	public void setDateTimeUploaded(Date dateTimeUploaded) {
		this.dateTimeUploaded = dateTimeUploaded;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public DeviceIdentifierType getDeviceIdentifierType() {
		return deviceIdentifierType;
	}

	public void setDeviceIdentifierType(DeviceIdentifierType deviceIdentifierType) {
		this.deviceIdentifierType = deviceIdentifierType;
	}

	public Long getTotalNumRecords() {
		return totalNumRecords;
	}

	public void setTotalNumRecords(Long totalNumRecords) {
		this.totalNumRecords = totalNumRecords;
	}

	public Long getNumValidatedRecords() {
		return numValidatedRecords;
	}

	public void setNumValidatedRecords(Long numValidatedRecords) {
		this.numValidatedRecords = numValidatedRecords;
	}

	public Long getNumInsertedRecords() {
		return numInsertedRecords;
	}

	public void setNumInsertedRecords(Long numInsertedRecords) {
		this.numInsertedRecords = numInsertedRecords;
	}

}
