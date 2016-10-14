package com.adfonic.adserver.impl;

import java.util.UUID;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

public class TestAdSpaceUtilsImpl extends BaseAdserverTest {
    private JmsTemplate centralJmsTemplate;
    private JmsUtils jmsUtils;
    private AdSpaceUtilsImpl adSpaceUtilsImpl;

    @Before
    public void runBeforeEachTest() {
        centralJmsTemplate = mock(JmsTemplate.class, "centralJmsTemplate");
        jmsUtils = mock(JmsUtils.class);
        adSpaceUtilsImpl = new AdSpaceUtilsImpl();
        inject(adSpaceUtilsImpl, "centralJmsTemplate", centralJmsTemplate);
        inject(adSpaceUtilsImpl, "jmsUtils", jmsUtils);
    }

    @Test
    public void testAdSpaceUtilsImpl_reactivateDormantAdSpace() {
        final String adSpaceExternalId = UUID.randomUUID().toString();
        expect(new Expectations() {
            {
                oneOf(jmsUtils).sendObject(centralJmsTemplate, JmsResource.ADSPACE_REACTIVATE, adSpaceExternalId);
            }
        });
        adSpaceUtilsImpl.reactivateDormantAdSpace(adSpaceExternalId);
    }
}
