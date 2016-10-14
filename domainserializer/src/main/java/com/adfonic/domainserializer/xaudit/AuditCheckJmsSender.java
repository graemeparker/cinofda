package com.adfonic.domainserializer.xaudit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.jms.CreativeApprovalMessage;
import com.adfonic.jms.JmsUtils;

public class AuditCheckJmsSender {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final JmsTemplate jmsTemplate;
    private final Queue approvalQueue;
    private final JmsUtils jmsUtils = new JmsUtils();

    public AuditCheckJmsSender(ConnectionFactory connectionFactory, Queue creativeApprovalQueue) {
        this.jmsTemplate = new JmsTemplate(connectionFactory);
        this.approvalQueue = creativeApprovalQueue;
    }

    public void syncExternalCreative(long creativeId, long publisherId) {
        long currentTime = System.currentTimeMillis();
        jmsUtils.sendObject(jmsTemplate, approvalQueue, new CreativeApprovalMessage(creativeId, publisherId));
        LOG.info("Sent CreativeApprovalMessage creative: " + creativeId + ", publisher: " + publisherId + "; JMS send took " + (System.currentTimeMillis() - currentTime) + " ms");

    }

    /**
     * Useful for tests
     */
    public static class FakeAuditCheckSender extends AuditCheckJmsSender {

        private Long creativeId;
        private Long publisherId;

        public FakeAuditCheckSender() {
            super(new FakeConnectionFactory(), null);
        }

        @Override
        public void syncExternalCreative(long creativeId, long publisherId) {
            this.creativeId = creativeId;
            this.publisherId = publisherId;

        }

        public void clear() {
            this.creativeId = null;
            this.publisherId = null;
        }

        public Long getCreativeId() {
            return creativeId;
        }

        public Long getPublisherId() {
            return publisherId;
        }

        private static class FakeConnectionFactory implements ConnectionFactory {

            @Override
            public Connection createConnection() throws JMSException {
                return null;
            }

            @Override
            public Connection createConnection(String userName, String password) throws JMSException {
                return null;
            }

        }

    }
}
