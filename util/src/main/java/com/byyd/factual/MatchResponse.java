package com.byyd.factual;

/**
 * @author mvanek
 * 
 * http://developer.factual.com/geopulse-on-prem-overview/#response
 *
 */
public class MatchResponse {

    /**
     * Name of the Geopulse Proximity or Audience Design, assigned in the Designer UI.
     */
    private String designName;

    /**
     * Factual generated unique ID for the Design.
     */
    private String designId;

    /**
     * A string assigned to the Set when designing the Design in the Designer UI. (In deprecated versions this was formerly called the “Group ID”.)
     */
    private String targetingCode;

    /**
     * Factual generated unique ID for the associated deploy of the Design.
     */
    private String deploymentId;

    /**
     * Factual generated unique ID for the Set within the Design that matched.
     */
    private String setId;

    /**
     * Factual generated URL prefix to implement Pixel assisted features. More parameters are required to be appended to this prefix to form the final Data Pixel URL.
     * http://developer.factual.com/geopulse-data-pixel/
     */
    private String dataPixelUrlPrefix;

    MatchResponse() {
        // default
    }

    public MatchResponse(String designName, String targetingCode, String deploymentId, String setId, String dataPixelUrlPrefix) {
        this.designName = designName;
        this.targetingCode = targetingCode;
        this.deploymentId = deploymentId;
        this.setId = setId;
        this.dataPixelUrlPrefix = dataPixelUrlPrefix;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getTargetingCode() {
        return targetingCode;
    }

    public void setTargetingCode(String targetingCode) {
        this.targetingCode = targetingCode;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getDataPixelUrlPrefix() {
        return dataPixelUrlPrefix;
    }

    public void setDataPixelUrlPrefix(String dataPixelUrlPrefix) {
        this.dataPixelUrlPrefix = dataPixelUrlPrefix;
    }

    @Override
    public String toString() {
        return "MatchResponse {designId=" + designId + ", targetingCode=" + targetingCode + ", deploymentId=" + deploymentId + ", setId=" + setId + ", dataPixelUrlPrefix="
                + dataPixelUrlPrefix + "}";
    }

}
