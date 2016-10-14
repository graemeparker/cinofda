package com.byyd.newrelic.cassandra;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;

import javax.management.AttributeValueExp;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.StringValueExp;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;

public class AccessIT {

	@Test
	@SuppressWarnings("rawtypes")
	public void test() throws Exception {
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, new HashMap());
        ObjectName objectName = new ObjectName("org.apache.cassandra.net:type=FailureDetector");
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        Object attrValue = mbsc.getAttribute(objectName, "UpEndpointCount");
        System.out.println("f: " + attrValue);
	}

}
