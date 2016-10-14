package com.adfonic.presentation.publication.model;

import com.adfonic.presentation.BaseSearchModel;

/**
 * Represent publication approvals dash board columns
 * 
 * Please define the properties in the same order as defined in the proc input parameters
 * The order will be used to populate sort field index
 * 
 */
public class PublicationApprovalModel extends BaseSearchModel {

    private static final long serialVersionUID = 1L;

    private String internalId;
    private String name;
    private String friendlyName;
    private String supplierName;
    private String supplierUserName;
    private String externalId;
    private String type;
    private String status;
    private String assignedTo;
    private String accountType;
    private String rtbId;
    private String sellerNetworkId;
    private String algorithmStatus;
    private String deadZoneStatus;
    private String bundle;
    private Boolean samplingActive;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierUserName() {
        return supplierUserName;
    }

    public void setSupplierUserName(String supplierUserName) {
        this.supplierUserName = supplierUserName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getRtbId() {
        return rtbId;
    }

    public void setRtbId(String rtbId) {
        this.rtbId = rtbId;
    }

    public String getSellerNetworkId() {
        return sellerNetworkId;
    }

    public void setSellerNetworkId(String sellerNetworkId) {
        this.sellerNetworkId = sellerNetworkId;
    }

    public String getAlgorithmStatus() {
        return algorithmStatus;
    }

    public void setAlgorithmStatus(String algorithmStatus) {
        this.algorithmStatus = algorithmStatus;
    }

    public String getDeadZoneStatus() {
        return deadZoneStatus;
    }

    public void setDeadZoneStatus(String deadZoneStatus) {
        this.deadZoneStatus = deadZoneStatus;
    }

    public Boolean getSamplingActive() {
		return samplingActive;
	}

	public void setSamplingActive(Boolean samplingActive) {
		this.samplingActive = samplingActive;
	}

	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationApprovalModel [internalId=").append(internalId).append(", name=").append(name).append(", friendlyName=").append(friendlyName)
                .append(", supplierName=").append(supplierName).append(", supplierUserName=").append(supplierUserName).append(", externalId=").append(externalId)
                .append(", bundle=").append(bundle).append(", type=").append(type).append(", status=").append(status).append(", assignedTo=").append(assignedTo)
                .append(", accountType=").append(accountType).append(", rtbId=").append(rtbId).append(", sellerNetworkId=").append(sellerNetworkId).append(", algorithmStatus=")
                .append(algorithmStatus).append(", deadZoneStatus=").append(deadZoneStatus).append(", samplingActive=").append(samplingActive).append("]");
        return builder.append("\n").append(super.toString()).toString();
    }

}
