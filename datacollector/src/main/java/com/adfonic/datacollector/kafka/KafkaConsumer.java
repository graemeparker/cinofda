package com.adfonic.datacollector.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.adfonic.datacollector.AdEventDataCollector;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.domain.cache.DataCollectorDomainCacheManager;

@Configuration
public class KafkaConsumer {
    
    private ConsumerConnector consumer;
    private static ExecutorService executor;
    @Autowired
    private ConsumerConfig consumerConfig;
    @Autowired
    private KafkaTopics kafkaTopics;
    @Value("${kafka.topic.failed:adevents.FAILED_prod_j1}")
    private String failedTopic;
    @Value("${kafka.consumer.max.thread.inactive.ms:300000}")
    private long maxThreadInactive;
    @Value("${kafka.consumer.check.active.threads.ms:120000}")
    private long checkActiveThreads;
    @Value("${kafka.consumer.unfilled.batch.size:1000}")
    private int batchSisze;
    
    @Autowired
    private AdEventDataCollector adEventDataCollector;
    @Autowired
    private ClusterDao clusterDao;
    @Autowired
    private AdEventConversionUtils adEventConversionUtils;
    @Autowired
    private DataCollectorDomainCacheManager dataCollectorDomainCacheManager;
    
    private Map<Integer, Long> threadsStatus;
    private Map<Integer, KafkaMessageProcessor> threads;
    private Map<Integer, KafkaStream<byte[], byte[]>> streams;
    private Map<Integer, String> threadTopics;
        
    private static final transient Logger LOG = Logger.getLogger(KafkaConsumer.class.getName());

    private Thread checkerThread;
    
    public KafkaConsumer(){
        this.threadsStatus = new ConcurrentHashMap<Integer, Long>();
        this.threads = new HashMap<Integer, KafkaMessageProcessor>();
        this.streams = new HashMap<Integer, KafkaStream<byte[], byte[]>>();
        this.threadTopics = new HashMap<Integer, String>();
    }
    
    @PostConstruct
    public void launch(){
    
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        int totalStreams = 0;
        for(KafkaTopicStream kts : kafkaTopics.getTopics()){
            if(kts.getStreams()>0){
                topicCountMap.put(kts.getTopic(), new Integer(kts.getStreams()));
            }
            totalStreams += kts.getStreams();
        }
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        
        // now launch all the threads
        executor = Executors.newFixedThreadPool(totalStreams);
        LOG.info(totalStreams + " threads created to consume from all topics");
        
        // now create the objects to consume all messages
        //
        int threadNumber = 0;
        for(KafkaTopicStream kts : kafkaTopics.getTopics()){
            if(kts.getStreams()>0){
                for(KafkaStream<byte[], byte[]> stream : consumerMap.get(kts.getTopic())){
                    LOG.fine("Creating Stream for topic " + kts.getTopic() + " on thread " + threadNumber);
                    KafkaMessageProcessor thread = new KafkaMessageProcessor(stream, threadNumber, threadsStatus, kts.getTopic().equals(failedTopic), kts.getTopic(), batchSisze, adEventDataCollector, clusterDao, adEventConversionUtils, dataCollectorDomainCacheManager);
                    executor.submit(thread);
                    this.threadsStatus.put(threadNumber, System.currentTimeMillis());
                    this.threads.put(threadNumber, thread);
                    this.streams.put(threadNumber, stream);
                    this.threadTopics.put(threadNumber, kts.getTopic());
                    threadNumber++;
                }
            }
        }
        LOG.info("All kafka streams created");
        //checkThreads(); // blocks forever!
        checkerThread = new Thread() {
            public void run() {
                checkThreads();
            };
        };
        checkerThread.start();
    } 
    
    
   
    public void checkThreads() {
        while(true){
            try{
                Thread.sleep(checkActiveThreads);
                for (Map.Entry<Integer, Long> entry : threadsStatus.entrySet()){
                    //Thread hasn't registered activity for 5 mins
                    if(System.currentTimeMillis() - entry.getValue() > maxThreadInactive){
                        LOG.warning("Thread " + entry.getKey() + " had " + (System.currentTimeMillis() - entry.getValue()) + " milliseconds whith no activity registered, stopping and creating new thread");
                        threads.get(entry.getKey()).interrupt();
                        KafkaMessageProcessor thread = new KafkaMessageProcessor(streams.get(entry.getKey()), entry.getKey(), threadsStatus,threadTopics.get(entry.getKey()).equals(failedTopic), threadTopics.get(entry.getKey()),batchSisze, adEventDataCollector, clusterDao, adEventConversionUtils, dataCollectorDomainCacheManager);
                        executor.submit(thread);
                        this.threadsStatus.put(entry.getKey(), System.currentTimeMillis());
                        this.threads.put(entry.getKey(), thread);
                        LOG.info("Thread successfully created for " + threadTopics.get(entry.getKey()));
                    }
                    
                }
            } catch (InterruptedException e) {
                LOG.info("Exiting cleanly");
            }
        }
    }
    
    @PreDestroy
    public void shutdown(){
        if(consumer!=null){
            consumer.shutdown();
        }
        if(checkerThread != null) {
            checkerThread.interrupt();
        }
        if(executor!=null){
            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                LOG.warning("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            LOG.warning("Interrupted during shutdown, exiting uncleanly");
        }
    }
}