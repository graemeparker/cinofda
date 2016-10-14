package com.adfonic.adserver.rtb.impl;

import java.io.Serializable;
import java.util.HashMap;

import javax.jms.Queue;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.Medium;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

@RunWith(MockitoJUnitRunner.class)
public class RtbIdServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private JmsTemplate centralJmsTemplate;

    @Mock
    private JmsUtils jmsUtils;
    @Mock
    private Queue rtbPublicationPersistenceQueue;
    @Mock
    private Queue rtbBundlePersistenceQueue;

    @Mock
    private DataSource cachedbDataSource;

    @InjectMocks
    private RtbIdServiceImpl testObj = new RtbIdServiceImpl(1, 100000);

    @Test
    public void testHandleUnrecognizedRtbId() throws InterruptedException {
        ByydRequest bidRequest = new ByydRequest("zx-zx-zx-zx", "123456");
        bidRequest.setPublicationName("siteOrAppName");
        bidRequest.setMedium(Medium.APPLICATION);
        bidRequest.setSellerNetworkId(666);
        bidRequest.setPublicationUrlString("http://PublicationUrlString");
        bidRequest.setBundleName("app.bundle.com");

        long publisherId = 12345;
        String rtbId = "rtbId-1234";

        testObj.handleUnrecognizedRtbId(bidRequest, publisherId, rtbId);
        Thread.sleep(200); // queued and asynchornously sent 

        HashMap<String, Serializable> msg = new HashMap<String, Serializable>();
        msg.put("publication.urlString", "http://PublicationUrlString");
        msg.put("publisher.id", publisherId);
        msg.put("publication.rtbId", rtbId);
        msg.put("publication.name", "siteOrAppName -app");
        msg.put("publication.publicationType.systemName", "OTHER_APP");
        msg.put("publicationProvidedInfo.sellerNetworkId", 666);
        msg.put("publication.bundle", "app.bundle.com");

        Mockito.verify(jmsUtils).sendObject(centralJmsTemplate, JmsResource.RTB_PUBLICATION_PERSISTENCE, msg);

    }

    @Test
    public void testhandleBundleAssociation() throws InterruptedException {
        ByydRequest bidRequest = new ByydRequest("zx-zx-zx-zx", "1234567");
        bidRequest.setBundleName("app.bundle.com");

        long publicationId = 12345;

        testObj.handleBundleAssociation(bidRequest, publicationId);
        Thread.sleep(200); // queued and asynchornously sent

        HashMap<String, Serializable> msg = new HashMap<String, Serializable>();
        msg.put("publication.id", publicationId);
        msg.put("publication.bundle", "app.bundle.com");

        Mockito.verify(jmsUtils).sendObject(centralJmsTemplate, JmsResource.RTB_APP_BUNDLE_PERSISTENCE, msg);

    }

}
