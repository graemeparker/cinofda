package com.adfonic.tracker.config;

import java.util.Properties;

import kafka.producer.ProducerConfig;
import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.tracker.kafka.TrackerKafka;
import com.adfonic.tracker.kafka.impl.TrackerKafkaImpl;


@Configuration
public class TrackerKafkaConfig {
    
    private final String APP_NAME = "Tracker";
    
    @Bean
    public TrackerKafka trackerKafka(@Value("${KafkaLogger.zookeepers}") String kafkaZookeeper, @Value("${KafkaLogger.brokers}") String kafkaBrokers, @Value("${KafkaLogger.topicprefix:adevents}") String topicPrefix, 
            @Value("${KafkaLogger.environment}") String environment, @Value("${KafkaLogger.topicposfix:j1}") String topicPosfix, @Value("${KafkaLogger.compression.codec}") String compressionCodec,
            @Value("${KafkaLogger.producer.type}") String producerType, @Value("${KafkaLogger.maxqueuetime.ms}") String kafkaMaxQueueTimeMs, @Value("${KafkaLogger.batchMessages}") Integer kafkaBatchMessages,
            @Value("${KafkaLogger.buffer.size}") String kafkaBufferSize, @Value("${KafkaLogger.refreshinterval.ms}") String kafkaRefreshInterval, @Value("${KafkaLogger.serializer}") String kafkaSerializer,
            @Value("${KafkaLogger.requestrequiredacks}") String rReqAcks, @Value("${KafkaLogger.enabled}") boolean kafkaLoggerEnabled ) {
        
        Properties props = new Properties();
        
        props.put("zk.connect", kafkaZookeeper);
        props.put("metadata.broker.list", kafkaBrokers);
        props.put("compression.codec", compressionCodec);
        props.put("producer.type", producerType);
        props.put("queue.buffering.max.ms", kafkaMaxQueueTimeMs);
        props.put("queue.buffering.max.messages", Integer.toString((kafkaBatchMessages*10)));
        props.put("batch.num.messages",Integer.toString(kafkaBatchMessages));
        props.put("send.buffer.bytes", kafkaBufferSize);
        props.put("topic.metadata.refresh.interval.ms", kafkaRefreshInterval);
        props.put("client.id", APP_NAME);
        
        props.put("serializer.class", kafkaSerializer);
        props.put("request.required.acks",rReqAcks);
         
        ProducerConfig config = new ProducerConfig(props);
        
        return new TrackerKafkaImpl(config, topicPrefix, topicPosfix, environment, kafkaLoggerEnabled);
    }
    
    @Bean
    public V1DomainModelMapper domainModelMapper(){
        return new V1DomainModelMapper();
    }
    
   
    
}
