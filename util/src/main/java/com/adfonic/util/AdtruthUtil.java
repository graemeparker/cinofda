package com.adfonic.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.deviceinsight.api.DeviceInsight;
import com.deviceinsight.api.DeviceInsightException;
import com.deviceinsight.api.LocalTime;
import com.deviceinsight.config.Configuration;
import com.deviceinsight.config.ConfigurationBuilder;
import com.deviceinsight.config.Version;
import com.deviceinsight.logging.LoggerBuilder;
import com.deviceinsight.utils.DeviceInsightUtils;

public class AdtruthUtil {

    private static final Logger LOG = Logger.getLogger(AdtruthUtil.class.getName());

    public static String getAtid(HttpServletRequest request, String adtruthData, String browserIp, Map<String, String> metadata) {
        LocalTime serverTime = LocalTime.utcNow();
        String atid = null;
        try {
            String encodedAdtruthData = URLEncoder.encode(adtruthData, "UTF-8");
            Configuration configuration = new ConfigurationBuilder(new Version("WEB_APP_BRIDGE_4_0")).build();
            DeviceInsight deviceInsight = new DeviceInsight(DeviceInsightUtils.extractHttpHeaders(request), encodedAdtruthData, serverTime, browserIp, configuration);

            // in order to control the logging for DeviceInsights, we only log
            // if the Level is FINE
            if (LOG.isLoggable(Level.FINE)) {
                // this method is currently called from adserver or tracker.
                // writing the DI logs to tomcat logs folder
                String logPath = System.getProperty("catalina.base");
                // if the logs folder is not present, write them to /tmp/logs
                // folder
                if (logPath == null) {
                    logPath = "/tmp";
                }
                File logFile = new File(logPath + "/logs");
                if ((!logFile.exists()) && (!logFile.mkdirs())) {
                    LOG.warning("Can not create log folder: " + logFile);
                }
                com.deviceinsight.logging.Logger logger = new LoggerBuilder(logFile).withMaxLogFileSize(1000000).withBufferCapacity(100).build();
                // only write to DI Logs if the logger was created
                if (logger != null) {
                    LOG.fine("DI logging to " + logger.getLoggingDirectory());
                    logger.log(metadata, deviceInsight);
                    logger.shutdown();
                }
            }

            atid = deviceInsight.getDeviceInsightId();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Adtruth Id " + atid + " generated for un-encoded payload:  " + adtruthData + "  on " + serverTime + " for client IP " + browserIp);
                LOG.fine("Encoded adtruthData: " + encodedAdtruthData);
            }
        } catch (DeviceInsightException de) {
            LOG.warning("Something went wrong while generating the ATID for un-encoded payload:  " + adtruthData + "  on " + serverTime + " for client IP " + browserIp);
        } catch (UnsupportedEncodingException ue) {
            LOG.warning("Something went wrong while encoding adtruthData:  " + adtruthData + "  on " + serverTime + " for client IP " + browserIp);
        }
        return atid;
    }
}
