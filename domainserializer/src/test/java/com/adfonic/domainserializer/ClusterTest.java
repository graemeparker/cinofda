package com.adfonic.domainserializer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

public class ClusterTest {

    @Test
    public void createNewCluster() throws Exception {

        Properties properties = new Properties();
        DsCluster cluster = new DsCluster("myCluster", properties);

        assertThat(cluster.getName(), is("myCluster"));

    }
}
