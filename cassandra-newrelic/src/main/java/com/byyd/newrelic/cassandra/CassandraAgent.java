package com.byyd.newrelic.cassandra;

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

public class CassandraAgent extends Agent {
    private final Logger logger = Logger.getLogger(CassandraAgent.class);

    private static final String GUID = "com.byyd.newrelic.cassandra.Adserver";
    private static final String VERSION = "1.0.0";
    private String name;
    private String host;
    private Long port;
    private String userName;
    private String password;

    private Map<String, Processor> processors = new HashMap<>();

    private CsMetrics[] all = new CsMetrics[] {
		new CsMetrics("org.apache.cassandra.net:type=FailureDetector", "DownEndpointCount", "net.DownEndpointCount"),
		new CsMetrics("org.apache.cassandra.net:type=FailureDetector", "UpEndpointCount", "net.UpEndpointCount"),
		new CsMetrics("org.apache.cassandra.metrics:type=CQL,name=PreparedStatementsCount", "Value", "stat.PreparedStatementsCount")
    };

    public CassandraAgent(String name, String host, Long port, String userName, String password) {
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
    	for (CsMetrics c : all) {
    		
            try {
            	Object attrValue = mbsc.getAttribute(new ObjectName(c.objectName), c.attributeName);
                Processor proc = getOrAdd(c.reportName);

                try {
                    Number value = asNumber(attrValue, attrValue != null ? attrValue.getClass().getName() : "");
    		
                    reportMetric("count/" + c.reportName, "count", value);
                    reportMetric("rate/" + c.reportName, "per sec", proc.process(value));
                } catch (Exception e) {
                    logger.error("eror getting attrName {} ", e);
                }

            } catch (Exception e) {
                logger.warn("missing " + c.attributeName);
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

    
    public class CsMetrics {
    	public String objectName;
    	public String attributeName;
    	public String reportName;
		
    	public CsMetrics(String objectName, String attributeName,
				String reportName) {
			super();
			this.objectName = objectName;
			this.attributeName = attributeName;
			this.reportName = reportName;
		}
    }
}
