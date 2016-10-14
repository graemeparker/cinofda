package com.byyd.adsquare.v2;

public class AdsqrEnrichQueryRequest {

    private String country;
    private String zip;
    private String deviceIdRaw;
    private String deviceIdMd5;
    private String deviceIdSha1;
    private Double longitude;
    private Double latitude;
    private String appId;
    private String appName;
    private String iabCategoryApp;
    private String applicationBundle;
    private String ipAddress;
    private String deviceModel;
    private String manufacturer;
    private String connectionType;
    private String carrier;
    private String deviceType;
    private Integer sspId;

    AdsqrEnrichQueryRequest() {
        // default constructor for json marshalling 
    }

    /**
     * Geo location only based query
     */
    public AdsqrEnrichQueryRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Device id only based query
     */
    public AdsqrEnrichQueryRequest(String deviceIdRaw, String deviceIdSha1, String deviceIdMd5, String deviceType, Integer sspId) {
        this.deviceIdRaw = deviceIdRaw;
        this.deviceIdSha1 = deviceIdSha1;
        this.deviceIdMd5 = deviceIdMd5;
        this.sspId = sspId;
    }

    /**
     * Combined query
     */
    public AdsqrEnrichQueryRequest(Double latitude, Double longitude, String deviceIdRaw, String deviceIdSha1, String deviceIdMd5, String deviceType, Integer sspId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceIdRaw = deviceIdRaw;
        this.deviceIdSha1 = deviceIdSha1;
        this.deviceIdMd5 = deviceIdMd5;
        this.deviceType = deviceType;
        this.sspId = sspId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getDeviceIdRaw() {
        return deviceIdRaw;
    }

    public void setDeviceIdRaw(String devideIdRaw) {
        this.deviceIdRaw = devideIdRaw;
    }

    public String getDeviceIdMd5() {
        return deviceIdMd5;
    }

    public void setDeviceIdMd5(String deviceIdMd5) {
        this.deviceIdMd5 = deviceIdMd5;
    }

    public String getDeviceIdSha1() {
        return this.deviceIdSha1;
    }

    public void setDeviceIdSha1(String devideIdSha1) {
        this.deviceIdSha1 = devideIdSha1;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIabCategoryApp() {
        return iabCategoryApp;
    }

    public void setIabCategoryApp(String iabCategoryApp) {
        this.iabCategoryApp = iabCategoryApp;
    }

    public String getApplicationBundle() {
        return applicationBundle;
    }

    public void setApplicationBundle(String applicationBundle) {
        this.applicationBundle = applicationBundle;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getSspId() {
        return sspId;
    }

    public void setSspId(Integer sspId) {
        this.sspId = sspId;
    }

    @Override
    public String toString() {
        return "AdsqrEnrichQueryRequest {deviceIdRaw=" + deviceIdRaw + ", deviceIdMd5=" + deviceIdMd5 + ", deviceIdSha1=" + deviceIdSha1 + ", longitude=" + longitude
                + ", latitude=" + latitude + ", ipAddress=" + ipAddress + ", deviceType=" + deviceType + ", sspId=" + sspId + "}";
    }

}
