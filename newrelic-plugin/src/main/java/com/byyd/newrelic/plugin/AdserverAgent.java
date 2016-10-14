package com.byyd.newrelic.plugin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang.StringUtils;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;
import com.newrelic.metrics.publish.util.Logger;

public class AdserverAgent extends Agent {
    private final Logger logger = Logger.getLogger(AdserverAgent.class);

    private static final String GUID = "com.byyd.newrelic.plugin.Adserver";
    private static final String VERSION = "1.0.0";
    private String name;
    private String host;
    private Long port;
    private String userName;
    private String password;

    private Map<String, Processor> processors = new HashMap<>();

    public AdserverAgent(String name, String host, Long port, String userName, String password) {
        super(GUID, VERSION);
        this.name = name;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;

        try {
            String localHostName = InetAddress.getLocalHost().getHostName();
            if( StringUtils.isBlank(this.name)) {                
                this.name = localHostName;
            }
        } catch (UnknownHostException e) {
            logger.error("failed to get hostname", e);
        }
    }

    @Override
    public String getAgentName() {
        return name;
    }

    @Override
    public void pollCycle() {
        try (JMXConnector jmxc = getMbeanServerConnection()) {
            reportMetricData(jmxc.getMBeanServerConnection());

        } catch (Exception e) {
            logger.error("name {} host {} ", name, host, e);
        }
    }

    void reportMetricData(MBeanServerConnection mbsc) throws Exception {
        ObjectName objectName = new ObjectName("AdfonicCounters:mbean=GenericCounters");
        MBeanInfo mBeanInfo = mbsc.getMBeanInfo(objectName);
        MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        for (MBeanAttributeInfo attr : attributes) {
            String type = attr.getType();
            String attrName = attr.getName();

            Processor proc = getOrAdd(attrName);

            try {
                Object attrValue = mbsc.getAttribute(objectName, attrName);

                try {
                    Number value = asNumber(attrValue, type);
                    String countName = "count/" + attrName;
                    reportMetric(countName, "count", value);

                    String rateName = "rate/" + attrName;
                    reportMetric(rateName, "per sec", proc.process(value));
                    logger.info(attrName + " -> " + attrValue);
                } catch (Exception e) {
                    logger.error("eror getting attrName {} ", e);
                }

            } catch (Exception e) {
                logger.warn("missing " + attrName);
            }
        }
    }

    private Processor getOrAdd(String attrName) {
        Processor p = processors.get(attrName);
        if (p == null) {
            p = new EpochProcessor();
            processors.put(attrName, p);
        }

        return p;
    }

    JMXConnector getMbeanServerConnection() throws MalformedURLException, IOException {
        Map<String, String[]> map = buildCredentials();
        String serviceURL = buildJmxServiceUrl();

        JMXServiceURL url = new JMXServiceURL(serviceURL);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, map);

        return jmxc;
    }

    Map<String, String[]> buildCredentials() {
        String[] creds = { userName, password };
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(JMXConnector.CREDENTIALS, creds);
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
            map = null;
        }
        return map;
    }

    String buildJmxServiceUrl() {
        String serviceURL = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        return serviceURL;
    }

    Number asNumber(Object attrValue, final String type) {
        // TODO CounterJmxManager wrongly reports String, but we only have java.lang.Long
        if (attrValue == null) {
            return null;
        }

        switch (type) {
        case "java.lang.String":
            long sValue = Long.parseLong((String) attrValue);
            return sValue;

        case "java.lang.Long":
            long longValue = (Long) attrValue;
            return longValue;
        case "java.lang.Integer":
            int iValue = (Integer) attrValue;
            return iValue;

        case "java.lang.Double":
            double dValue = (Double) attrValue;
            return dValue;
        case "java.lang.Float":
            float fValue = (Float) attrValue;
            return fValue;

        default:
            logger.info("{} not supported", type);
            break;
        }
        return null;
    }

}
