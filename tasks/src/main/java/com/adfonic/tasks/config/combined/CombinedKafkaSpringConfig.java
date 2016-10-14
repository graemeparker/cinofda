package com.adfonic.tasks.config.combined;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mapping JMS consumers to not have it in textual spring xml file (more type-safe you know...)
 * 
 * They should not be started for single off tasks...
 * 
 * @author jamat
 *
 */
@Configuration
public class CombinedKafkaSpringConfig {
    
    private final String APP_NAME = "Tasks";

    @Bean
    public Producer<String, String> kafkaProducer(@Value("${KafkaLogger.zookeepers}") String kafkaZookeeper, @Value("${KafkaLogger.brokers}") String kafkaBrokers,  
            @Value("${KafkaLogger.compression.codec}") String compressionCodec,
            @Value("${KafkaLogger.producer.type}") String producerType, @Value("${KafkaLogger.maxqueuetime.ms}") String kafkaMaxQueueTimeMs, @Value("${KafkaLogger.batchMessages}") Integer kafkaBatchMessages,
            @Value("${KafkaLogger.buffer.size}") String kafkaBufferSize, @Value("${KafkaLogger.refreshinterval.ms}") String kafkaRefreshInterval, @Value("${KafkaLogger.serializer}") String kafkaSerializer,
            @Value("${KafkaLogger.requestrequiredacks}") String rReqAcks ) {
        
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
        return  new Producer<String, String>(config);
    }
    
    @Bean
    public String topicPrefix(@Value("${KafkaLogger.topicprefix:adevents}") String topicPrefix){
        return topicPrefix;
    }
    
    @Bean
    public String topicPosfix(@Value("${KafkaLogger.environment}") String environment, @Value("${KafkaLogger.topicposfix:j1}") String topicPosfix){
        return environment + "_" + topicPosfix;
    }
    
    @Bean
    public boolean kafkaEnabled(@Value("${KafkaLogger.enabled}") boolean kafkaLoggerEnabled){
        return kafkaLoggerEnabled;
    }
    
    @Bean
    public V1DomainModelMapper domainModelMapper(){
        return new V1DomainModelMapper();
    }
}
