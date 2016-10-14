package com.adfonic.retargeting.citrusleaf;

import java.util.HashMap;
import java.util.Map;

import net.citrusleaf.CitrusleafClient;
import net.citrusleaf.CitrusleafClient.ClOptions;
import net.citrusleaf.CitrusleafClient.ClResult;
import net.citrusleaf.CitrusleafClient.ClResultCode;
import net.citrusleaf.CitrusleafClient.ClWriteOptions;

public class TestCLData {

    private static final String namespace = "retargeting";
    private static final String SET_NAME = "TEST";
    private static final String INCLUDE_CAMPAIGN_KEY = "IC";
    private static final ClOptions clOptions = new ClOptions(60000);

    private static final String master = "lon3cache01";
    private static final String slave = "10.97.1.42";

    public static void main(String[] args) throws CitrusleafException {

        CitrusleafClient masteServerCitrusleafClient = new CitrusleafClient(master, 30000);
        CitrusleafClient slaveServerCitrusleafClient = new CitrusleafClient(slave, 30000);

        String key = "company";
        String data = "adfonic";
        ClWriteOptions clWriteOptions = new ClWriteOptions();
        clWriteOptions.expiration = 100000;

        // Write to Master
        writeToCache(masteServerCitrusleafClient, key, data, "Master", clWriteOptions);
        System.out.println("Waiting for write to replicate");
        waitForFewSeconds(50);
        // Now read back from Master server
        String masterData = readFromCache(masteServerCitrusleafClient, key, "Master");
        System.out.println("1. Master server returned " + masterData);
        String slaveData = readFromCache(slaveServerCitrusleafClient, key, "Slave");
        System.out.println("2. Slave server returned " + slaveData);
        if (masterData.equals(slaveData)) {
            System.out.println("** Write replication Test Pass");
        } else {
            System.out.println("** Write replication Test Failed");
        }
        // Delete from Master Server
        deleteFromCache(masteServerCitrusleafClient, key, "Master");
        System.out.println("Waiting for delete to replicate");
        waitForFewSeconds(10);

        masterData = readFromCache(masteServerCitrusleafClient, key, "Master");
        System.out.println("3. Master server returned " + masterData);
        slaveData = readFromCache(slaveServerCitrusleafClient, key, "Slave");
        System.out.println("4. Slave server returned " + slaveData);
        if (null == slaveData) {
            System.out.println("** Delete replication Test Pass");
        } else {
            System.out.println("** Delete replication Test Failed");
        }

        System.out.println("Writing to Master Server again with Expiration as 5 seconds");
        clWriteOptions = new ClWriteOptions();
        clWriteOptions.expiration = 5;
        String updatedData = "NewAdfonic";
        writeToCache(masteServerCitrusleafClient, key, updatedData, "Master", clWriteOptions);

        System.out.println("Waiting for write to replicate");
        waitForFewSeconds(20);

        masterData = readFromCache(masteServerCitrusleafClient, key, "Master");
        System.out.println("Master server returned " + masterData);
        slaveData = readFromCache(slaveServerCitrusleafClient, key, "Slave");
        System.out.println("Slave server returned " + slaveData);

        if (masterData.equals(slaveData)) {
            System.out.println("** Write replication Test Pass");
        } else {
            System.out.println("** Write replication Test Failed");
        }

        System.out.println("Waiting for expire to replicate");
        waitForFewSeconds(50);

        masterData = readFromCache(masteServerCitrusleafClient, key, "Master");
        System.out.println("Master server returned " + masterData);
        slaveData = readFromCache(slaveServerCitrusleafClient, key, "Slave");
        System.out.println("Slave server returned " + slaveData);

    }

    private static void writeToCache(CitrusleafClient citrusleafClient, String key, String data, String server, ClWriteOptions clWriteOptions) throws CitrusleafException {
        System.out.println("Writing Key = '" + key + "' and data = '" + data + "' to '" + server + "' server");
        Map<String, Object> mapToSave = new HashMap<>();
        mapToSave.put(INCLUDE_CAMPAIGN_KEY, data);
        ClResultCode resultCode = citrusleafClient.set(namespace, SET_NAME, key, mapToSave, clOptions, clWriteOptions);
        switch (resultCode) {
        case OK:
            // logger.info("Success CitrusleafClient.set(" + namespace + ", " +
            // SET_NAME + ", " + deviceIdKey + ", " + mapToSave+ ", " +
            // clOptions + ", "+clWriteOptions+")");
            break;
        default:
            throw new CitrusleafException("CitrusleafClient.set(" + namespace + ", " + SET_NAME + ", " + key + ", " + mapToSave + ", " + clOptions + ", " + clWriteOptions
                    + ") failed: " + resultCode + " ," + ClResult.resultCodeToString(resultCode));
        }

    }

    private static void deleteFromCache(CitrusleafClient citrusleafClient, String key, String server) throws CitrusleafException {
        // logger.info("CitrusleafClient.set(" + namespace + ", " + NO_SET +
        // ", " + deviceIdKey + ", " + mapToSave+ ", " + clOptions + ", null)");
        System.out.println("Deleting Key = '" + key + "' from '" + server + "' server");
        ClResultCode resultCode = citrusleafClient.delete(namespace, SET_NAME, key, clOptions, null);
        switch (resultCode) {
        case OK:
            // logger.info("Success CitrusleafClient.delete(" + namespace + ", "
            // + SET_NAME + ", " + deviceIdKey + ", " + clOptions + ", null)");
            break;
        default:
            throw new CitrusleafException("CitrusleafClient.delete(" + namespace + ", " + SET_NAME + ", " + key + ", " + clOptions + ", null)" + resultCode + " ,"
                    + ClResult.resultCodeToString(resultCode));
        }

    }

    private static String readFromCache(CitrusleafClient citrusleafClient, String key, String server) {
        System.out.println("Reading Key = '" + key + "' from '" + server + "' server");
        ClResult result = citrusleafClient.getAll(namespace, SET_NAME, key, clOptions);
        if (result != null && result.resultCode != ClResultCode.OK) {
            // System.out.println("Failed to get dmp audience data for key=" +
            // key + ": resultCode=" + result.resultCode + " " +
            // ClResult.resultCodeToString(result.resultCode));
            return null;
        } else if (result == null || result.results == null) {
            // System.out.println("No results for key=" + key);
            return null;
        }
        // System.out.println("Got result for key=" + key);
        Map<String, Object> resultMap = result.results;
        String targettedCampaigns = (String) resultMap.get(INCLUDE_CAMPAIGN_KEY);
        return targettedCampaigns;

    }

    private static void waitForFewSeconds(int seconds) {
        System.out.println("Waiting for " + seconds + " Seconds");
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
