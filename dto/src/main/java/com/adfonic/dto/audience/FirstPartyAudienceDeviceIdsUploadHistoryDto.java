package com.adfonic.dto.audience;

import java.util.Date;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;

public class FirstPartyAudienceDeviceIdsUploadHistoryDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source("dateTimeUploaded")
    private Date dateTimeUploaded;
    @Source("filename")
    private String filename;
    @DTOCascade
    @Source("deviceIdentifierType")
    private DeviceIdentifierTypeDto deviceIdentifierType;
    @Source("totalNumRecords")
    private Long totalNumRecords;
    @Source("numValidatedRecords")
    private Long numValidatedRecords;
    @Source("numInsertedRecords")
    private Long numInsertedRecords;

    public Date getDateTimeUploaded() {
        return dateTimeUploaded;
    }

    public void setDateTimeUploaded(Date dateTimeUploaded) {
        this.dateTimeUploaded = (dateTimeUploaded == null? null : new Date(dateTimeUploaded.getTime()));
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DeviceIdentifierTypeDto getDeviceIdentifierType() {
        return deviceIdentifierType;
    }

    public void setDeviceIdentifierType(DeviceIdentifierTypeDto deviceIdentifierType) {
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
