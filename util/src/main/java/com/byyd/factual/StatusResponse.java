package com.byyd.factual;

import java.util.List;

/**
 * 
 * @author mvanek
 *
 * JSON reponse from /zz/status
 * 
 * http://developer.factual.com/geopulse-on-prem-overview/#server_status
 */
public class StatusResponse {

    public static class ProximityStatus {

        private String state;
        private double requiredMemory;
        private List<IndexStatus> indices;

    }

    public static class AudienceStatus {

    }

    public static class IndexStatus {
        private String designName;
        private String designId;
        private List<String> targetingCodes;
    }

}
